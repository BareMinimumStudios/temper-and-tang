package com.bareminimumstudios.temperandtang.client
import com.bareminimumstudios.temperandtang.smithing.ThermalState
import com.bareminimumstudios.temperandtang.smithing.GearSupport
import net.minecraft.client.Minecraft
import net.minecraft.world.item.ItemStack

object WorkpieceAppearance {
    fun shown(stack:ItemStack):ItemStack {
        if(!ThermalState.blank(stack))return stack
        val level=Minecraft.getInstance().level?:return stack
        val gear=ItemStack.parseOptional(level.registryAccess(),ThermalState.data(stack).getCompound("weapon"))
        return if(!gear.isEmpty && !ThermalState.blank(gear) && GearSupport.supported(gear))gear else stack
    }
}
