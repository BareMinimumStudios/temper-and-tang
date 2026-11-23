package com.bareminimumstudios.temperandtang.smithing

import com.bareminimumstudios.temperandtang.TemperAndTang
import com.bareminimumstudios.temperandtang.api.TemperAndTangApi
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import java.util.WeakHashMap

/** Owned heat is processed once per player, including the cursor, rather than once per hot stack. */
object HotMetalHandling {
    private class Exposure { var burn=Long.MIN_VALUE; var wear=Long.MIN_VALUE }
    private val exposures=WeakHashMap<Player,Exposure>()
    fun isTongs(stack:ItemStack)=!stack.isEmpty && stack.`is`(TemperAndTangApi.TONGS)
    fun tick(server:MinecraftServer) {
        server.playerList.players.forEach { if(it.level().gameTime%10L==0L) process(it) }
    }
    fun process(player:Player) {
        val level=player.level()
        if(level.isClientSide) return
        val now=level.gameTime
        var exposed=false
        fun normalize(stack:ItemStack):ItemStack {
            if(stack.isEmpty) return stack
            val until=ThermalState.data(stack).getLong("hot_until")
            if(until<=0) return stack
            if(now>=until) return ThermalState.airCooled(stack,level)
            exposed=true
            return stack
        }
        for(i in 0 until player.inventory.containerSize) {
            val before=player.inventory.getItem(i)
            val after=normalize(before)
            if(after!==before) player.inventory.setItem(i,after)
        }
        val cursor=player.inventoryMenu.carried
        val cooled=normalize(cursor)
        if(cooled!==cursor) player.inventoryMenu.setCarried(cooled)
        if(!exposed || player.isCreative || player.isSpectator || !player.isAlive ||
            !TemperAndTang.config.thermalEnabled.get() || !TemperAndTang.config.hotMetalHandling.get()) return
        val exposure=exposures.getOrPut(player) { Exposure() }
        val hand=when {
            isTongs(player.mainHandItem)->EquipmentSlot.MAINHAND
            isTongs(player.offhandItem)->EquipmentSlot.OFFHAND
            else->null
        }
        if(hand!=null) {
            if(HeatHandlingRules.due(now,exposure.wear,TemperAndTang.config.tongsWearTicks.get())) {
                exposure.wear=now
                player.getItemBySlot(hand).hurtAndBreak(1,player,hand)
            }
        } else if(!player.hasEffect(MobEffects.FIRE_RESISTANCE) && HeatHandlingRules.due(now,exposure.burn,TemperAndTang.config.burnIntervalTicks.get())) {
            exposure.burn=now
            val damage=TemperAndTang.config.hotMetalDamage.get().toFloat()
            if(damage>0f) player.hurt(player.damageSources().hotFloor(),damage)
            player.displayClientMessage(Component.translatable("temperandtang.thermal.burn_warning"),true)
        }
    }
    fun clear() { exposures.clear() }
}
