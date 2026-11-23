package com.bareminimumstudios.temperandtang.client

import com.bareminimumstudios.temperandtang.TemperAndTang
import com.pockyl.diegesis.api.client.*
import com.pockyl.diegesis.api.state.StationState
import net.minecraft.client.Minecraft
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import org.lwjgl.glfw.GLFW

object ThermalClient {
    fun initialize() {
        for ((name, action) in listOf("charcoal_hearth" to "heat", "shaping_anvil" to "strike", "quenching_trough" to "quench"))
            DiegesisClient.registerBehavior(TemperAndTang.id(name)) { Feedback(action) }
    }
    private class Feedback(val action: String) : StationBehavior {
        private var progress = 0f
        private var returnAfter = 0f
        private var animationTime = 0f
        override fun onOpen(view: StationView) { refresh(view) }
        override fun onStateChanged(view: StationView, changes: StationState.Changes) {
            val now = view.state().getControl("progress")
            if (action == "strike" && now > progress) {
                view.node("strike").ifPresent { it.rotateTo(0f,0f,35f,0.06f); it.moveTo(0f,-0.5f,0f,0.06f) }
                returnAfter=0.08f
            }
            progress=now
            refresh(view)
        }
        override fun frame(view: StationView, deltaSeconds: Float) {
            animationTime+=deltaSeconds
            val status=view.state().data().getString("status")
            if(action=="heat") view.node("heat").ifPresent { it.moveTo(0f,if(status=="heating") -0.35f*(1f+kotlin.math.sin(animationTime*5f)) else 0f,0f,0.05f) }
            if(action=="quench") view.node("quench").ifPresent { it.moveTo(0f,if(status=="quenching") -3f else 0f,0f,0.18f) }
            if (returnAfter<=0f) return
            returnAfter-=deltaSeconds
            if (returnAfter<=0f) view.node("strike").ifPresent { it.rotateTo(0f,0f,0f,0.14f); it.moveTo(0f,0f,0f,0.14f) }
        }
        override fun onKey(view: StationView, keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
            val command=when {
                keyCode==GLFW.GLFW_KEY_SPACE -> action
                action=="strike" && keyCode==GLFW.GLFW_KEY_LEFT -> "previous"
                action=="strike" && keyCode==GLFW.GLFW_KEY_RIGHT -> "next"
                else -> return false
            }
            if (!view.isFocused) return false
            if (Minecraft.getInstance().player?.containerMenu?.carried?.isEmpty != true) {
                view.showCaption(Component.translatable("temperandtang.station.empty_cursor"),3f)
            } else view.sendAction(command,CompoundTag())
            return true
        }
        private fun refresh(view: StationView) {
            val state=view.state()
            val data=state.data()
            val status=data.getString("status").ifEmpty { "stock" }
            val id=ResourceLocation.tryParse(data.getString("target"))
            val target=id?.let { ItemStack(BuiltInRegistries.ITEM.get(it)).hoverName } ?: Component.empty()
            val args: Array<Any> = when(status) {
                "pattern" -> arrayOf(target)
                "work" -> arrayOf(state.getControl("progress").toInt(),state.getControl("required").toInt())
                "heating", "quenching" -> arrayOf(state.getControl("progress").toInt())
                else -> emptyArray()
            }
            val caption=Component.translatable("temperandtang.thermal."+status,*args)
            if (action=="heat" && status=="idle") caption.append(Component.literal(" Fuel: "+state.getControl("fuel_cost").toInt()+" coal / charcoal."))
            if (view.isFocused) view.showCaption(caption,0f)
            if (action!="strike") return
            for(i in 0..8) {
                val ghost=ResourceLocation.tryParse(data.getString("ghost_"+i))?.let { ItemStack(BuiltInRegistries.ITEM.get(it)) }
                view.node("grid_"+i).ifPresent { it.setGhostItems(if(ghost==null || ghost.isEmpty) emptyList() else listOf(ghost)) }
            }
            view.node("strike").ifPresent { it.setTint(if(state.getControl("beat")>0f) 0xFFFFC46B.toInt() else 0xFFFFFFFF.toInt()) }
        }
    }
}
