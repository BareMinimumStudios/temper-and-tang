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
        val summary=Component.empty()
        for((key,amount) in listOf("damage" to effect.damage,"speed" to effect.speed,"durability" to effect.durability)) {
            if(amount==0.0)continue
            if(summary.siblings.isNotEmpty())summary.append(Component.literal(", "))
            summary.append(Component.translatable("temperandtang.tooltip."+key,Component.literal(String.format(Locale.ROOT,"%+.1f",amount*100).removeSuffix(".0")+"%").withStyle {it.withColor(if(amount>0)0x91C88E else 0xD28A7E)}))
        }
        tooltip.add(summary.withStyle(ChatFormatting.GRAY))
        if(finish==Finish.LIGHTENED)tooltip.add(Component.translatable("temperandtang.short.armor_bonus",Component.literal(String.format(Locale.ROOT,"%+.1f%%",TemperAndTang.config.lightenedArmorSpeed.get()*100)).withStyle {it.withColor(0x91C88E)}).withStyle(ChatFormatting.GRAY))
        tooltip.add(Component.translatable("temperandtang.short.treatment_"+finish.key).withStyle(ChatFormatting.DARK_GRAY))
    }
}
class SmithingHammerItem(properties: Properties) : Item(properties) {
    override fun appendHoverText(stack: ItemStack, context: TooltipContext, tooltip: MutableList<Component>, flag: TooltipFlag) {
        super.appendHoverText(stack, context, tooltip, flag)
        tooltip.add(Component.translatable("temperandtang.tooltip.hammer",Component.literal("SPACE").withStyle {it.withColor(0xD7B16A)}).withStyle(ChatFormatting.GRAY))
    }
}
