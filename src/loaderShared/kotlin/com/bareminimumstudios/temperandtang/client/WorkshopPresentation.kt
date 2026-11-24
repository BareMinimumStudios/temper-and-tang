package com.bareminimumstudios.temperandtang.client

import com.bareminimumstudios.temperandtang.TemperAndTang
import com.bareminimumstudios.temperandtang.smithing.RhythmTrack
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
    private var noteId=0L
    private var initialized=false
    private var glow=0f
    private var glowScore=0f
    private var pulseTime=0f
    private var wasWorking=false
    private var beatArmed=false
    private fun surface(view:StationView):Float=if(finishing)14.4f else if(view.level().getBlockState(view.pos()).`is`(com.bareminimumstudios.temperandtang.content.TemperContent.block("shaping_anvil")))13.4f else 16.4f
    fun sync(view:StationView) {
        val state=view.state()
        val period=state.getControl("period").coerceAtLeast(1f)
        val until=state.getControl("target").takeIf {it>0f}?:((.5f-state.getControl("phase"))*period)
        if(state.getControl("target")>0f || state.getControl("phase")>0f)beatArmed=true
        val id=state.data().getLong("rhythm_target")
        if(!initialized){marker=until;initialized=true}
        else if(id!=0L && noteId!=0L && id!=noteId)marker+=(id-noteId).toFloat()
        noteId=id
        anchor=until;sinceSync=0f
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
        val side=if(net.minecraft.client.Minecraft.getInstance().player?.mainArm==net.minecraft.world.entity.HumanoidArm.LEFT)-1 else 1
        val pose=view.node("strike_pose")
        val grip=if(pose.isPresent)Motion.sideHammerGrip(contactY,side)else Motion.hammerGrip(contactY)
        pose.ifPresent {it.moveTo(grip.x-8f,grip.y,grip.z,0f);it.rotateTo(0f,90f*side,0f,0f)}
        view.node("strike").ifPresent {
            it.setVisible(show)
            if(pose.isPresent)it.moveTo(0f,0f,0f,0f)else it.moveTo(grip.x-8f,grip.y,grip.z,0f)
            it.rotateTo(if(strike!=null)Motion.hammerAngle(elapsed) else -42f,0f,0f,0f);it.setTint(0xFFFFFFFF.toInt())
        }
        // Keep the physical tool identical to the held item; no disembodied glove geometry.
        view.node("smith_hand").ifPresent {it.setVisible(false)}
        view.node("hammer").ifPresent {it.setVisible(!show)}
        view.node("impact").ifPresent {it.moveTo(0f,contactY,0f,0f)}
        run {
            val reforging=state.data().getBoolean("reforging")
            val progress=state.getControl("progress").toInt()
            val visibleHits=(progress-pending.size-if(strike!=null && !impacted)1 else 0).coerceAtLeast(0)
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
            view.node("workpiece_display").ifPresent {it.setVisible(show && (existing || visibleHits>0) && (!ready || pending.isNotEmpty() || !impacted));it.moveTo(0f,contactY-0.15f,0f,0f);it.setTint(if(existing)0xFFFFFFFF.toInt() else 0xFFFFA06A.toInt())}
            if(!finishing)view.node("pattern_board").ifPresent {it.setVisible(!show && !ready && !reforging);it.moveTo(0f,contactY-.14f,0f,0f)}
            if(!finishing) {
                val metals=(0..8).filter {com.bareminimumstudios.temperandtang.smithing.WorkshopPlans.metal(state.getItem(com.bareminimumstudios.temperandtang.station.ThermalStations.grid(it)))}
                val columns=metals.size.coerceIn(1,3);val rows=(metals.size+columns-1)/columns
                for(i in 0..8)view.node("grid_$i").ifPresent {
                    val index=metals.indexOf(i)
                    it.setVisible(!ready && !reforging && (!show || visibleHits==0 && index>=0))
                    if(show && index>=0) {
                        val x=8f+(index%columns-(columns-1)/2f)*1.25f
                        val z=9f+(index/columns-(rows-1)/2f)*1.2f
                        it.moveTo(x-(4f+4f*(i%3)),contactY+.1f-13.3f,z-(5f+4f*(i/3)),0f)
                        it.scaleTo(.45f,0f)
                    } else {it.moveTo(0f,contactY-13.4f,0f,0f);it.scaleTo(1f,0f)}
                }
            }
            if(!finishing)view.node("gear").ifPresent {it.setVisible(!show && !ready && reforging)}
        }
        view.node("result").ifPresent {it.setVisible(ready && pending.isEmpty() && (!busy || impacted))}
        if(finishing)view.node("weapon").ifPresent {it.setVisible(!ready && !show)}
        pulseTime+=dt
        glow=(glow-dt*5f).coerceAtLeast(0f)
        sinceSync+=dt
        val predicted=anchor-sinceSync*20f
        marker=if(!working || !beatArmed)anchor else RhythmTrack.advance(marker,predicted,dt)
        val x=RhythmTrack.position(marker)
        view.node("timing_marker").ifPresent {it.moveTo(x,0f,0f,0f);it.setVisible(working && !ready && beatArmed && RhythmTrack.visible(x))}
        for(id in listOf("timing_rail","timing_band"))view.node(id).ifPresent {it.setVisible(working && !ready)}
        view.node("timing_band").ifPresent {it.moveTo(RhythmTrack.TARGET,0f,0f,0f)}
        val proximity=(1f-abs(marker)/8f).coerceIn(0f,1f)
        val pulse=.5f+.5f*sin(pulseTime*9f)
        val light=(120+proximity*100+pulse*25).toInt().coerceIn(0,255)
        view.node("timing_band").ifPresent {it.setTint((0xFF000000L or (65L shl 16) or (light.toLong() shl 8) or 95L).toInt())}
        view.node("timing_marker").ifPresent {it.scaleTo(1f+proximity*.07f+glow*.06f,0f)}
        view.node("timing_marker").ifPresent {it.setTint(if(glow>0f)if(glowScore>=.75f)0xFFE8FFD5.toInt()else 0xFFFFAF79.toInt()else if(proximity>.65f)0xFFFFF3C2.toInt()else 0xFFFFCF62.toInt())}
        view.node("impact_flash").ifPresent {
            it.setVisible(glow>0f && show)
            it.moveTo(0f,contactY+.08f,0f,0f)
            it.scaleTo(1f+(1f-glow)*.65f,0f)
            it.setTint(((glow*180).toLong().coerceIn(0,255) shl 24 or 0xFFD68AL).toInt())
        }
        val count=state.getControl("phrase_size").toInt().coerceIn(1,3)
        val step=state.getControl("phrase_step").toInt().coerceIn(1,3)
        val gap=state.getControl("next_gap")
        for(i in 1..3)view.node("rhythm_note_$i").ifPresent {
            val upcoming=RhythmTrack.position(marker+gap*i)
            it.setVisible(working && !ready && beatArmed && i<=count-step && gap>0f && RhythmTrack.visible(upcoming))
            it.moveTo(upcoming,0f,0f,0f)
            it.setTint(0xFFFFC66B.toInt())
        }
        strike?.let {event->
            val before=elapsed;elapsed+=dt*1.35f
            if(!impacted && before<Motion.IMPACT && elapsed>=Motion.IMPACT) {
                // Render exactly one contact frame before rebounding, even after a slow frame.
                view.node("strike").ifPresent {it.rotateTo(0f,0f,0f,0f)}
                impacted=true;glow=1f;glowScore=event.accuracy
                view.node("impact_flash").ifPresent {it.setVisible(true);it.moveTo(0f,contactY+.08f,0f,0f);it.scaleTo(1f,0f);it.setTint(0xB4FFD68A.toInt())}
                view.playSound(SoundEvent.createVariableRangeEvent(TemperAndTang.id("forge_impact")),"impact",.72f,event.pitch)
                if(event.sparks) {
                    val random=view.level().random
                    repeat(if(event.accuracy>=.99f)18 else if(event.accuracy>=.75f)12 else 5) {
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
        val closure=when {u<.10f->0f;u<.22f->Motion.ease((u-.10f)/.12f);u<.90f->1f;else->1f-Motion.ease((u-.90f)/.10f)}
        val open=6f*(1f-closure)
        view.node("tong_left").ifPresent {it.rotateTo(0f,-open,0f,0f)}
        view.node("tong_right").ifPresent {it.rotateTo(0f,open,0f,0f)}
        view.node("quench_hand").ifPresent {it.setVisible(false)}
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
