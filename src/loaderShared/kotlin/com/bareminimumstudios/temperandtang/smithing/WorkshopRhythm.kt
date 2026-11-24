package com.bareminimumstudios.temperandtang.smithing

import com.bareminimumstudios.temperandtang.TemperAndTang
import com.pockyl.diegesis.api.state.StationState
import net.minecraft.world.item.ItemStack

object WorkshopRhythm {
    fun evaluationTick(now:Long,player:net.minecraft.world.entity.player.Player?):Long {
        val ping=(player as? net.minecraft.server.level.ServerPlayer)?.connection?.latency()?:0
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
