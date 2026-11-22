package com.bareminimumstudios.temperandtang.client

import com.bareminimumstudios.temperandtang.TemperAndTang
import com.bareminimumstudios.temperandtang.station.FinishingStation
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
        DiegesisClient.registerBehavior(TemperAndTang.id("finishing_anvil")) { Feedback() }
    }
    private class Feedback : StationBehavior {
        private var carrying = false
        override fun onOpen(view: StationView) { refresh(view) }
        override fun onStateChanged(view: StationView, changes: StationState.Changes) { refresh(view) }
        override fun tick(view: StationView) {
            if (!view.isFocused) return
            val now = Minecraft.getInstance().player?.containerMenu?.carried?.isEmpty == false
            if (now != carrying) { carrying = now; refresh(view) }
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
            val key = when {
                carrying -> "empty_cursor"
                state.getItem(FinishingStation.WEAPON).isEmpty -> "insert_weapon"
                state.getItem(FinishingStation.MATERIAL).isEmpty -> "insert_material"
                state.getItem(FinishingStation.HAMMER).isEmpty -> "insert_hammer"
                !state.getOutput("result").isEmpty -> "take_result"
                else -> "instructions"
            }
            view.showCaption(Component.translatable("temperandtang.station." + key,
                state.getControl("progress").toInt(), TemperAndTang.config.requiredStrikes.get(),
                TemperAndTang.config.experienceLevels.get()), 0f)
        }
    }
}
