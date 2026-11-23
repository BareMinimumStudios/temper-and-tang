package com.bareminimumstudios.temperandtang.client

import com.bareminimumstudios.temperandtang.TemperAndTang
import com.bareminimumstudios.temperandtang.smithing.WorkshopMotion as Motion
import com.pockyl.diegesis.api.client.*
import com.pockyl.diegesis.api.scene.Visual
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import org.joml.Vector3f
import kotlin.math.*

/** One accepted server event drives the contact pose, particles and local sound together. */
class HammerPresentation(private val finishing:Boolean) {
    private data class Strike(val pitch:Float,val sparks:Boolean,val accuracy:Float)
    private val pending=ArrayDeque<Strike>()
    private var strike:Strike?=null
    private var elapsed=0f
    private var impacted=false
    private var lastTarget=""
    private var cachedItem=""
    val busy get()=strike!=null || pending.isNotEmpty()
    private var anchor=0f
    private var sinceSync=0f
    private var marker=0f
    private var initialized=false
    private var wasWorking=false
    private var beatArmed=false
    private fun surface(view:StationView):Float=if(finishing)14.4f else if(view.level().getBlockState(view.pos()).`is`(com.bareminimumstudios.temperandtang.content.TemperContent.block("shaping_anvil")))13.4f else 16.4f
    fun sync(view:StationView) {
        val shown=view.state().getControl("phase")
        val remaining=view.state().getControl("target")
        if(remaining>0f || shown>0f)beatArmed=true
        val phase=if(shown==0f && remaining>0f).5f-remaining/TemperAndTang.config.shapingBeatTicks.get() else shown
        if(!initialized || phase<anchor-.2f){marker=phase.coerceIn(0f,1f);initialized=true}
        anchor=phase;sinceSync=0f
    }
    fun effect(effect:ResourceLocation,data:CompoundTag) {
        if(effect!=TemperAndTang.id("hammer_strike"))return
        if(pending.size<40)pending.addLast(Strike(data.getFloat("pitch"),data.getBoolean("sparks"),data.getFloat("accuracy")))
    }
    fun frame(view:StationView,dt:Float) {
        val state=view.state();val ready=!state.getOutput("result").isEmpty
        val input=if(finishing)state.getItem(com.bareminimumstudios.temperandtang.station.FinishingStation.WEAPON) else state.getItem(com.bareminimumstudios.temperandtang.station.ThermalStations.GEAR)
        val working=if(finishing)!input.isEmpty && !state.getItem(com.bareminimumstudios.temperandtang.station.FinishingStation.HAMMER).isEmpty && com.bareminimumstudios.temperandtang.smithing.WeaponFinishes.ingredient(state.getItem(com.bareminimumstudios.temperandtang.station.FinishingStation.MATERIAL))!=null else state.data().getString("status")=="work"
        if(working && !wasWorking){initialized=false;sync(view)}
        if(!working){marker=0f;initialized=false;beatArmed=false}
        wasWorking=working
        if(strike==null && pending.isNotEmpty()){strike=pending.removeFirst();elapsed=0f;impacted=false}
        val show=working || busy
        val contactY=surface(view)
        val grip=Motion.hammerGrip(contactY)
        view.node("strike").ifPresent {it.setVisible(show);it.moveTo(grip.x-8f,grip.y,grip.z,0f);it.rotateTo(if(strike!=null)Motion.hammerAngle(elapsed) else -42f,0f,0f,0f);it.setTint(0xFFFFFFFF.toInt())}
        // Keep the physical tool identical to the held item; no disembodied glove geometry.
        view.node("smith_hand").ifPresent {it.setVisible(false)}
        view.node("hammer").ifPresent {it.setVisible(!show)}
        view.node("impact").ifPresent {it.moveTo(0f,contactY,0f,0f)}
        run {
            val reforging=state.data().getBoolean("reforging")
            val progress=state.getControl("progress").toInt()
            val visibleHits=(progress-if(busy && !impacted)1 else 0).coerceAtLeast(0)
            val existing=finishing || reforging
            val id=if(existing && !input.isEmpty)BuiltInRegistries.ITEM.getKey(input.item).toString() else if(finishing)cachedItem else state.data().getString("target")
            if(id.isNotEmpty())cachedItem=id
            val targetItem=ResourceLocation.tryParse(id)?.let {BuiltInRegistries.ITEM.get(it)}?:net.minecraft.world.item.Items.AIR
            val required=if(finishing)TemperAndTang.config.requiredStrikes.get()else state.getControl("required").toInt().coerceAtLeast(1)
            val ratio=visibleHits.toFloat()/required
            val stage=if(existing)id else when {ratio<.35f->"shaping_billet";ratio<.7f->if(WorkpieceForms.plate(targetItem))"armor_plate" else "drawn_stock";else->WorkpieceForms.model(targetItem)}
            val key=(if(existing)"item:" else "stock:")+stage
            if(key!=lastTarget){
                val visual=if(existing)ResourceLocation.tryParse(id)?.let {Visual.ofItem(BuiltInRegistries.ITEM.get(it))} else Visual.ofModel(TemperAndTang.id("diegesis/"+stage)).asEmissive()
                visual?.let {v->view.node("workpiece_display").ifPresent {it.setVisual(v.withAnchor(Vector3f(8f,8f,8f)));it.rotateTo(if(existing)0f else -90f,0f,0f,0f)}}
                lastTarget=key
            }
            view.node("workpiece_display").ifPresent {it.setVisible(show && (existing || visibleHits>0) && (!ready || !impacted));it.moveTo(0f,contactY-0.15f,0f,0f);it.setTint(if(existing)0xFFFFFFFF.toInt() else 0xFFFFA06A.toInt())}
            if(!finishing)view.node("pattern_board").ifPresent {it.setVisible(!show && !ready && !reforging);it.moveTo(0f,contactY-.35f,0f,0f)}
            if(!finishing)for(i in 0..8)view.node("grid_$i").ifPresent {it.setVisible(!ready && !reforging && (!show || visibleHits==0))}
            if(!finishing)view.node("gear").ifPresent {it.setVisible(!show && !ready && reforging)}
        }
        view.node("result").ifPresent {it.setVisible(ready && (!busy || impacted))}
        if(finishing)view.node("weapon").ifPresent {it.setVisible(!ready && !show)}
        sinceSync+=dt
        val predicted=(anchor+sinceSync*20f/TemperAndTang.config.shapingBeatTicks.get()).coerceIn(0f,1f)
        val advanced=marker+dt*20f/TemperAndTang.config.shapingBeatTicks.get()
        marker=if(!working || !beatArmed || predicted<=0f)0f else max(marker,advanced+(predicted-advanced)*(1-exp(-dt*8f))).coerceIn(0f,1f)
        view.node("timing_marker").ifPresent {it.moveTo((marker-.5f)*12f,0f,0f,0f)}
        for(id in listOf("timing_rail","timing_band","timing_marker"))view.node(id).ifPresent {it.setVisible(working && !ready)}
        strike?.let {event->
            val before=elapsed;elapsed+=dt
            if(!impacted && before<Motion.IMPACT && elapsed>=Motion.IMPACT) {
                // Render exactly one contact frame before rebounding, even after a slow frame.
                view.node("strike").ifPresent {it.rotateTo(0f,0f,0f,0f)}
                impacted=true
                view.playSound(SoundEvent.createVariableRangeEvent(TemperAndTang.id("forge_impact")),"impact",.72f,event.pitch)
                if(event.sparks) {
                    val random=view.level().random
                    repeat(if(event.accuracy>=.75f)12 else 7) {
                        val a=random.nextFloat()*PI.toFloat()*2
                        val speed=9f+random.nextFloat()*10f
                        view.spawnParticles(TemperAndTang.id("forge_spark"),Vector3f(8f,contactY+.15f,9f),Vector3f(cos(a)*speed,7f+random.nextFloat()*8f,sin(a)*speed),.18f,1,0xFFFFD38A.toInt())
                    }
                }
            }
            if(elapsed>=Motion.SWING)strike=null
        }
    }
}

