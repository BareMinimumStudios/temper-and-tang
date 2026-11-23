package com.bareminimumstudios.temperandtang

import com.bareminimumstudios.temperandtang.content.TemperContent
import com.bareminimumstudios.temperandtang.station.FinishingStation
import com.mojang.authlib.GameProfile
import com.mojang.serialization.JsonOps
import com.pockyl.diegesis.api.Diegesis
import com.pockyl.diegesis.api.state.StationState
import com.pockyl.diegesis.api.station.StationContext
import com.pockyl.diegesis.impl.action.ActionHandler
import com.pockyl.diegesis.impl.action.StationAction
import net.minecraft.commands.CommandSourceStack
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import java.util.UUID

/** Operator-invoked fixtures: no real player inventory, blocks or world time are changed. */
object StationVerification {
    private data class Job(val source: CommandSourceStack, val step: () -> Boolean)
    private val jobs = mutableMapOf<MinecraftServer, Job>()
    private class Fixture(val source: CommandSourceStack) {
        val type = Diegesis.stationType(TemperAndTang.id("finishing_anvil")).orElseThrow()
        var state = StationState(type.schema())
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
        var logic = type.logic().create(context)
        fun reset(weapon: ItemStack = ItemStack(Items.IRON_SWORD), hammer: ItemStack = ItemStack(TemperContent.item("smithing_hammer"))) {
            state = StationState(type.schema())
            logic = type.logic().create(context)
            player.inventory.clearContent()
            player.inventoryMenu.setCarried(ItemStack.EMPTY)
            player.experienceLevel = TemperAndTang.config.experienceLevels.get() + 3
            state.setItem(FinishingStation.WEAPON, weapon.copy())
            state.setItem(FinishingStation.MATERIAL, ItemStack(TemperContent.item("tempering_compound"), 2))
            state.setItem(FinishingStation.HAMMER, hammer.copy())
            logic.onSlotChanged(context, FinishingStation.WEAPON)
            logic.updateOutputs(context)
        }
        fun press(keyboard: Boolean) {
            val action = if (keyboard) StationAction.Custom("strike", CompoundTag()) else StationAction.Press("strike")
            check(ActionHandler.apply(context, logic, player, action))
        }
        fun take() = logic.takeOutput(context, player, "result")
        fun inventoryCount(item: net.minecraft.world.item.Item) = player.inventory.items.filter { it.item == item }.sumOf { it.count }
    }
    fun start(source: CommandSourceStack) {
        check(!jobs.containsKey(source.server)) { "A station verification is already running" }
        val f = Fixture(source)
        f.reset()
        check(f.type.schema().slotGroups()["hammer"]!!.rules().accepts().test(ItemStack(TemperContent.item("smithing_hammer"))))
        check(f.context.blockState().getMenuProvider(source.level, f.context.pos()) == null) { "Finishing station exposes vanilla repair menu" }
        if (TemperAndTang.modLoaded("overgeared")) {
            val nativeHammer = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("overgeared", "smithing_hammer"))
            check(FinishingStation.isHammer(ItemStack(nativeHammer))) { "Native hammer not accepted" }
        }
        val required = TemperAndTang.config.requiredStrikes.get()
        val cost = TemperAndTang.config.experienceLevels.get()
        var phase = 0
        var strikes = 0
        var next = source.level.gameTime
        val cancelledWeapon = ItemStack(Items.IRON_SWORD).also {
            it.damageValue = 7
            it.set(DataComponents.CUSTOM_NAME, Component.literal("Cancellation fixture"))
        }
        val ops = source.server.registryAccess().createSerializationContext(JsonOps.INSTANCE)
        jobs[source.server] = Job(source) {
            if (source.level.gameTime < next) false else {
                f.press(strikes % 2 != 0)
                strikes++
                check(f.state.getControl("progress").toInt() == strikes) { "Progress not synchronized" }
                if (strikes == 1) {
                    f.press(true)
                    check(f.state.getControl("progress").toInt() == 1) { "Same-tick spam bypassed timing" }
                }
                next = source.level.gameTime + TemperAndTang.config.strikeIntervalTicks.get() + 1
                val target = if (phase == 1) minOf(3, required) else required
                if (strikes < target) {
                    check(f.state.getOutput("result").isEmpty) { "Premature output" }
                    false
                } else if (phase == 0) {
                    check(!f.state.getOutput("result").isEmpty) { "No completed output" }
                    val completed = f.take()
                    check(!completed.isEmpty && f.player.experienceLevel == 3)
                    check(f.state.getItem(FinishingStation.WEAPON).isEmpty)
                    check(f.state.getItem(FinishingStation.MATERIAL).count == 1)
                    check(f.state.getItem(FinishingStation.HAMMER).damageValue == required)
                    check(f.take().isEmpty) { "Duplicate output" }
                    f.logic.updateOutputs(f.context)
                    check(f.state.getOutput("result").isEmpty)

                    f.reset(completed)
                    f.press(false)
                    check(f.state.getControl("progress") == 0f && f.state.getOutput("result").isEmpty) { "Finished weapon accepted a second finish" }
                    check(f.player.experienceLevel == cost + 3 && f.state.getItem(FinishingStation.MATERIAL).count == 2)
                    check(!ActionHandler.apply(f.context, f.logic, f.player, StationAction.SetControl("progress", 40f))) { "Locked progress accepted a client write" }
                    val worn = ItemStack(TemperContent.item("smithing_hammer"))
                    worn.damageValue = worn.maxDamage - required + 1
                    f.reset(hammer = worn)
                    f.press(false)
                    check(f.state.getControl("progress") == 0f && f.take().isEmpty) { "Worn hammer bypassed work cost" }

                    f.reset(cancelledWeapon)
                    phase = 1
                    strikes = 0
                    false
                } else if (phase == 1) {
                    check(f.player.experienceLevel == cost + 3)
                    check(f.state.getItem(FinishingStation.MATERIAL).count == 2)
                    check(f.state.getItem(FinishingStation.HAMMER).damageValue == 0)
                    ActionHandler.returnItems(f.context, f.logic, f.player, true)
                    check(listOf(FinishingStation.WEAPON, FinishingStation.MATERIAL, FinishingStation.HAMMER).all { f.state.getItem(it).isEmpty })
                    check(f.inventoryCount(Items.IRON_SWORD) == 1)
                    check(f.inventoryCount(TemperContent.item("tempering_compound")) == 2)
                    check(f.inventoryCount(TemperContent.item("smithing_hammer")) == 1)
                    val returned = f.player.inventory.items.first { it.item == Items.IRON_SWORD }
                    check(ItemStack.CODEC.encodeStart(ops, returned).getOrThrow() == ItemStack.CODEC.encodeStart(ops, cancelledWeapon).getOrThrow()) { "Cancellation changed weapon data" }
                    check(f.player.experienceLevel == cost + 3)
                    val nextHammer = if (TemperAndTang.modLoaded("overgeared")) ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("overgeared", "smithing_hammer"))) else ItemStack(TemperContent.item("smithing_hammer"))
                    f.reset(hammer = nextHammer)
                    f.player.experienceLevel = 0
                    phase = 2
                    strikes = 0
                    false
                } else {
                    check(!f.state.getOutput("result").isEmpty)
                    if (cost > 0) {
                        check(f.take().isEmpty) { "Output taken without XP" }
                        check(f.state.getItem(FinishingStation.MATERIAL).count == 2)
                        check(f.state.getItem(FinishingStation.HAMMER).damageValue == 0)
                        check(!f.state.getItem(FinishingStation.WEAPON).isEmpty)
                    }
                    f.player.experienceLevel = cost + 3
                    check(!f.take().isEmpty && f.player.experienceLevel == 3) { "Could not take ready output after gaining XP" }
                    check(f.state.getItem(FinishingStation.HAMMER).damageValue == required) { "Native hammer wear incorrect" }
                    TemperAndTang.logger.info("TEMPER_STATION_VERIFY_PASSED: paced mouse/keyboard work, consumption, spam and progress guards, non-stacking, worn tools, cancellation and XP recovery")
                    true
                }
            }
        }
        source.sendSuccess({ Component.literal("Running isolated finishing station scenarios...") }, false)
    }
    fun tick(server: MinecraftServer) {
        ThermalVerification.tick(server)
        val job = jobs[server] ?: return
        try {
            if (job.step()) {
                jobs.remove(server)
                job.source.sendSuccess({ Component.literal("Finishing station scenarios passed.") }, false)
            }
        } catch (failure: Exception) {
            jobs.remove(server)
            job.source.sendFailure(Component.literal("Finishing station verification failed: " + failure.message))
            TemperAndTang.logger.error("TEMPER_STATION_VERIFY_FAILED", failure)
        }
    }
    fun stop(server: MinecraftServer) { jobs.remove(server); ThermalVerification.stop(server) }
}
