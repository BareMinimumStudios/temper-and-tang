package com.bareminimumstudios.temperandtang
import net.minecraft.commands.CommandSourceStack
import net.minecraft.server.MinecraftServer
object StationVerification {
    fun start(source:CommandSourceStack)=CraftVerification.startFinishing(source)
    fun tick(server:MinecraftServer)=CraftVerification.tick(server)
    fun stop(server:MinecraftServer)=CraftVerification.stop(server)
}
