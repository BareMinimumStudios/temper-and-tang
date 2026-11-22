package com.bareminimumstudios.temperandtang.content

import com.bareminimumstudios.temperandtang.TemperAndTang
import com.bareminimumstudios.temperandtang.smithing.Finish
import com.bareminimumstudios.temperandtang.smithing.FinishRules
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import java.util.Locale

class FinishingMaterialItem(private val finish: Finish, properties: Properties) : Item(properties) {
    override fun appendHoverText(stack: ItemStack, context: TooltipContext, tooltip: MutableList<Component>, flag: TooltipFlag) {
        super.appendHoverText(stack, context, tooltip, flag)
        val effect = FinishRules.effect(finish, TemperAndTang.config.balance())
        for ((key, amount) in listOf("damage" to effect.damage, "speed" to effect.speed, "durability" to effect.durability)) {
            if (amount == 0.0) continue
            tooltip.add(Component.translatable("temperandtang.tooltip." + key, String.format(Locale.ROOT, "%+.0f%%", amount * 100.0))
                .withStyle(if (amount > 0) ChatFormatting.GREEN else ChatFormatting.RED))
        }
        tooltip.add(Component.translatable("temperandtang.tooltip.material").withStyle(ChatFormatting.GRAY))
    }
}
class SmithingHammerItem(properties: Properties) : Item(properties) {
    override fun appendHoverText(stack: ItemStack, context: TooltipContext, tooltip: MutableList<Component>, flag: TooltipFlag) {
        super.appendHoverText(stack, context, tooltip, flag)
        tooltip.add(Component.translatable("temperandtang.tooltip.hammer").withStyle(ChatFormatting.GRAY))
    }
}
