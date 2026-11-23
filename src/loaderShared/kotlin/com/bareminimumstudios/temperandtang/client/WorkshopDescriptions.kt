package com.bareminimumstudios.temperandtang.client

import com.bareminimumstudios.temperandtang.TemperAndTang
import com.bareminimumstudios.temperandtang.smithing.*
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.item.ItemStack
import java.util.Locale

object WorkshopDescriptions {
    private fun effects(values:List<Pair<String,Double>>):MutableComponent {
        val text=Component.empty()
        for((key,value) in values.filter {kotlin.math.abs(it.second)>.000001}) {
            if(text.siblings.isNotEmpty())text.append(Component.literal(", "))
            text.append(Component.translatable("temperandtang.guide.stat",String.format(Locale.ROOT,"%+.1f%%",value*100),Component.translatable("temperandtang.guide.stat_"+key)))
        }
        if(text.siblings.isEmpty())text.append(Component.translatable("temperandtang.guide.neutral"))
        return text
    }
    fun material(stack:ItemStack,preview:Boolean=false):Component {
        val material=if(preview)CraftMaterials.identify(stack) else CraftMaterials.recorded(stack)
        val power=if(preview)TemperAndTang.config.materialPower.get() else CraftMaterials.power(stack)
        val values=mutableListOf("durability" to material.durability*power)
        if(GearSupport.armor(stack))values+=listOf("armor" to material.armor*power,"movement" to material.movement*power)
        else if(GearSupport.weapon(stack))values+=listOf("damage" to material.damage*power,"speed" to material.speed*power)
        return Component.translatable("temperandtang.guide.material",Component.translatable("temperandtang.material.name."+material.key),effects(values))
    }
    fun finish(stack:ItemStack,finish:Finish):Component {
        val effect=FinishRules.effect(finish,TemperAndTang.config.balance())
        val values=mutableListOf("durability" to effect.durability)
        if(GearSupport.armor(stack)) {if(finish==Finish.LIGHTENED)values+="movement" to TemperAndTang.config.lightenedArmorSpeed.get()}
        else values+=listOf("damage" to effect.damage,"speed" to effect.speed)
        return Component.translatable("temperandtang.guide.finish_effect",effects(values))
    }
}
