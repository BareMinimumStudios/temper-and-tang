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
            text.append(Component.translatable("temperandtang.guide.stat",WorkshopText.style(Component.literal(String.format(Locale.ROOT,"%+.1f%%",value*100)),if(value>0)WorkshopText.BENEFIT else WorkshopText.COST),WorkshopText.style(Component.translatable("temperandtang.guide.stat_"+key),WorkshopText.MUTED)))
        }
        if(text.siblings.isEmpty())text.append(Component.translatable("temperandtang.guide.neutral"))
        return text
    }
    fun material(stack:ItemStack,preview:Boolean=false,inputs:List<ItemStack> = emptyList()):Component {
        val material=if(preview)CraftMaterials.choose(stack,inputs) else CraftMaterials.recorded(stack)
        val power=if(preview)TemperAndTang.config.materialPower.get() else CraftMaterials.power(stack)
        val values=mutableListOf("durability" to material.durability*power)
        if(GearSupport.armor(stack))values+=listOf("armor" to material.armor*power,"movement" to material.movement*power)
        else if(GearSupport.weapon(stack))values+=listOf("damage" to material.damage*power,"speed" to material.speed*power)
        if(stack.item is net.minecraft.world.item.DiggerItem)values+="mining" to material.speed*power
        if(stack.item is net.minecraft.world.item.ShieldItem)values+="movement" to material.movement*power
        return Component.translatable("temperandtang.guide.material",WorkshopText.style(Component.translatable("temperandtang.material.name."+material.key),WorkshopText.QUANTITY),effects(values))
    }
    fun trait(stack:ItemStack):Component {
        val trait=CraftTraits.recorded(stack);val power=CraftTraits.power(stack)
        val values=mutableListOf("durability" to trait.durability*power)
        when(CraftTraits.family(stack)) {
            ForgeFamily.ARMOR->values+=listOf("armor" to trait.armor*power,"movement" to trait.movement*power)
            ForgeFamily.SHIELD->values+="movement" to trait.movement*power
            else->{if(GearSupport.weapon(stack))values+=listOf("damage" to trait.damage*power,"speed" to trait.speed*power);if(stack.item is net.minecraft.world.item.DiggerItem)values+="mining" to trait.mining*power}
        }
        return Component.translatable("temperandtang.guide.trait",WorkshopText.style(Component.translatable("temperandtang.trait."+trait.key),WorkshopText.STAGE),effects(values))
    }
    fun finish(stack:ItemStack,finish:Finish):Component {
        val effect=FinishRules.effect(finish,TemperAndTang.config.balance())
        val values=mutableListOf("durability" to effect.durability)
        if(GearSupport.armor(stack)) {if(finish==Finish.LIGHTENED)values+="movement" to TemperAndTang.config.lightenedArmorSpeed.get()}
        else values+=listOf("damage" to effect.damage,"speed" to effect.speed)
        return Component.translatable("temperandtang.guide.finish_effect",effects(values))
    }
}
