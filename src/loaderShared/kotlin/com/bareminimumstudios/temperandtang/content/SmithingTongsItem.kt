package com.bareminimumstudios.temperandtang.content

import com.bareminimumstudios.temperandtang.TemperAndTang
import net.minecraft.ChatFormatting
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag

class SmithingTongsItem(properties: Properties) : Item(properties) {
    override fun appendHoverText(stack: ItemStack, context: TooltipContext, tooltip: MutableList<Component>, flag: TooltipFlag) {
        super.appendHoverText(stack,context,tooltip,flag)
        tooltip.add(Component.translatable("temperandtang.tooltip.tongs").withStyle(ChatFormatting.GRAY))
        tooltip.add(Component.translatable("temperandtang.tooltip.tongs_wear",TemperAndTang.config.tongsWearTicks.get()/20).withStyle(ChatFormatting.GRAY))
    }
    override fun isValidRepairItem(tool: ItemStack, material: ItemStack): Boolean = material.`is`(IRON)
    companion object {
        private val IRON=TagKey.create(Registries.ITEM,ResourceLocation.fromNamespaceAndPath("c","ingots/iron"))
    }
}
