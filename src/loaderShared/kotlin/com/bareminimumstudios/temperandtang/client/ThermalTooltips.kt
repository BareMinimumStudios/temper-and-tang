package com.bareminimumstudios.temperandtang.client
import com.bareminimumstudios.temperandtang.smithing.ThermalState
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.ChatFormatting
object ThermalTooltips {
    fun append(stack:ItemStack,lines:MutableList<Component>) {
        val level=Minecraft.getInstance().level?:return
        if(ThermalState.hot(stack,level)) {
            val until=ThermalState.data(stack).getLong("hot_until")
            val seconds=if(until>0) ((until-level.gameTime+19)/20).coerceAtLeast(0) else 0
            lines.add((if(until>0) Component.translatable("temperandtang.thermal.tooltip.hot",seconds) else Component.translatable("temperandtang.thermal.tooltip.native_hot")).withStyle(ChatFormatting.GOLD))
            lines.add(Component.translatable("temperandtang.thermal.tooltip.handle").withStyle(ChatFormatting.GRAY))
        } else if(ThermalState.blank(stack)) lines.add(Component.translatable("temperandtang.thermal.tooltip.blank").withStyle(ChatFormatting.GRAY))
        if(ThermalState.blank(stack)) lines.add(Component.translatable("temperandtang.thermal.tooltip.quench").withStyle(ChatFormatting.GRAY))
    }
}
