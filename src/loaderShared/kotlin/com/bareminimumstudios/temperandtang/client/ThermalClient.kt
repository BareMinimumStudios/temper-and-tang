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
        for ((name, action) in listOf("charcoal_hearth" to "heat", "shaping_anvil" to "strike", "quenching_trough" to "quench"))
            DiegesisClient.registerBehavior(TemperAndTang.id(name)) { Feedback(action) }
    }
    private class Feedback(val action: String) : StationBehavior {
        private val hammer=HammerPresentation(false)
        private val quench=QuenchPresentation()
        private val bellows=BellowsPresentation()
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
        override fun onKey(view: StationView, keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
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
                view.node("timing_band").ifPresent {it.scaleTo(12f*perfect/TemperAndTang.config.shapingBeatTicks.get(),0.05f)}
                view.node("strike").ifPresent {it.setVisible(!ready)}
                view.node("gear").ifPresent {it.setGhostItems(listOf(ItemStack(net.minecraft.world.item.Items.IRON_CHESTPLATE)))}
                view.node("flux").ifPresent {it.setGhostItems(listOf(ItemStack(com.bareminimumstudios.temperandtang.content.TemperContent.item("smithing_flux"))))}
                view.node("blueprint").ifPresent {it.setGhostItems(listOf(ItemStack(com.bareminimumstudios.temperandtang.content.TemperContent.item("blank_blueprint"))))}
            } else {
                view.node("stock").ifPresent {it.setVisible(!ready)}
                val status=visibleState.data().getString("status")
                if(action=="heat") {
                    view.node("embers").ifPresent {it.setVisible(status in listOf("heating","ready"))}
                    view.node("fire").ifPresent {it.setEmitting(status=="heating")}
                } else {
                    val water=visibleState.getItem(com.bareminimumstudios.temperandtang.station.ThermalStations.WATER)
                    view.node("water_surface").ifPresent {it.setVisible(water.`is`(net.minecraft.world.item.Items.WATER_BUCKET) || status in listOf("quenching","ready"))}


                }
            }
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
            val captionKey=if(status=="ready") when(action) { "heat"->"heated_ready"; "quench"->"cooled_ready"; else->status } else status
            val caption=Component.translatable("temperandtang.thermal."+captionKey,*args)
            if(action=="strike" && status=="work") caption.append(Component.translatable("temperandtang.craft.live",state.getControl("accuracy").toInt(),data.getInt("mastery"),state.getControl("misses").toInt()))
            if(action=="strike" && status in listOf("pattern","blueprint"))caption.append(Component.translatable("temperandtang.craft.category_"+data.getInt("category")))
            if(action=="strike" && status=="pattern") caption.append(Component.translatable("temperandtang.craft.pattern_keys",data.getInt("mastery")))
            if (action=="heat" && status in listOf("idle","fuel")) caption.append(Component.literal(" Fuel: "+state.getControl("fuel_cost").toInt()+" coal / charcoal."))
            if (view.isFocused) view.showCaption(caption,0f)
            if (action!="strike") return
            for(i in 0..8) {
                val ghost=ResourceLocation.tryParse(data.getString("ghost_"+i))?.let { ItemStack(BuiltInRegistries.ITEM.get(it)) }
                view.node("grid_"+i).ifPresent { it.setGhostItems(if(ghost==null || ghost.isEmpty) emptyList() else listOf(ghost)) }
            }


        }
    }
}
