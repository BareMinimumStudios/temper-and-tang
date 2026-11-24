package com.bareminimumstudios.temperandtang.compat.viewer

import com.bareminimumstudios.temperandtang.TemperAndTang
import com.bareminimumstudios.temperandtang.content.TemperContent
import com.bareminimumstudios.temperandtang.smithing.*
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.*
import net.minecraft.world.item.crafting.*
import net.minecraft.world.level.Level

enum class WorkshopCategory(val station:String) { REFINING("charcoal_hearth"),ALLOYING("charcoal_hearth"),FORGING("shaping_anvil"),FINISHING("finishing_anvil"),UPGRADING("minecraft:smithing_table");
    val key get()=name.lowercase(java.util.Locale.ROOT)
    fun icon()=if(station.contains(':'))ItemStack(Items.SMITHING_TABLE)else ItemStack(TemperContent.item(station))
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
            out+=WorkshopDisplay(id("refining",holder.id()),WorkshopCategory.REFINING,listOf(inputs,list(Items.CHARCOAL,ThermalRules.coalCost(recipe.cookingTime))),recipe.getResultItem(registry).copy(),emptyList(),"Refine ore; take hot metal.")
        }
        for(holder in level.recipeManager.getAllRecipesFor(AlloyRecipes.TYPE)) {
            val recipe=holder.value();val advanced=recipe.requiresAdvanced
            out+=WorkshopDisplay(id("alloying",holder.id()),WorkshopCategory.ALLOYING,listOf(recipe.primary.items.map {it.copyWithCount(recipe.primaryCount)},recipe.additive.items.map {it.copyWithCount(recipe.additiveCount)},list(Items.CHARCOAL,ThermalRules.coalCost(recipe.cookingTime))),recipe.result.copy(),listOf(ItemStack(TemperContent.item(if(advanced)"alloy_furnace"else"charcoal_hearth"))),if(advanced)"Nether furnace required."else"Alloy in a hearth or furnace.","Fuel shown for one complete batch.")
        }
        for(plan in WorkshopPlans.forming(level)) {
            val result=plan.recipe.getResultItem(registry).copy()
            val steel=plan.id.path.startsWith("steel_core/")
            val shown=if(steel)CraftMaterials.stamp(result,ForgeMaterial.STEEL)else result
            val inputs=plan.recipe.ingredients.map {it.items.map(ItemStack::copy)}.toMutableList()
            val base=plan.recipe as ShapingRecipe
            val grid=MutableList<List<ItemStack>>(9){emptyList()};val offset=(3-base.width)/2
            for(y in 0 until base.height)for(x in 0 until base.width)grid[y*3+x+offset]=inputs.getOrElse(y*base.width+x){emptyList()}
            val catalysts=mutableListOf(ItemStack(TemperContent.item("smithing_hammer")))
            if(plan.requiresBlueprint)catalysts+=Blueprints.make(plan.id.toString(),shown.hoverName)
            if(!plan.polishing)grid+=list(Items.WATER_BUCKET)
            out+=WorkshopDisplay(id("forging",plan.id),WorkshopCategory.FORGING,grid,shown,catalysts,if(plan.polishing)"${plan.required()} strikes; ${((plan.recipe as PrecisionRefiningRecipe).minimumAccuracy*100).toInt()}% accuracy."else"Heat → strike → water quench.",if(plan.polishing)"Failure returns 4 scraps; gold is spent."else if(steel)"Steel core; original weapon identity."else"Quality is rolled from timing and mastery.",true)
        }
        val supports=BuiltInRegistries.ITEM.map {ItemStack(it)}.filter(GearSupport::supported)
        for(gear in supports)for(finish in Finish.entries) {
            val preview=WeaponFinishes.preview(gear,finish)
            if(preview.isEmpty)continue
            val material=TemperContent.item(when(finish){Finish.TEMPERED->"tempering_compound";Finish.KEEN->"honing_compound";Finish.WEIGHTED->"balance_weight";Finish.LIGHTENED->"lightweight_grip"})
            out+=WorkshopDisplay(id("finishing_"+finish.key,BuiltInRegistries.ITEM.getKey(gear.item)),WorkshopCategory.FINISHING,listOf(listOf(gear),list(material)),preview,listOf(ItemStack(TemperContent.item("smithing_hammer"))),"Strike, then pay "+TemperAndTang.config.experienceLevels.get()+" levels.","One treatment; quality and wear are preserved.")
        }
        for(holder in level.recipeManager.getAllRecipesFor(RecipeType.SMITHING)) {
            val recipe=holder.value() as? SteelUpgradeRecipe?:continue
            val native=recipe.base as com.bareminimumstudios.temperandtang.mixin.SmithingIngredientsAccess
            out+=WorkshopDisplay(id("upgrading",holder.id()),WorkshopCategory.UPGRADING,listOf(native.temperTemplate().items.toList(),native.temperBase().items.map {CraftMaterials.stamp(it,ForgeMaterial.STEEL)},native.temperAddition().items.toList()),recipe.getResultItem(registry).copy(),emptyList(),"Requires a steel-forged weapon.","Keeps quality, name, enchantments and wear.")
        }
        return out
    }
}
