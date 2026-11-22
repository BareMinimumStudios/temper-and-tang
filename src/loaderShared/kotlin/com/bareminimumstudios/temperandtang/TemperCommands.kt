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
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.CustomData

object TemperCommands {
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(Commands.literal("temperandtang").requires { it.hasPermission(2) }
            .then(Commands.literal("verify_station").executes { context ->
                StationVerification.start(context.source)
                1
            })
            .then(Commands.literal("verify").executes { context ->
                try {
                    verify(context.source)
                    ForgingVerification.verify(context.source)
                    RecipeVerification.verify(context.source)
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
    private fun verify(source: CommandSourceStack) {
        check(Diegesis.stationType(TemperAndTang.id("finishing_anvil")).isPresent) { "Station not registered" }
        (TemperContent.items.keys + TemperContent.blocks.keys).forEach { check(TemperContent.item(it) != Items.AIR) { "Missing item $it" } }
        val original = ItemStack(Items.IRON_SWORD)
        original.damageValue = 10
        original.enchant(source.server.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.SHARPNESS), 2)
        original.set(DataComponents.CUSTOM_NAME, Component.literal("Preserved name"))
        val foreign = CompoundTag().also { it.putString("other_mod", "preserved") }
        original.set(DataComponents.CUSTOM_DATA, CustomData.of(foreign))
        for (finish in Finish.entries) {
            val result = WeaponFinishes.preview(original, finish)
            check(!result.isEmpty) { "No result for $finish" }
            check(result.damageValue == original.damageValue) { "Finishing repaired damage" }
            check(result.get(DataComponents.CUSTOM_NAME) == original.get(DataComponents.CUSTOM_NAME)) { "Custom name lost" }
            check(result.get(DataComponents.CUSTOM_DATA)?.copyTag()?.getString("other_mod") == "preserved") { "Foreign data lost" }
            check(result.get(DataComponents.ENCHANTMENTS) == original.get(DataComponents.ENCHANTMENTS)) { "Enchantment lost" }
            check(result.get(DataComponents.MAX_DAMAGE) == FinishRules.maximumDurability(original.getOrDefault(DataComponents.MAX_DAMAGE, original.maxDamage), FinishRules.effect(finish, TemperAndTang.config.balance())))
            val ops = source.server.registryAccess().createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE)
            val saved = ItemStack.CODEC.encodeStart(ops, result).getOrThrow()
            val restored = ItemStack.CODEC.parse(ops, saved).getOrThrow()
            // Codecs normalize default components; compare the persisted representation.
            val resaved = ItemStack.CODEC.encodeStart(ops, restored).getOrThrow()
            check(saved == resaved) { "Finish failed persistence roundtrip" }
            check(WeaponFinishes.preview(result, finish).isEmpty) { "Same finish applied twice" }
        }
        check(original.get(DataComponents.CUSTOM_DATA)?.copyTag()?.contains("temperandtang") == false) { "Preview changed original" }
        check(original.damageValue == 10)
        for (metal in listOf("tin", "bronze")) {
            check(ItemStack(TemperContent.item(metal + "_ingot")).`is`(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "ingots/" + metal)))) { "Missing common material tag" }
        }
        if (TemperAndTang.modLoaded("simplyswords") && TemperAndTang.config.simplySwords.get()) {
            val swordId = ResourceLocation.fromNamespaceAndPath("simplyswords", "iron_longsword")
            val sword = ItemStack(BuiltInRegistries.ITEM.get(swordId))
            check(WeaponFinishes.accepts(sword)) { "Simply Swords weapon not accepted" }
            if (TemperAndTang.modLoaded("overgeared")) {
                @Suppress("UNCHECKED_CAST")
                val type = BuiltInRegistries.DATA_COMPONENT_TYPE.get(ResourceLocation.fromNamespaceAndPath("overgeared", "forging_quality")) as? DataComponentType<Any>
                check(type != null) { "Overgeared quality component absent" }
                val quality = Class.forName("net.stirdrem.overgeared.ForgingQuality").enumConstants.first { (it as Enum<*>).name == "EXPERT" }
                sword.set(type, quality)
                val raw = sword.getOrDefault(DataComponents.MAX_DAMAGE, sword.maxDamage)
                val finished = WeaponFinishes.preview(sword, Finish.TEMPERED)
                check(!finished.isEmpty && finished.get(type) == quality) { "Native forging quality lost" }
                check(finished.get(DataComponents.MAX_DAMAGE) == FinishRules.maximumDurability(raw, FinishRules.effect(Finish.TEMPERED, TemperAndTang.config.balance()))) { "Native durability multiplier applied twice" }
                val recipe = source.server.recipeManager.byKey(swordId).orElseThrow()
                check(BuiltInRegistries.RECIPE_SERIALIZER.getKey(recipe.value().serializer).toString() == "overgeared:forging") { "Basic Simply Swords recipe did not become native forging" }
                TemperAndTang.logger.info("TEMPER_COMPAT_VERIFY_PASSED: Simply Swords acceptance, Overgeared quality preservation and native forging recipe")
            }
        }
    }
}
