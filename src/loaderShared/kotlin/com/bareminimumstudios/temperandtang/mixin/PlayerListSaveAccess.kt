package com.bareminimumstudios.temperandtang.mixin
import net.minecraft.server.players.PlayerList
import net.minecraft.server.level.ServerPlayer
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Invoker
@Mixin(PlayerList::class)
interface PlayerListSaveAccess {
    @Invoker("save") fun temperSaveWorkshopOwner(player:ServerPlayer)
}
