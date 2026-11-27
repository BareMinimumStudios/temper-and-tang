package com.bareminimumstudios.temperandtang.smithing

import com.bareminimumstudios.temperandtang.TemperAndTang
import com.pockyl.diegesis.api.state.StationState
import com.pockyl.diegesis.api.station.StationContext
import java.util.WeakHashMap
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

object WorkshopRhythm {
    private data class TimingSample(val milliseconds:Int,val measuredAt:Long)
    private val timings=WeakHashMap<ServerPlayer,TimingSample>()
    private fun roundTrip(player:Player?):Int {
        val serverPlayer=player as? ServerPlayer?:return 0
        val sample=timings[serverPlayer]
        return if(sample!=null && System.nanoTime()-sample.measuredAt<120_000_000_000L)sample.milliseconds else serverPlayer.connection.latency()
    }
    /** Three server-owned round trips avoid the initially-zero, slowly averaged vanilla ping. */
    class Measurement {
        private var nonce=0;private var sent=0L;private var samples=0;private var best=Int.MAX_VALUE
        fun begin(s:StationContext) {
            if(s.isClientSide)return
            samples=0;best=Int.MAX_VALUE;challenge(s)
        }
        private fun challenge(s:StationContext) {
            nonce=ForgeRandom.nextInt(Int.MAX_VALUE-1)+1;sent=System.nanoTime()
            val data=s.state().data().copy();data.putInt("timing_probe",nonce)
            data.putInt("rhythm_ping",roundTrip(s.users().firstOrNull()));s.state().setData(data)
        }
        fun acknowledge(s:StationContext,player:Player,data:CompoundTag) {
            val owner=player as? ServerPlayer?:return
            if(s.isClientSide || nonce==0 || data.getInt("nonce")!=nonce || s.users().none {it.uuid==owner.uuid})return
            val measured=((System.nanoTime()-sent)/1_000_000L).coerceIn(0,5000).toInt()
            best=minOf(best,measured);timings[owner]=TimingSample(best,System.nanoTime());samples++;nonce=0
            val state=s.state().data().copy();state.putInt("rhythm_ping",best);s.state().setData(state)
            if(samples<3)challenge(s)
        }
    }

    fun evaluationTick(now:Long,player:Player?):Long {
        val ping=roundTrip(player)
        return RhythmLatency.evaluationTick(now,ping,TemperAndTang.config.latencyCompensationTicks.get())
    }

    fun material(gear:ItemStack,inputs:List<ItemStack> = emptyList())=CraftMaterials.recorded(gear).takeUnless {it==ForgeMaterial.NEUTRAL}?:CraftMaterials.choose(gear,inputs)
    fun profile(gear:ItemStack,inputs:List<ItemStack> = emptyList()):WorkRhythm {
        val c=TemperAndTang.config
        return RhythmRules.profile(material(gear,inputs),c.shapingBeatTicks.get(),c.shapingWindowTicks.get(),c.timingJitter.get(),c.materialDifficulty.get())
    }
    fun create(required:Int,profile:WorkRhythm,now:Long):HammerWork {
        val c=TemperAndTang.config
        return HammerWork(required,profile.period,profile.window,profile.jitter,ForgeRandom::nextInt)
            .configureRhythm(if(c.rhythmPhrases.get())profile.doubles else 0,profile.triples,profile.gap,profile.perfect,c.strikeIntervalTicks.get()).also {it.arm(now)}
    }
    fun sync(state:StationState,job:HammerWork?,profile:WorkRhythm) {
        val data=state.data().copy()
        data.putLong("rhythm_target",job?.target?:0L)
        if(data!=state.data())state.setData(data)
        state.setControl("period",(job?.tempo?:profile.period).toFloat())
        state.setControl("perfect_window",(job?.perfectWindow?:profile.perfect).toFloat())
        state.setControl("phrase_size",(job?.phraseSize?:1).toFloat())
        state.setControl("phrase_step",(job?.phraseStep?:1).toFloat())
        state.setControl("next_gap",(job?.nextGap?:0).toFloat())
        state.setControl("combo",(job?.combo?:0).toFloat())
    }
}
