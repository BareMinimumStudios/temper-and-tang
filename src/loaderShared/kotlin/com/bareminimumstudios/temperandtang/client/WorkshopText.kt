package com.bareminimumstudios.temperandtang.client

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

/** Restrained workshop palette: meaning is still conveyed by words, not color alone. */
object WorkshopText {
    const val STAGE=0xD7B16A
    const val QUANTITY=0x8EBCC3
    const val BENEFIT=0x91C88E
    const val COST=0xD28A7E
    const val TEXT=0xE4E2DB
    const val MUTED=0xB2AEA4
    fun style(text:Component,color:Int,bold:Boolean=false):MutableComponent=text.copy().withStyle {it.withColor(color).withBold(bold)}
    fun number(value:Number)=style(Component.literal(value.toString()),QUANTITY)
    fun accuracy(value:Int)=style(Component.literal(value.toString()),when {value>=75->BENEFIT;value>=40->STAGE;else->COST})
}
