package com.bareminimumstudios.temperandtang.station
import com.bareminimumstudios.temperandtang.TemperAndTang
import com.bareminimumstudios.temperandtang.content.TemperContent
import com.bareminimumstudios.temperandtang.smithing.WeaponFinishes
import com.bareminimumstudios.temperandtang.smithing.WorkProgress
import com.pockyl.diegesis.api.Diegesis
import com.pockyl.diegesis.api.state.ControlSpec
import com.pockyl.diegesis.api.state.SlotRef
import com.pockyl.diegesis.api.state.SlotRules
import com.pockyl.diegesis.api.state.StationSchema
import com.pockyl.diegesis.api.station.StationContext
import com.pockyl.diegesis.api.station.StationLogic
import net.minecraft.network.chat.Component
import net.minecraft.nbt.CompoundTag
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

object FinishingStation {
    val WEAPON = SlotRef.of("weapon")
    val MATERIAL = SlotRef.of("material")
    val HAMMER = SlotRef.of("hammer")
    fun register() {
        Diegesis.station(TemperAndTang.id("finishing_anvil"))
            .block { TemperContent.block("finishing_anvil") }
            .schema(StationSchema.builder()
                .slots("weapon", 1, SlotRules.DEFAULT.maxStack(1).accepts(WeaponFinishes::accepts))
                .slots("material", 1, SlotRules.DEFAULT.accepts { WeaponFinishes.ingredient(it) != null })
                .slots("hammer", 1, SlotRules.DEFAULT.maxStack(1).accepts { it.isDamageableItem && it.`is`(TagKey.create(Registries.ITEM, TemperAndTang.id("smithing_hammers"))) })
                .control("strike", ControlSpec.button())
                .control("progress", ControlSpec.range(0f, 40f, 0f).locked())
                .output("result").build())
            .exclusive()
            .openWhen { _, _, _ -> TemperAndTang.config.enabled.get() }
            .logic { Logic() }
            .register()
    }
    private class Logic : StationLogic {
        private val progress = WorkProgress()
        override fun onSlotChanged(station: StationContext, slot: SlotRef) {
            progress.reset()
            station.state().setControl("progress", 0f)
        }
        override fun onCustomAction(station: StationContext, player: Player, action: String, data: CompoundTag) {
            if (action == "strike") onButton(station, player, "strike")
        }
        override fun onButton(station: StationContext, player: Player, control: String) {
            if (control != "strike" || !TemperAndTang.config.enabled.get() || station.isClientSide) return
            val state = station.state()
            val finish = WeaponFinishes.ingredient(state.getItem(MATERIAL)) ?: return
            if (state.getItem(HAMMER).isEmpty || WeaponFinishes.preview(state.getItem(WEAPON), finish).isEmpty) return
            val config = TemperAndTang.config
            if (!progress.strike(station.level().gameTime, config.strikeIntervalTicks.get(), config.requiredStrikes.get())) return
            state.setControl("progress", progress.strikes.toFloat())
            station.level().playSound(null, station.pos(), SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.4f, 1.0f)
            player.displayClientMessage(Component.translatable("temperandtang.station.progress", progress.strikes, config.requiredStrikes.get()), true)
            updateOutputs(station)
        }
        override fun updateOutputs(station: StationContext) {
            val state = station.state()
            val finish = WeaponFinishes.ingredient(state.getItem(MATERIAL))
            val ready = TemperAndTang.config.enabled.get() && progress.ready(TemperAndTang.config.requiredStrikes.get()) && !state.getItem(HAMMER).isEmpty
            state.setOutput("result", if (ready && finish != null) WeaponFinishes.preview(state.getItem(WEAPON), finish) else ItemStack.EMPTY)
        }
        override fun takeOutput(station: StationContext, player: Player, output: String): ItemStack {
            if (station.isClientSide || output != "result" || player.isSpectator || !TemperAndTang.config.enabled.get()) return ItemStack.EMPTY
            val state = station.state()
            val config = TemperAndTang.config
            if (!progress.ready(config.requiredStrikes.get()) || state.getItem(HAMMER).isEmpty) return ItemStack.EMPTY
            val finish = WeaponFinishes.ingredient(state.getItem(MATERIAL)) ?: return ItemStack.EMPTY
            val result = WeaponFinishes.preview(state.getItem(WEAPON), finish)
            if (result.isEmpty) return ItemStack.EMPTY
            if (!player.isCreative && player.experienceLevel < config.experienceLevels.get()) {
                player.displayClientMessage(Component.translatable("temperandtang.station.experience", config.experienceLevels.get()), true)
                return ItemStack.EMPTY
            }
            if (!player.isCreative) player.giveExperienceLevels(-config.experienceLevels.get())
            val material = state.getItem(MATERIAL).copy().also { it.shrink(1) }
            val hammer = state.getItem(HAMMER).copy()
            hammer.damageValue += config.requiredStrikes.get()
            state.setItem(WEAPON, ItemStack.EMPTY)
            state.setItem(MATERIAL, material)
            state.setItem(HAMMER, if (hammer.damageValue >= hammer.maxDamage) ItemStack.EMPTY else hammer)
            progress.reset()
            state.setControl("progress", 0f)
            return result
        }
    }
}
