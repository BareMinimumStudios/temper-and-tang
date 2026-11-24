package com.bareminimumstudios.temperandtang.station

import com.bareminimumstudios.temperandtang.mixin.PlayerListSaveAccess
import com.pockyl.diegesis.api.station.StationContext
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack

/** Return owned session items before library fallback, after station-specific cancellation settles its costs. */
object WorkshopRecovery {
    private val active=java.util.WeakHashMap<ServerPlayer,Unit>()
    fun opened(station:StationContext,player:Player) {
        if(!station.isClientSide && player is ServerPlayer)active[player]=Unit
    }
    fun stopping(server:net.minecraft.server.MinecraftServer) {
        for(player in active.keys.filter {it.server===server}.toList())
            com.pockyl.diegesis.impl.server.StationManager.get(server).close(player,false)
    }
    fun clear(server:net.minecraft.server.MinecraftServer) {active.keys.removeIf {it.server===server}}
    fun closed(station:StationContext,player:Player) {
        val owner=player as? ServerPlayer?:return
        active.remove(owner)
        if(station.isClientSide || owner.isDeadOrDying)return
        val items=mutableListOf<ItemStack>()
        val cursor=owner.inventoryMenu.carried
        if(!cursor.isEmpty){items.add(cursor.copy());owner.inventoryMenu.carried=ItemStack.EMPTY}
        for(group in station.type().schema().slotGroups().values) {
            if(!group.rules().returnOnClose())continue
            for(i in 0 until group.size()) {
                val ref=group.ref(i);val stack=station.state().getItem(ref)
                if(!stack.isEmpty){items.add(stack.copy());station.state().setItem(ref,ItemStack.EMPTY)}
            }
        }
        if(items.isEmpty())return
        for(stack in items) {
            owner.inventory.add(stack)
            if(!stack.isEmpty) {
                val pos=station.pos()
                val overflow=ItemEntity(station.level(),pos.x+.5,pos.y+1.0,pos.z+.5,stack.copy())
                overflow.setTarget(owner.uuid);overflow.setUnlimitedLifetime();overflow.setDefaultPickUpDelay()
                station.level().addFreshEntity(overflow)
            }
        }
        // Logout callbacks may follow vanilla's normal save; persist the returned inventory explicitly.
        if(owner.hasDisconnected())(owner.server.playerList as PlayerListSaveAccess).temperSaveWorkshopOwner(owner)
    }
}
