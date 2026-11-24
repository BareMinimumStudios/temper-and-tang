package com.bareminimumstudios.temperandtang.compat.viewer

import com.bareminimumstudios.temperandtang.TemperAndTang
import dev.emi.emi.api.*
import dev.emi.emi.api.recipe.*
import dev.emi.emi.api.stack.*
import dev.emi.emi.api.widget.WidgetHolder
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component

@EmiEntrypoint
class TemperEmiPlugin:EmiPlugin {
    override fun register(registry:EmiRegistry) {
        val level=Minecraft.getInstance().level?:return
        val categories=WorkshopCategory.entries.associateWith {EmiRecipeCategory(TemperAndTang.id(it.key),EmiStack.of(it.icon()))}
        categories.forEach {(kind,category)->registry.addCategory(category);registry.addWorkstation(category,EmiStack.of(kind.icon()))}
        registry.addWorkstation(categories.getValue(WorkshopCategory.ALLOYING),EmiStack.of(net.minecraft.world.item.ItemStack(com.bareminimumstudios.temperandtang.content.TemperContent.item("alloy_furnace"))))
        for(display in WorkshopDisplays.collect(level))registry.addRecipe(DisplayRecipe(display,categories.getValue(display.category)))
        TemperAndTang.logger.info("EMI_WORKSHOP_REGISTERED: synchronized workshop recipes and native weapon upgrades")
    }
    private class DisplayRecipe(val display:WorkshopDisplay,val kind:EmiRecipeCategory):EmiRecipe {
        override fun getCategory()=kind
        override fun getId()=display.id
        override fun getInputs()=display.inputs.filter {it.isNotEmpty()}.map {EmiIngredient.of(it.map(EmiStack::of))}
        override fun getOutputs()=listOf(EmiStack.of(display.output))
        override fun getCatalysts()=display.catalysts.map(EmiStack::of)
        override fun getDisplayWidth()=176
        override fun getDisplayHeight()=112
        override fun supportsRecipeTree()=display.category!=WorkshopCategory.FINISHING
        override fun addWidgets(widgets:WidgetHolder) {
            display.inputs.forEachIndexed {i,items->if(items.isNotEmpty()){val (x,y)=display.location(i);widgets.addSlot(EmiIngredient.of(items.map(EmiStack::of)),x,y)}}
            display.catalysts.forEachIndexed {i,item->widgets.addSlot(EmiStack.of(item),4+i*20,72).catalyst(true)}
            widgets.addText(Component.literal("→"),110,39,0xB8A17A,false)
            widgets.addSlot(EmiStack.of(display.output),142,34).large(true).recipeContext(this)
            Minecraft.getInstance().font.split(Component.literal(display.note),168).take(2).forEachIndexed {i,line->widgets.addText(line,4,94+i*9,0x9EBE9B,false)}
            if(display.detail.isNotEmpty())widgets.addTooltipText(listOf(Component.literal(display.detail)),0,92,176,20)
        }
    }
}
