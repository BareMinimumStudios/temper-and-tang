package com.bareminimumstudios.temperandtang.client

import com.bareminimumstudios.temperandtang.TemperAndTang
import com.bareminimumstudios.temperandtang.station.FinishingStation
import com.bareminimumstudios.temperandtang.smithing.WeaponFinishes
import com.pockyl.diegesis.api.client.DiegesisClient
import com.pockyl.diegesis.api.client.StationBehavior
import com.pockyl.diegesis.api.client.StationView
import com.pockyl.diegesis.api.state.StationState
import net.minecraft.client.Minecraft
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW

/** Client feedback only. Both input routes use the same server-authoritative strike checks. */
object TemperClient {
    fun initialize() {
        ThermalClient.initialize()
        DiegesisClient.registerBehavior(TemperAndTang.id("finishing_anvil")) { Feedback() }
    }
    private class Feedback : StationBehavior {
        private var carrying = false
        private var levels = -1
        private val hammer=HammerPresentation(true)
        override fun onOpen(view: StationView) {
            view.setInventoryFilter { stack -> WeaponFinishes.accepts(stack) || WeaponFinishes.ingredient(stack) != null || FinishingStation.isHammer(stack) }
            hammer.sync(view)
            refresh(view)
        }
        override fun onStateChanged(view:StationView,changes:StationState.Changes) {hammer.sync(view);refresh(view)}
        override fun onEffect(view:StationView,effect:net.minecraft.resources.ResourceLocation,node:String?,data:CompoundTag) {hammer.effect(effect,data)}
        override fun frame(view:StationView,deltaSeconds:Float) {hammer.frame(view,deltaSeconds)}
        override fun tick(view: StationView) {
            if (!view.isFocused) return
            val now = Minecraft.getInstance().player?.containerMenu?.carried?.isEmpty == false
            val experience = Minecraft.getInstance().player?.experienceLevel ?: 0
            if (now != carrying || levels != experience) {
                carrying = now
                levels = experience
                refresh(view)
            }
        }
        override fun onKey(view: StationView, keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
            if (keyCode != GLFW.GLFW_KEY_SPACE || !view.isFocused) return false
            if (Minecraft.getInstance().player?.containerMenu?.carried?.isEmpty != true) {
                view.showCaption(Component.translatable("temperandtang.station.empty_cursor"), 3f)
                return true
            }
            view.sendAction("strike", CompoundTag())
            return true
        }
        private fun refresh(view: StationView) {
            if (!view.isFocused) return
            val state = view.state()
            val ready=!state.getOutput("result").isEmpty
            view.node("weapon").ifPresent {it.setVisible(!ready)}
            view.node("result").ifPresent {it.setVisible(ready)}
            view.node("strike").ifPresent {it.setVisible(!ready)}
            val finish = WeaponFinishes.ingredient(state.getItem(FinishingStation.MATERIAL))
            val key = when {
                !state.getOutput("result").isEmpty && state.getItem(FinishingStation.WEAPON).isEmpty -> "take_result"
                carrying -> "empty_cursor"
                state.getItem(FinishingStation.WEAPON).isEmpty -> "insert_weapon"
                state.getItem(FinishingStation.MATERIAL).isEmpty -> "insert_material"
                state.getItem(FinishingStation.HAMMER).isEmpty -> "insert_hammer"
                !FinishingStation.hammerReady(state.getItem(FinishingStation.HAMMER)) -> "worn_hammer"
                finish != null && WeaponFinishes.preview(state.getItem(FinishingStation.WEAPON), finish).isEmpty -> "invalid_weapon"
                !state.getOutput("result").isEmpty && (Minecraft.getInstance().player?.experienceLevel ?: 0) < TemperAndTang.config.experienceLevels.get()
                    && Minecraft.getInstance().player?.isCreative != true -> "need_experience"
                !state.getOutput("result").isEmpty -> "take_result"
                else -> "instructions"
            }
            for(id in listOf("timing_rail","timing_band","timing_marker"))view.node(id).ifPresent {it.setVisible(key=="instructions" && !ready)}
            val perfect=(state.getControl("window").toInt()/2+1).toFloat()
            view.node("timing_band").ifPresent {it.scaleTo(12f*perfect/TemperAndTang.config.shapingBeatTicks.get(),0.05f)}
            val caption=Component.translatable("temperandtang.station." + key,
                state.getControl("progress").toInt(), TemperAndTang.config.requiredStrikes.get(),
                TemperAndTang.config.experienceLevels.get())
            if(key=="instructions")caption.append(Component.translatable("temperandtang.craft.finish_timing",state.getControl("accuracy").toInt()))
            view.showCaption(caption,0f)
        }
    }
}
