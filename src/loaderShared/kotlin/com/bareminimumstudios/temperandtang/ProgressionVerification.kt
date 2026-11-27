package com.bareminimumstudios.temperandtang

import com.bareminimumstudios.temperandtang.compat.TemplateLoot
import com.bareminimumstudios.temperandtang.content.TemperContent
import com.bareminimumstudios.temperandtang.mixin.SmithingIngredientsAccess
import com.bareminimumstudios.temperandtang.smithing.*
import kotlin.math.abs
import net.minecraft.commands.CommandSourceStack
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.*
import net.minecraft.world.item.component.ItemAttributeModifiers
import net.minecraft.world.item.crafting.*
import net.minecraft.world.level.block.Blocks

/** Read-only recipe/quality checks; all stacks are local and no player inventory or world clock is modified. */
object ProgressionVerification {
    fun run(source:CommandSourceStack) {
        val level=source.level;val registry=level.registryAccess();val manager=level.recipeManager
        check(CraftGrade.entries.size==5)
        check(CraftGrade.parse("EXCEPTIONAL")==CraftGrade.MASTERWORK)
        val stone=ItemStack(Items.STONE_PICKAXE);val bronze=ItemStack(TemperContent.item("bronze_pickaxe"))
        check(!stone.isCorrectToolForDrops(Blocks.IRON_ORE.defaultBlockState()))
        check(bronze.isCorrectToolForDrops(Blocks.IRON_ORE.defaultBlockState()))
        check(!bronze.isCorrectToolForDrops(Blocks.DIAMOND_ORE.defaultBlockState()))
        val diamondCraft=manager.getAllRecipesFor(RecipeType.CRAFTING).filter {holder->
            val id=BuiltInRegistries.ITEM.getKey(holder.value().getResultItem(registry).item)
            id.path.startsWith("diamond_") && GearSupport.supported(holder.value().getResultItem(registry))
        }
        check(diamondCraft.isEmpty()){ "Diamond crafting shortcut remains: ${diamondCraft.map {it.id()}}" }
        val plans=WorkshopPlans.forming(level)
        check(plans.none {BuiltInRegistries.ITEM.getKey(it.recipe.getResultItem(registry).item).path.startsWith("diamond_")}) {"Diamond shaping shortcuts: "+plans.filter {BuiltInRegistries.ITEM.getKey(it.recipe.getResultItem(registry).item).path.startsWith("diamond_")}.map {it.id to it.recipe.getResultItem(registry)}}
        for(part in listOf("sword","pickaxe","axe","shovel","hoe","helmet","chestplate","leggings","boots")) {
            val item=TemperContent.item("tin_"+part);val stack=ItemStack(item)
            check(GearSupport.supported(stack) && CraftMaterials.identify(stack)==ForgeMaterial.TIN)
            check(plans.any {it.recipe.getResultItem(registry).item==item}) {"Missing tin shaping recipe: $part"}
            val refined=Craftsmanship.apply(CraftMaterials.stamp(stack,ForgeMaterial.TIN),CraftRoll(CraftGrade.FINE,.8,.8,.5,10),"TinFixture","temperandtang:tin_"+part)
            check(!refined.isEmpty && Craftsmanship.grade(refined)==CraftGrade.FINE)
            val restored=ItemStack.parseOptional(registry,refined.save(registry) as CompoundTag)
            check(CraftMaterials.recorded(restored)==ForgeMaterial.TIN && restored.maxDamage==refined.maxDamage)
        }
        val tinPick=ItemStack(TemperContent.item("tin_pickaxe"))
        check(tinPick.isCorrectToolForDrops(TemperContent.block("tin_ore").defaultBlockState()))
        check(!tinPick.isCorrectToolForDrops(Blocks.IRON_ORE.defaultBlockState())) {"Tin bypasses bronze mining progression"}
        TemperAndTang.logger.info("TIN_CATALOG_PASSED: all nine native recipes, forging, saved material/quality and lower-tier mining restrictions")
        val silver=ItemStack(TemperContent.item("silver_ingot"))
        check(WorkshopPlans.stock(silver) && CraftMaterials.identify(silver)==ForgeMaterial.SILVER){"Silver tag mismatch: stock=${WorkshopPlans.stock(silver)} material=${CraftMaterials.identify(silver)} tags=${silver.tags.toList()}"}
        for(name in listOf("raw_silver","silver_ore","deepslate_silver_ore")) {
            val refining=checkNotNull(WorkshopPlans.heating(ItemStack(TemperContent.item(name)),level)){"Silver cannot be refined: $name"}
            check(refining.output.item==silver.item && refining.output.count==1)
        }
        check(ItemStack(Items.IRON_PICKAXE).isCorrectToolForDrops(TemperContent.block("silver_ore").defaultBlockState()))
        check(!bronze.isCorrectToolForDrops(TemperContent.block("silver_ore").defaultBlockState()))
        val silverPlans=plans.filter {BuiltInRegistries.ITEM.getKey(it.recipe.getResultItem(registry).item).let {id->id.namespace=="magistuarmory" && id.path.startsWith("silver_")}}
        if(BuiltInRegistries.ITEM.containsKey(ResourceLocation.fromNamespaceAndPath("magistuarmory","iron_shortsword")))check(silverPlans.isNotEmpty()){ "Epic Knights silver recipes did not resolve common silver ingots" }
        for(p in silverPlans) {
            check(p.requiresBlueprint)
            check(CraftMaterials.choose(p.recipe.getResultItem(registry),p.ghosts)==ForgeMaterial.SILVER)
        }
        TemperAndTang.logger.info("SILVER_ROUTE_PASSED: ore/raw refinement, iron mining gate, common silver stock, {} Epic Knights blueprint patterns",silverPlans.size)
        val template=ItemStack(TemperContent.item("diamond_upgrade_smithing_template"))
        check(manager.recipes.none {it.value().getResultItem(registry).item==template.item}) {"Structure-only diamond template has a manufacturing recipe"}
        var upgrades=0;var native=0
        for(holder in manager.getAllRecipesFor(RecipeType.SMITHING)) {
            val recipe=holder.value();val output=recipe.getResultItem(registry);val id=BuiltInRegistries.ITEM.getKey(output.item)
            if(!id.path.startsWith("diamond_") || !GearSupport.supported(output) || !recipe.isTemplateIngredient(template))continue
            val transform=when(recipe) {is SteelUpgradeRecipe->recipe.base;is SmithingTransformRecipe->recipe;else->error("Unknown template recipe: ${holder.id()}")}
            val ingredient=(transform as SmithingIngredientsAccess).temperBase()
            val base=ingredient.items.firstOrNull()?.copy()?:error("Empty upgrade base: ${holder.id()}")
            if(id.namespace!="minecraft")native++
            if(base.isEmpty)continue
            val marked=Craftsmanship.apply(CraftMaterials.stamp(base,ForgeMaterial.STEEL),CraftRoll(CraftGrade.SUPERIOR,.9,.85,.5,10),"UpgradeFixture","fixture")
            marked.damageValue=7;marked.set(DataComponents.CUSTOM_NAME,Component.literal("Preserved steel core"))
            val input=SmithingRecipeInput(template,marked,ItemStack(Items.DIAMOND))
            check(recipe.matches(input,level)){"No steel-to-diamond match: ${holder.id()}"}
            check(!recipe.matches(SmithingRecipeInput(ItemStack(Items.PAPER),marked,ItemStack(Items.DIAMOND)),level))
            if(recipe is SteelUpgradeRecipe)check(!recipe.matches(SmithingRecipeInput(template,base,ItemStack(Items.DIAMOND)),level)){"Unforged iron bypasses steel"}
            if(recipe is SteelUpgradeRecipe)check(plans.any {it.recipe.getResultItem(registry).item==base.item && it.ghosts.any {stock->CraftMaterials.identify(stock)==ForgeMaterial.STEEL}}){"No obtainable steel-core pattern for ${holder.id()}"}
            val upgraded=recipe.assemble(input,registry)
            check(upgraded.item==output.item && upgraded.damageValue==7)
            check(Craftsmanship.grade(upgraded)==CraftGrade.SUPERIOR && CraftMaterials.recorded(upgraded)==ForgeMaterial.DIAMOND)
            check(upgraded.get(DataComponents.CUSTOM_NAME)==marked.get(DataComponents.CUSTOM_NAME))
            check(Craftsmanship.root(upgraded).getInt("origin_durability")==output.item.defaultInstance.maxDamage)
            upgrades++
        }
        check(upgrades>=9){"Missing vanilla diamond upgrade families"}
        if(TemperAndTang.modLoaded("simplyswords"))check(native>0){"Missing native weapon upgrade recipes"}
        for(item in BuiltInRegistries.ITEM) {
            val stack=ItemStack(item);val id=BuiltInRegistries.ITEM.getKey(item)
            if(id.namespace in setOf("simplyswords","simplymore") && !id.path.matches(Regex("(iron|gold|golden|diamond|netherite)_.+")))check(!GearSupport.supported(stack)){"Unique/runic item accepted: $id"}
        }
        check(manager.getAllRecipesFor(RecipeType.CRAFTING).none {BuiltInRegistries.ITEM.getKey(it.value().getResultItem(registry).item).path.startsWith("netherite_") && GearSupport.supported(it.value().getResultItem(registry))}) {"Netherite crafting shortcut remains"}
        check(plans.none {!it.polishing && BuiltInRegistries.ITEM.getKey(it.recipe.getResultItem(registry).item).path.startsWith("netherite_")}) {"Netherite equipment shaping shortcut remains"}
        var netheriteUpgrades=0
        val netheriteTemplate=ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)
        for(holder in manager.getAllRecipesFor(RecipeType.SMITHING)) {
            val recipe=holder.value();val output=recipe.getResultItem(registry)
            if(!BuiltInRegistries.ITEM.getKey(output.item).path.startsWith("netherite_") || !GearSupport.supported(output) || !recipe.isTemplateIngredient(netheriteTemplate))continue
            val transform=recipe as? SmithingTransformRecipe?:continue
            val base=(transform as SmithingIngredientsAccess).temperBase().items.firstOrNull()?.copy()?:error("Empty netherite base: ${holder.id()}")
            check(BuiltInRegistries.ITEM.getKey(base.item).path.startsWith("diamond_")){"Netherite skips diamond: ${holder.id()}"}
            val marked=Craftsmanship.apply(CraftMaterials.stamp(base,ForgeMaterial.DIAMOND),CraftRoll(CraftGrade.SUPERIOR,.9,.85,.5,10),"NetheriteFixture","fixture")
            marked.damageValue=7;marked.set(DataComponents.CUSTOM_NAME,Component.literal("Preserved diamond core"))
            val input=SmithingRecipeInput(netheriteTemplate,marked,ItemStack(Items.NETHERITE_INGOT))
            check(recipe.matches(input,level)){"Unreachable netherite upgrade: ${holder.id()}"}
            val result=recipe.assemble(input,registry)
            check(result.item==output.item && result.damageValue==7 && Craftsmanship.grade(result)==CraftGrade.SUPERIOR && CraftMaterials.recorded(result)==ForgeMaterial.NETHERITE)
            check(result.get(DataComponents.CUSTOM_NAME)==marked.get(DataComponents.CUSTOM_NAME))
            check(Craftsmanship.root(result).getInt("origin_durability")==output.item.defaultInstance.maxDamage)
            netheriteUpgrades++
        }
        check(netheriteUpgrades>=9){"Missing diamond-to-netherite upgrade families"}
        TemperAndTang.logger.info("NETHERITE_UPGRADES_PASSED: {} diamond bases, required vanilla templates, preserved quality/name/wear, no crafting or shaping shortcuts",netheriteUpgrades)
        val primary=ItemStack(Items.NETHERITE_SCRAP,4);val gold=ItemStack(Items.GOLD_INGOT,4)
        check(WorkshopPlans.heating(primary,level,gold)==null){"Regular hearth allows netherite alloy"}
        val plan=checkNotNull(WorkshopPlans.heating(primary,level,gold,true))
        check(plan.output.item==Items.NETHERITE_INGOT && plan.output.count==1 && plan.additiveCost==4)
        check(HearthProcesses.duration(plan,4)==1200)
        check(WorkshopPlans.heating(primary.copyWithCount(3),level,gold,true)==null)
        check(WorkshopPlans.heating(primary,level,gold.copyWithCount(3),true)==null)
        check(plans.none {it.id==TemperAndTang.id("netherite_refining")}) {"Obsolete billet refinement remains mandatory"}
        check(TemplateLoot.pool(ResourceLocation.withDefaultNamespace("chests/desert_pyramid"))!=null)
        check(TemplateLoot.pool(ResourceLocation.withDefaultNamespace("chests/abandoned_mineshaft"))!=null)
        check(TemplateLoot.pool(ResourceLocation.withDefaultNamespace("blocks/stone"))==null)
        val sample=ItemStack(Items.IRON_PICKAXE)
        val values=CraftGrade.entries.map {grade->Craftsmanship.apply(CraftMaterials.stamp(sample,ForgeMaterial.IRON),CraftRoll(grade,.9,.8,.5,10),"QualityFixture","fixture")}
        check(values.zipWithNext().all {(lo,hi)->lo.maxDamage<hi.maxDamage})
        for((grade,gear) in CraftGrade.entries.zip(values)) {
            val mods=gear.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,ItemAttributeModifiers.EMPTY).modifiers()
            val mining=mods.firstOrNull {it.modifier().id()==TemperAndTang.id("craft_mining_speed")}?.modifier()?.amount()?:0.0
            check(abs(mining-(grade.mining*TemperAndTang.config.qualityPower.get()+ForgeMaterial.IRON.speed*TemperAndTang.config.materialPower.get()))<.00001)
            val saved=gear.save(registry);val loaded=ItemStack.parseOptional(registry,saved as CompoundTag)
            check(Craftsmanship.grade(loaded)==grade && loaded.maxDamage==gear.maxDamage)
        }
        TemperAndTang.logger.info("PROGRESSION_VERIFY_PASSED: five grades, bronze mining, {} diamond upgrades ({} native), preserved steel quality, excluded unique/runic gear, counted furnace alloy and direct netherite output",upgrades,native)
    }
}
