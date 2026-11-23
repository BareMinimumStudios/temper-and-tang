package com.bareminimumstudios.temperandtang.smithing

import com.bareminimumstudios.temperandtang.TemperAndTang
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.*

object WorkshopPlans {
    private val STOCK=TagKey.create(Registries.ITEM,TemperAndTang.id("heatable_stock"))
    fun stock(stack:ItemStack)=!stack.isEmpty && stack.`is`(STOCK)
    fun metal(stack:ItemStack)=stock(stack) || GearSupport.supported(stack)
    fun basicWeapon(stack:ItemStack)=GearSupport.supported(stack)
    fun heatable(stack:ItemStack)=stock(stack) || ThermalState.blank(stack) || GearSupport.supported(stack)
    data class Heating(val output:ItemStack,val perItem:Int,val nativeRecipe:RecipeHolder<*>?=null)
    fun heating(stack:ItemStack,level:net.minecraft.world.level.Level):Heating? {
        if(!heatable(stack))return null
        val path=BuiltInRegistries.ITEM.getKey(stack.item).path
        val factor=when {"netherite" in path->2.5;"diamond" in path->2.0;"steel" in path->1.5;else->1.0}
        return Heating(stack.copy(),(TemperAndTang.config.heatingTicks.get()*factor).toInt())
    }
    data class Forming(val id:ResourceLocation,val recipe:Recipe<*>,val native:Boolean,val ghosts:List<ItemStack>,val hits:Int,val polishing:Boolean=false) {
        val requiresBlueprint get()=GearSupport.blueprint(recipe.getResultItem(net.minecraft.core.RegistryAccess.EMPTY))
        @Suppress("UNCHECKED_CAST")
        fun matches(grid:List<ItemStack>,level:net.minecraft.world.level.Level)=(recipe as CraftingRecipe).matches(CraftingInput.of(3,3,grid),level)
        fun assemble(grid:List<ItemStack>,level:net.minecraft.world.level.Level):ItemStack {
            val result=(recipe as CraftingRecipe).assemble(CraftingInput.of(3,3,grid),level.registryAccess())
            return GearTransfer.inherit(result,grid.filter(GearSupport::supported).singleOrNull())
        }
        fun remainders(grid:List<ItemStack>)=(recipe as CraftingRecipe).getRemainingItems(CraftingInput.of(3,3,grid)).toList()
        fun current(level:net.minecraft.world.level.Level)=level.recipeManager.byKey(id).orElse(null)?.value()===recipe
        fun hot(grid:List<ItemStack>,level:net.minecraft.world.level.Level)=grid.any(::metal) && grid.filter(::metal).all {ThermalState.hot(it,level)}
        fun required()=maxOf(hits,TemperAndTang.config.shapingStrikes.get()).coerceAtMost(40)
        fun output(grid:List<ItemStack>,level:net.minecraft.world.level.Level,player:Player,clean:Int,accuracy:Double=clean.toDouble()/required()):ItemStack {
            val gear=Craftsmanship.forge(assemble(grid,level),accuracy,player,id.toString(),grid)
            return if(gear.isEmpty)gear else ThermalState.unquenched(gear,level)
        }
    }
    fun forming(level:net.minecraft.world.level.Level):List<Forming> {
        val result=mutableListOf<Forming>()
        for(holder in level.recipeManager.getAllRecipesFor(ThermalRecipes.TYPE)) {
            val recipe=holder.value();val output=recipe.getResultItem(level.registryAccess())
            if(!GearSupport.supported(output))continue
            val ghosts=ghostGrid(recipe.ingredients,recipe.width,recipe.height)?:continue
            if(ghosts.none(::metal))continue
            val id=BuiltInRegistries.ITEM.getKey(output.item).path
            val hits=TemperAndTang.config.shapingStrikes.get()+(if(GearSupport.armor(output))2 else 0)+(if("netherite" in id || "diamond" in id)2 else 0)
            result.add(Forming(holder.id(),recipe,false,ghosts,hits))
        }
        return result.sortedBy {it.id.toString()}
    }
    private fun ghostGrid(ingredients:List<Ingredient>,width:Int,height:Int):List<ItemStack>? {
        if(width !in 1..3 || height !in 1..3 || ingredients.any {!it.isEmpty && it.items.isEmpty()})return null
        val grid=MutableList(9){ItemStack.EMPTY};val offset=(3-width)/2
        for(y in 0 until height)for(x in 0 until width) {
            val ingredient=ingredients.getOrNull(y*width+x)?:continue
            if(!ingredient.isEmpty)grid[y*3+x+offset]=ingredient.items.first().copyWithCount(1)
        }
        return grid
    }
}
