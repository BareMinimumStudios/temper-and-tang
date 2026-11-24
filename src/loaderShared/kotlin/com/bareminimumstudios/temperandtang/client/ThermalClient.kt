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
        DiegesisClient.registerParticle(TemperAndTang.id("forge_spark"),ParticleStyle.builder(ParticleStyle.frames("spark",8))
            .color(0xFFFFD38A.toInt()).size(.45f,.85f).life(.22f,.42f).gravity(42f).drag(.25f).stretch().emissive().build())
        DiegesisClient.registerParticle(TemperAndTang.id("bellows_air"),ParticleStyle.builder(ResourceLocation.fromNamespaceAndPath("diegesis","steam_3"))
            .color(0xDDE5EFF5.toInt()).size(.7f,1f).life(.42f,.52f).gravity(0f).drag(.05f).stretch().grow(1.15f).fadeIn(.18f).emissive().build())
        for ((name, action) in listOf("charcoal_hearth" to "heat", "alloy_furnace" to "heat", "nether_alloy_furnace" to "heat", "shaping_anvil" to "strike", "quenching_trough" to "quench"))
            DiegesisClient.registerBehavior(TemperAndTang.id(name)) { Feedback(action) }
    }
    private class Feedback(val action: String) : StationBehavior {
        private val hammer=HammerPresentation(false)
        private val quench=QuenchPresentation()
        private val bellows=BellowsPresentation()
        private var patterns:PatternPicker?=null
        override fun onOpen(view:StationView) {if(action=="strike")hammer.sync(view);refresh(view)}
        override fun onStateChanged(view:StationView,changes:StationState.Changes) {if(action=="strike")hammer.sync(view);refresh(view)}
        override fun onEffect(view:StationView,effect:ResourceLocation,node:String?,data:CompoundTag) {
            if(action=="strike")hammer.effect(effect,data)
            if(action=="quench")quench.effect(effect,data)
        }
        override fun frame(view:StationView,deltaSeconds:Float) {

            val status=view.state().data().getString("status")
            if(action=="heat")bellows.frame(view,deltaSeconds)
            if(action=="strike")hammer.frame(view,deltaSeconds)
            if(action=="quench")quench.frame(view,deltaSeconds)
        }
        override fun onClose(view:StationView) {patterns?.close();patterns=null}
        override fun onChar(view:StationView,character:Char,modifiers:Int):Boolean = patterns?.type(character)?:false
        override fun onKey(view: StationView, keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
            patterns?.let {if(it.open)return it.key(keyCode)}
            if(action=="strike" && keyCode==GLFW.GLFW_KEY_F && view.isFocused && view.state().data().getString("status")!="work" && view.state().getOutput("result").isEmpty && !view.state().data().getBoolean("reforging")) {
                patterns=PatternPicker(view);view.showOverlay(patterns!!);return true
            }
            if(keyCode==GLFW.GLFW_KEY_H && view.isFocused){WorkshopGuidance.toggle(view);refresh(view);return true}
            val command=when {
                keyCode==GLFW.GLFW_KEY_SPACE -> action
                action=="strike" && keyCode==GLFW.GLFW_KEY_LEFT -> "previous"
                action=="strike" && keyCode==GLFW.GLFW_KEY_RIGHT -> "next"
                action=="strike" && keyCode==GLFW.GLFW_KEY_B -> "scribe"
                action=="strike" && keyCode==GLFW.GLFW_KEY_TAB -> "category"
                action=="strike" && keyCode==GLFW.GLFW_KEY_R -> "mode"
                else -> return false
            }
            if (!view.isFocused) return false
            if (Minecraft.getInstance().player?.containerMenu?.carried?.isEmpty != true) {
                view.showCaption(Component.translatable("temperandtang.station.empty_cursor"),3f)
            } else view.sendAction(command,CompoundTag())
            return true
        }
        private fun refresh(view: StationView) {
            val visibleState=view.state()
            val ready=!visibleState.getOutput("result").isEmpty
            view.node("result").ifPresent {it.setVisible(ready)}
            if(action=="strike") {
                val reforging=visibleState.data().getBoolean("reforging")
                for(i in 0..8)view.node("grid_"+i).ifPresent {it.setVisible(!reforging && !ready)}
                view.node("gear").ifPresent {it.setVisible(reforging && !ready)}
                view.node("flux").ifPresent {it.setVisible(reforging)}
                for(key in listOf("previous","next","category","scribe","blueprint"))view.node(key).ifPresent {it.setVisible(!reforging && !ready)}
                val working=visibleState.data().getString("status")=="work"
                for(key in listOf("previous","next","category","scribe","mode"))view.node(key).ifPresent {it.setVisible(!ready && !working && (key=="mode" || !reforging))}
                for(key in listOf("timing_rail","timing_band","timing_marker"))view.node(key).ifPresent {it.setVisible(working && !ready)}
                val perfect=(visibleState.getControl("window").toInt()/2+1).toFloat()
                view.node("timing_band").ifPresent {it.scaleTo(com.bareminimumstudios.temperandtang.smithing.RhythmTrack.bandWidth(view.state().getControl("perfect_window").coerceAtLeast(1f))/2f,0.05f)}
                view.node("strike").ifPresent {it.setVisible(!ready)}
                view.node("gear").ifPresent {it.setGhostItems(listOf(ItemStack(net.minecraft.world.item.Items.IRON_CHESTPLATE)))}
                view.node("flux").ifPresent {it.setGhostItems(listOf(ItemStack(com.bareminimumstudios.temperandtang.content.TemperContent.item("smithing_flux"))))}
                view.node("blueprint").ifPresent {it.setGhostItems(listOf(ItemStack(com.bareminimumstudios.temperandtang.content.TemperContent.item("blank_blueprint"))))}
            } else {
                view.node("stock").ifPresent {it.setVisible(!ready)}
                val status=visibleState.data().getString("status")
                if(action=="heat") {
                    view.node("additive").ifPresent {it.setGhostItems(listOf(ItemStack(com.bareminimumstudios.temperandtang.content.TemperContent.item("tin_ingot")),ItemStack(net.minecraft.world.item.Items.CHARCOAL)))}
                    view.node("embers").ifPresent {it.setVisible(status in listOf("heating","ready"))}
                    view.node("fire").ifPresent {it.setEmitting(status=="heating")}
                } else {
                    val water=visibleState.getItem(com.bareminimumstudios.temperandtang.station.ThermalStations.WATER)
                    view.node("water_surface").ifPresent {it.setVisible(water.`is`(net.minecraft.world.item.Items.WATER_BUCKET) || status in listOf("quenching","ready"))}


                }
            }
            val state=view.state()
            val data=state.data()
            if(view.isFocused)view.showCaption(WorkshopGuidance.caption(view,action),0f)
            if(action!="strike")return
            for(i in 0..8) {
                val ghost=ResourceLocation.tryParse(data.getString("ghost_"+i))?.let { ItemStack(BuiltInRegistries.ITEM.get(it)) }
                view.node("grid_"+i).ifPresent { it.setGhostItems(if(ghost==null || ghost.isEmpty) emptyList() else listOf(ghost)) }
            }


        }
    }
}