class QuenchPresentation {
    private var elapsed=-1f
    private var duration=2f
    private var hiss=false
    val busy get()=elapsed>=0f && elapsed<duration
    fun effect(effect:ResourceLocation,data:CompoundTag) {
        if(effect!=TemperAndTang.id("quench_started"))return
        elapsed=0f;duration=(data.getInt("ticks")/20f).coerceAtLeast(.5f);hiss=false
    }
    fun frame(view:StationView,dt:Float) {
        val status=view.state().data().getString("status")
        if(busy && status !in listOf("quenching","ready"))elapsed=-1f
        if(elapsed>=0f)elapsed=(elapsed+dt).coerceAtMost(duration)
        val u=if(busy)(elapsed/duration).coerceIn(0f,1f) else 0f
        val grip=Motion.tongGrip(u)
        val piece=Motion.quenchPiece(u)
        view.node("quench").ifPresent {it.moveTo(grip.point.x-2.15f,grip.point.y-12.2f,grip.point.z-2.6f,0f);it.rotateTo(grip.angle,0f,0f,0f)}
        val open=if(u>.20f && u<.93f)0f else 6f
        view.node("tong_left").ifPresent {it.rotateTo(0f,open,0f,0f)}
        view.node("tong_right").ifPresent {it.rotateTo(0f,-open,0f,0f)}
        view.node("quench_hand").ifPresent {it.setVisible(busy)}
        val ready=!view.state().getOutput("result").isEmpty
        view.node("stock").ifPresent {it.setVisible(!ready || busy);it.moveTo(piece.x-12.5f,piece.y-12.35f,piece.z-8f,0f)}
        view.node("result").ifPresent {it.setVisible(ready && !busy)}
        view.node("steam").ifPresent {it.setEmitting(busy && u>.52f && u<.81f)}
        if(busy && !hiss && u>=.52f) {
            hiss=true;view.playSound(SoundEvents.FIRE_EXTINGUISH,"water_surface",.65f,1.25f)
            view.spawnParticles(DiegesisParticles.SPLASH,Vector3f(8f,9.85f,8f),Vector3f(0f,6f,0f),1f,7,0xAA88BBE8.toInt())
            view.spawnParticles(DiegesisParticles.STEAM,Vector3f(8f,10f,8f),Vector3f(0f,6f,0f),.8f,14,0)
        }
    }
}
