package com.bareminimumstudios.temperandtang.compat.viewer

import com.bareminimumstudios.temperandtang.TemperAndTang
import com.bareminimumstudios.temperandtang.content.TemperContent
import com.bareminimumstudios.temperandtang.mixin.SmithingIngredientsAccess
import com.bareminimumstudios.temperandtang.smithing.*
import java.util.Locale
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.*
import net.minecraft.world.item.crafting.*
import net.minecraft.world.level.Level

enum class WorkshopCategory(val station:String) { REFINING("minecraft:furnace"),ALLOYING("alloy_forge"),FORGING("shaping_anvil"),FINISHING("finishing_anvil"),UPGRADING("minecraft:smithing_table"),BLUEPRINTS("shaping_anvil");
    val key get()=name.lowercase(Locale.ROOT)
    fun icon()=if(station.contains(':'))ItemStack(if(station=="minecraft:furnace")Items.FURNACE else Items.SMITHING_TABLE)else ItemStack(TemperContent.item(station))
}
data class WorkshopDisplay(val id:ResourceLocation,val category:WorkshopCategory,val inputs:List<List<ItemStack>>,val output:ItemStack,val catalysts:List<ItemStack>,val note:String,val detail:String="",val grid:Boolean=false) {
    fun location(index:Int)=if(grid && index<9)(4+index%3*18) to (18+index/3*18)else (if(grid)76 else 4+index*24) to (if(grid)18+(index-9)*20 else 36)
}
/** Shared, read-only projection of the synchronized server recipe graph; viewers never mint outputs or transfer items. */
object WorkshopDisplays {
    fun collect(level:Level):List<WorkshopDisplay> {
        val out=mutableListOf<WorkshopDisplay>();val registry=level.registryAccess()
        fun id(kind:String,source:ResourceLocation)=TemperAndTang.id("viewer/$kind/"+source.namespace+"/"+source.path)
        fun list(item:Item,count:Int=1)=listOf(ItemStack(item,count))
        for(holder in level.recipeManager.getAllRecipesFor(RecipeType.SMELTING)) {
            val recipe=holder.value();val inputs=recipe.ingredients.firstOrNull()?.items?.filter(HearthProcesses::refinable)?:continue
            if(inputs.isEmpty() || !WorkshopPlans.stock(recipe.getResultItem(registry)))continue
            out+=WorkshopDisplay(id("refining",holder.id()),WorkshopCategory.REFINING,listOf(inputs,list(Items.CHARCOAL,ThermalRules.coalCost(recipe.cookingTime))),recipe.getResultItem(registry).copy(),emptyList(),"Smelt ore normally, then heat the ingots at a hearth.")
        }
        for(holder in level.recipeManager.getAllRecipesFor(AlloyRecipes.TYPE)) {
            val recipe=holder.value();val advanced=recipe.requiresAdvanced
            out+=WorkshopDisplay(id("alloying",holder.id()),WorkshopCategory.ALLOYING,listOf(recipe.primary.items.map {it.copyWithCount(recipe.primaryCount)},recipe.additive.items.map {it.copyWithCount(recipe.additiveCount)},list(Items.CHARCOAL,ThermalRules.coalCost(recipe.cookingTime))),recipe.result.copy(),listOf(ItemStack(TemperContent.item(if(advanced)"nether_alloy_forge"else"alloy_forge"))),if(advanced)"Requires the Nether Alloy Forge."else"Works in either forge.","Charcoal shown as fuel example; any furnace fuel works. Carbon for steel is separate.")
        }
        for(plan in WorkshopPlans.forming(level)) {
            val result=plan.recipe.getResultItem(registry).copy()
            val steel=plan.id.path.startsWith("steel_core/")
            val shown=if(steel)WorkshopVariants.steel(result)else result
            out+=WorkshopDisplay(id("blueprint",plan.id),WorkshopCategory.BLUEPRINTS,listOf(list(TemperContent.item("blank_blueprint"))),Blueprints.make(plan.id.toString(),shown.hoverName),emptyList(),"Choose pattern; press B to scribe.","Smithing mastery "+TemperAndTang.config.blueprintMastery.get()+" required. Reusable.")
            val inputs=plan.recipe.ingredients.map {it.items.flatMap {item->WorkshopVariants.alternatives(item.copy())}}.toMutableList()
            val base=plan.recipe as ShapingRecipe
            val grid=MutableList<List<ItemStack>>(9){emptyList()};val offset=(3-base.width)/2
            for(y in 0 until base.height)for(x in 0 until base.width)grid[y*3+x+offset]=inputs.getOrElse(y*base.width+x){emptyList()}
            val catalysts=mutableListOf(ItemStack(TemperContent.item("smithing_hammer")))
            if(plan.requiresBlueprint)catalysts+=Blueprints.make(plan.id.toString(),shown.hoverName)
            if(!plan.polishing)grid+=list(Items.WATER_BUCKET)
            out+=WorkshopDisplay(id("forging",plan.id),WorkshopCategory.FORGING,grid,shown,catalysts,if(plan.polishing)"${plan.required()} strikes; ${((plan.recipe as PrecisionRefiningRecipe).minimumAccuracy*100).toInt()}% accuracy."else"Heat → strike → water quench.",if(plan.polishing)(plan.recipe as PrecisionRefiningRecipe).failure.let {"Failure recovers ${it.count} × ${it.hoverName.string}. Alloy ingredients are spent."}else if(steel)"Steel core; original weapon identity."else"Quality is rolled from timing and mastery.",true)
        }
        val supports=BuiltInRegistries.ITEM.map {ItemStack(it)}.filter(GearSupport::supported)+WorkshopVariants.examples()
        for(gear in supports)for(finish in Finish.entries) {
            val preview=WeaponFinishes.preview(gear,finish)
            if(preview.isEmpty)continue
            val material=TemperContent.item(when(finish){Finish.TEMPERED->"tempering_compound";Finish.KEEN->"honing_compound";Finish.WEIGHTED->"balance_weight";Finish.LIGHTENED->"lightweight_grip"})
            out+=WorkshopDisplay(id("finishing_"+finish.key+(if(WorkshopVariants.key(gear)=="steel_core")"_steel_core"else""),BuiltInRegistries.ITEM.getKey(gear.item)),WorkshopCategory.FINISHING,listOf(listOf(gear),list(material)),preview,listOf(ItemStack(TemperContent.item("smithing_hammer"))),"Strike, then pay "+TemperAndTang.config.experienceLevels.get()+" levels.","One treatment; quality and wear are preserved.")
        }
        for(holder in level.recipeManager.getAllRecipesFor(RecipeType.SMITHING)) {
            val recipe=holder.value() as? SteelUpgradeRecipe?:continue
            val native=recipe.base as SmithingIngredientsAccess
            out+=WorkshopDisplay(id("upgrading",holder.id()),WorkshopCategory.UPGRADING,listOf(native.temperTemplate().items.toList(),native.temperBase().items.map {WorkshopVariants.steel(it)},native.temperAddition().items.toList()),recipe.getResultItem(registry).copy(),emptyList(),"Requires a steel-forged weapon.","Keeps quality, name, enchantments and wear.")
        }
        return out
    }
}
