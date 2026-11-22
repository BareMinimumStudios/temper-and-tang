package com.bareminimumstudios.temperandtang

import com.bareminimumstudios.temperandtang.content.TemperContent
import com.bareminimumstudios.temperandtang.station.FinishingStation
import com.mojang.authlib.GameProfile
import com.pockyl.diegesis.api.Diegesis
import com.pockyl.diegesis.api.state.StationState
import com.pockyl.diegesis.api.station.StationContext
import com.pockyl.diegesis.impl.action.ActionHandler
import com.pockyl.diegesis.impl.action.StationAction
import net.minecraft.commands.CommandSourceStack
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import java.util.UUID

/** Operator-invoked, isolated fixture. No blocks, inventories or world time are changed. */
object StationVerification {
    private data class Job(val source: CommandSourceStack, val step: () -> Boolean)
    private val jobs = mutableMapOf<MinecraftServer, Job>()
    fun start(source: CommandSourceStack) {
        check(!jobs.containsKey(source.server)) { "A station verification is already running" }
        val type = Diegesis.stationType(TemperAndTang.id("finishing_anvil")).orElseThrow()
        val state = StationState(type.schema())
        val player = object : Player(source.level, BlockPos.ZERO, 0f, GameProfile(UUID.randomUUID(), "TemperFixture")) {
            override fun isSpectator() = false
            override fun isCreative() = false
        }
        val context = object : StationContext {
            override fun type() = type
            override fun level() = source.level
            override fun pos() = BlockPos.containing(source.position)
            override fun blockState() = TemperContent.block("finishing_anvil").defaultBlockState()
            override fun facing() = Direction.NORTH
            override fun state() = state
            override fun users() = listOf(player)
            override fun effect(effect: ResourceLocation, node: String?, data: CompoundTag) {}
        }
        val logic = type.logic().create(context)
        state.setItem(FinishingStation.WEAPON, ItemStack(Items.IRON_SWORD))
        state.setItem(FinishingStation.MATERIAL, ItemStack(TemperContent.item("tempering_compound"), 2))
        state.setItem(FinishingStation.HAMMER, ItemStack(TemperContent.item("smithing_hammer")))
        logic.onSlotChanged(context, FinishingStation.WEAPON)
        logic.updateOutputs(context)
        val required = TemperAndTang.config.requiredStrikes.get()
        val cost = TemperAndTang.config.experienceLevels.get()
        player.experienceLevel = cost + 3
        var strikes = 0
        var next = source.level.gameTime
        jobs[source.server] = Job(source) {
            if (source.level.gameTime < next) false else {
                val action = if (strikes % 2 == 0) StationAction.Press("strike") else StationAction.Custom("strike", CompoundTag())
                check(ActionHandler.apply(context, logic, player, action))
                strikes++
                check(state.getControl("progress").toInt() == strikes) { "Progress not synchronized" }
                next = source.level.gameTime + TemperAndTang.config.strikeIntervalTicks.get() + 1
                if (strikes < required) {
                    check(state.getOutput("result").isEmpty) { "Premature output after $strikes strikes" }
                    false
                } else {
                    check(!state.getOutput("result").isEmpty) { "No output after $strikes actual button actions" }
                    val result = logic.takeOutput(context, player, "result")
                    check(!result.isEmpty) { "Output could not be taken" }
                    check(state.getItem(FinishingStation.WEAPON).isEmpty)
                    check(state.getItem(FinishingStation.MATERIAL).count == 1)
                    check(state.getItem(FinishingStation.HAMMER).damageValue == required)
                    check(player.experienceLevel == 3)
                    check(logic.takeOutput(context, player, "result").isEmpty) { "Duplicate output" }
                    logic.updateOutputs(context)
                    check(state.getOutput("result").isEmpty) { "Stale output" }
                    TemperAndTang.logger.info("TEMPER_STATION_VERIFY_PASSED: {} paced Diegesis button/keyboard actions, output, material, XP, hammer and duplicate prevention", strikes)
                    true
                }
            }
        }
        source.sendSuccess({ Component.literal("Running isolated finishing station verification...") }, false)
    }
    fun tick(server: MinecraftServer) {
        val job = jobs[server] ?: return
        try {
            if (job.step()) {
                jobs.remove(server)
                job.source.sendSuccess({ Component.literal("Finishing station verification passed.") }, false)
            }
        } catch (failure: Exception) {
            jobs.remove(server)
            job.source.sendFailure(Component.literal("Finishing station verification failed: " + failure.message))
            TemperAndTang.logger.error("TEMPER_STATION_VERIFY_FAILED", failure)
        }
    }
    fun stop(server: MinecraftServer) { jobs.remove(server) }
}

