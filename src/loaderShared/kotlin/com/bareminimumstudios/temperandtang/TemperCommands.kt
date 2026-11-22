package com.bareminimumstudios.temperandtang

import com.bareminimumstudios.temperandtang.content.TemperContent
import com.bareminimumstudios.temperandtang.smithing.Finish
import com.bareminimumstudios.temperandtang.smithing.FinishRules
import com.bareminimumstudios.temperandtang.smithing.WeaponFinishes
import com.mojang.brigadier.CommandDispatcher
import com.pockyl.diegesis.api.Diegesis
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.CustomData

object TemperCommands {
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(Commands.literal("temperandtang").requires { it.hasPermission(2) }
            .then(Commands.literal("verify").executes { context ->
                try {
                    verify()
                    context.source.sendSuccess({ Component.literal("Temper & Tang verification passed: station, materials, finishes, preservation and non-stacking.") }, false)
                    TemperAndTang.logger.info("TEMPER_VERIFY_PASSED")
                    1
                } catch (failure: Exception) {
                    context.source.sendFailure(Component.literal("Temper & Tang verification failed: " + failure.message))
                    TemperAndTang.logger.error("TEMPER_VERIFY_FAILED", failure)
                    0
                }
            }))
    }
    private fun verify() {
        check(Diegesis.stationType(TemperAndTang.id("finishing_anvil")).isPresent) { "Station not registered" }
        (TemperContent.items.keys + TemperContent.blocks.keys).forEach { check(TemperContent.item(it) != Items.AIR) { "Missing item $it" } }
        val original = ItemStack(Items.IRON_SWORD)
        original.damageValue = 10
        original.set(DataComponents.CUSTOM_NAME, Component.literal("Preserved name"))
        val foreign = CompoundTag().also { it.putString("other_mod", "preserved") }
        original.set(DataComponents.CUSTOM_DATA, CustomData.of(foreign))
        for (finish in Finish.entries) {
            val result = WeaponFinishes.preview(original, finish)
            check(!result.isEmpty) { "No result for $finish" }
            check(result.damageValue == original.damageValue) { "Finishing repaired damage" }
            check(result.get(DataComponents.CUSTOM_NAME) == original.get(DataComponents.CUSTOM_NAME)) { "Custom name lost" }
            check(result.get(DataComponents.CUSTOM_DATA)?.copyTag()?.getString("other_mod") == "preserved") { "Foreign data lost" }
            check(result.maxDamage == FinishRules.maximumDurability(original.maxDamage, FinishRules.effect(finish, TemperAndTang.config.balance())))
            check(WeaponFinishes.preview(result, finish).isEmpty) { "Same finish applied twice" }
        }
        check(original.get(DataComponents.CUSTOM_DATA)?.copyTag()?.contains("temperandtang") == false) { "Preview changed original" }
        check(original.damageValue == 10)
    }
}
