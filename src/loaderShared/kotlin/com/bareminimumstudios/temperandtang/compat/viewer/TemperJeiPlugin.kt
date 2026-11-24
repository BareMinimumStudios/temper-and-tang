package com.bareminimumstudios.temperandtang.compat.viewer

import com.bareminimumstudios.temperandtang.TemperAndTang
import mezz.jei.api.*
import mezz.jei.api.registration.*
import mezz.jei.api.recipe.*
import mezz.jei.api.recipe.category.IRecipeCategory
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder
import mezz.jei.api.gui.ingredient.IRecipeSlotsView
import mezz.jei.api.gui.drawable.IDrawable
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component

@JeiPlugin
class TemperJeiPlugin:IModPlugin {
    companion object {val types=WorkshopCategory.entries.associateWith {RecipeType.create("temperandtang",it.key,WorkshopDisplay::class.java)}}
    override fun getPluginUid()=TemperAndTang.id("jei")
    private fun enabled()=!TemperAndTang.modLoaded("emi")
    override fun registerItemSubtypes(registration:ISubtypeRegistration) {
        if(!enabled())return
        val interpreter=object:mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter<net.minecraft.world.item.ItemStack> {
            override fun getSubtypeData(stack:net.minecraft.world.item.ItemStack,context:mezz.jei.api.ingredients.subtypes.UidContext):Any=WorkshopVariants.key(stack)
            override fun getLegacyStringSubtypeInfo(stack:net.minecraft.world.item.ItemStack,context:mezz.jei.api.ingredients.subtypes.UidContext)=WorkshopVariants.key(stack)
        }
        for(example in WorkshopVariants.examples())registration.registerSubtypeInterpreter(example.item,interpreter)
        registration.registerSubtypeInterpreter(com.bareminimumstudios.temperandtang.content.TemperContent.item("smithing_blueprint"),interpreter)
    }
    override fun registerExtraIngredients(registration:IExtraIngredientRegistration) {if(enabled())registration.addExtraItemStacks(WorkshopVariants.examples())}
    override fun registerCategories(registration:IRecipeCategoryRegistration) {
        if(!enabled())return
        for(kind in WorkshopCategory.entries)registration.addRecipeCategories(Category(kind,registration.jeiHelpers.guiHelper.createDrawableItemStack(kind.icon())))
    }
    override fun registerRecipes(registration:IRecipeRegistration) {
        if(!enabled())return
        val level=Minecraft.getInstance().level?:return
        for((kind,recipes) in WorkshopDisplays.collect(level).groupBy {it.category})registration.addRecipes(types.getValue(kind),recipes)
        registration.addItemStackInfo(net.minecraft.world.item.ItemStack(com.bareminimumstudios.temperandtang.content.TemperContent.item("diamond_upgrade_smithing_template")),Component.literal("Found in Overworld structure chests: strongholds, temples, treasure, mansions and ancient cities; rarer in mineshafts and dungeons."))
        TemperAndTang.logger.info("JEI_WORKSHOP_REGISTERED: synchronized workshop recipes and native weapon upgrades")
    }
    override fun registerRecipeCatalysts(registration:IRecipeCatalystRegistration) {
        if(!enabled())return
        for(kind in WorkshopCategory.entries)registration.addRecipeCatalyst(kind.icon(),types.getValue(kind))
        registration.addRecipeCatalyst(net.minecraft.world.item.ItemStack(com.bareminimumstudios.temperandtang.content.TemperContent.item("alloy_furnace")),types.getValue(WorkshopCategory.ALLOYING))
    }
    private class Category(val kind:WorkshopCategory,val categoryIcon:IDrawable):IRecipeCategory<WorkshopDisplay> {
        override fun getRecipeType()=types.getValue(kind)
        override fun getTitle()=Component.translatable("emi.category.temperandtang."+kind.key)
        override fun getIcon()=categoryIcon
        override fun getWidth()=176
        override fun getHeight()=112
        override fun getRegistryName(recipe:WorkshopDisplay)=recipe.id
        override fun setRecipe(builder:IRecipeLayoutBuilder,recipe:WorkshopDisplay,focuses:IFocusGroup) {
            recipe.inputs.forEachIndexed {i,items->if(items.isNotEmpty()){val (x,y)=recipe.location(i);builder.addSlot(RecipeIngredientRole.INPUT,x+1,y+1).addItemStacks(items).setStandardSlotBackground()}}
            recipe.catalysts.forEachIndexed {i,item->builder.addSlot(RecipeIngredientRole.CATALYST,5+i*20,73).addItemStack(item).setStandardSlotBackground()}
            builder.addSlot(RecipeIngredientRole.OUTPUT,143,35).addItemStack(recipe.output).setOutputSlotBackground()
        }
        override fun draw(recipe:WorkshopDisplay,slots:IRecipeSlotsView,graphics:GuiGraphics,mouseX:Double,mouseY:Double) {
            val font=Minecraft.getInstance().font
            graphics.drawString(font,"→",110,39,0x72532F,false)
            font.split(Component.literal(recipe.note),168).take(2).forEachIndexed {i,line->graphics.drawString(font,line,4,94+i*9,0x35543B,false)}
        }
        override fun getTooltipStrings(recipe:WorkshopDisplay,slots:IRecipeSlotsView,mouseX:Double,mouseY:Double)=if(recipe.detail.isEmpty() || mouseY<90)emptyList()else listOf(Component.literal(recipe.detail))
    }
}
