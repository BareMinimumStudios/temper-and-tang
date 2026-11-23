package com.bareminimumstudios.temperandtang.station

import com.bareminimumstudios.temperandtang.TemperAndTang
import com.pockyl.diegesis.api.station.StationContext
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource

object SmithingFeedback {
    fun strike(station:StationContext,node:String,accuracy:Double) {
        if(station.isClientSide)return
        val pitch=(0.9+station.level().random.nextDouble()*0.25+accuracy*0.15).toFloat()
        station.level().playSound(null,station.pos(),SoundEvents.ANVIL_HIT,SoundSource.BLOCKS,0.65f,pitch)
        if(!TemperAndTang.config.sparks.get())return
        station.effect(ResourceLocation.fromNamespaceAndPath("diegesis","spark"),node,CompoundTag().also {it.putInt("count",if(accuracy>=0.75) 7 else 3)})
        (station.level() as? ServerLevel)?.sendParticles(ParticleTypes.ELECTRIC_SPARK,station.pos().x+0.5,station.pos().y+1.03,station.pos().z+0.5,if(accuracy>=0.75) 5 else 2,0.08,0.03,0.08,0.035)
    }
}
