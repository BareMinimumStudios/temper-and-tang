package com.bareminimumstudios.temperandtang.compat.viewer

import com.bareminimumstudios.temperandtang.TemperAndTang
import com.bareminimumstudios.temperandtang.smithing.Blueprints
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
        for(example in WorkshopVariants.examples()) {
            registry.setDefaultComparison(example.item) {previous->Comparison.of({left,right->previous.compare(left,right) && WorkshopVariants.key(left.itemStack)==WorkshopVariants.key(right.itemStack)},{stack->31*previous.getHash(stack)+WorkshopVariants.key(stack.itemStack).hashCode()})}
            registry.addEmiStack(EmiStack.of(example))
            registry.addAlias(EmiStack.of(example),Component.literal("Steel-forged ").append(example.hoverName))
        }
        registry.setDefaultComparison(com.bareminimumstudios.temperandtang.content.TemperContent.item("smithing_blueprint"),Comparison.compareData {Blueprints.pattern(it.itemStack)})
        val categories=WorkshopCategory.entries.associateWith {EmiRecipeCategory(TemperAndTang.id(it.key),EmiStack.of(it.icon()))}
        categories.forEach {(kind,category)->registry.addCategory(category);registry.addWorkstation(category,EmiStack.of(kind.icon()))}
        registry.addWorkstation(categories.getValue(WorkshopCategory.ALLOYING),EmiStack.of(net.minecraft.world.item.ItemStack(com.bareminimumstudios.temperandtang.content.TemperContent.item("alloy_forge"))))
        registry.addWorkstation(categories.getValue(WorkshopCategory.ALLOYING),EmiStack.of(net.minecraft.world.item.ItemStack(com.bareminimumstudios.temperandtang.content.TemperContent.item("nether_alloy_forge"))))
        for(display in WorkshopDisplays.collect(level))registry.addRecipe(DisplayRecipe(display,categories.getValue(display.category)))
        TemperAndTang.logger.info("EMI_WORKSHOP_REGISTERED: synchronized workshop recipes and native weapon upgrades")
    }
    private class DisplayRecipe(val display:WorkshopDisplay,val kind:EmiRecipeCategory):EmiRecipe {
        private fun ingredientStack(stack:net.minecraft.world.item.ItemStack)=EmiStack.of(stack).also {
            if(stack.item==net.minecraft.world.item.Items.WATER_BUCKET)it.setRemainder(EmiStack.of(net.minecraft.world.item.Items.BUCKET))
        }
        override fun getCategory()=kind
        override fun getId()=display.id
        override fun getInputs()=display.inputs.filter {it.isNotEmpty()}.map {EmiIngredient.of(it.map(::ingredientStack))}
        override fun getOutputs()=listOf(EmiStack.of(display.output))
        override fun getCatalysts()=display.catalysts.map(EmiStack::of)
        override fun getDisplayWidth()=176
        override fun getDisplayHeight()=112
        override fun supportsRecipeTree()=display.category !in setOf(WorkshopCategory.FINISHING,WorkshopCategory.BLUEPRINTS)
        override fun addWidgets(widgets:WidgetHolder) {
            display.inputs.forEachIndexed {i,items->if(items.isNotEmpty()){val (x,y)=display.location(i);widgets.addSlot(EmiIngredient.of(items.map(::ingredientStack)),x,y)}}
            display.catalysts.forEachIndexed {i,item->widgets.addSlot(EmiStack.of(item),4+i*20,72).catalyst(true)}
            widgets.addDrawable(0, 0, 176, 112) { graphics, _, _, _ -> WorkshopProgress.draw(graphics, display.category) }
            widgets.addSlot(EmiStack.of(display.output),142,34).large(true).recipeContext(this)
            Minecraft.getInstance().font.split(Component.literal(display.note),168).take(2).forEachIndexed {i,line->widgets.addText(line,4,94+i*9,0x35543B,false)}
            if(display.detail.isNotEmpty())widgets.addTooltipText(listOf(Component.literal(display.detail)),0,92,176,20)
        }
    }
}
