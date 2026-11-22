package com.bareminimumstudios.temperandtang.smithing

import kotlin.math.roundToInt

enum class Finish(val key: String) {
    TEMPERED("tempered"), KEEN("keen"), WEIGHTED("weighted"), LIGHTENED("lightened");
    companion object { fun fromKey(key: String): Finish? = entries.firstOrNull { it.key == key } }
}

data class FinishBalance(
    val temperedDurability: Double = 0.15,
    val keenDamage: Double = 0.05,
    val keenDurabilityCost: Double = 0.10,
    val weightedDamage: Double = 0.10,
    val weightedSpeedCost: Double = 0.05,
    val lightenedSpeed: Double = 0.05,
    val lightenedDurabilityCost: Double = 0.10,
)

data class FinishEffect(val damage: Double = 0.0, val speed: Double = 0.0, val durability: Double = 0.0)

object FinishRules {
    fun effect(finish: Finish, balance: FinishBalance = FinishBalance()): FinishEffect {
        fun safe(value: Double) = if (value.isFinite()) value.coerceIn(0.0, 1.0) else 0.0
        return when (finish) {
            Finish.TEMPERED -> FinishEffect(durability = safe(balance.temperedDurability))
            Finish.KEEN -> FinishEffect(damage = safe(balance.keenDamage), durability = -safe(balance.keenDurabilityCost))
            Finish.WEIGHTED -> FinishEffect(damage = safe(balance.weightedDamage), speed = -safe(balance.weightedSpeedCost))
            Finish.LIGHTENED -> FinishEffect(speed = safe(balance.lightenedSpeed), durability = -safe(balance.lightenedDurabilityCost))
        }
    }
    fun maximumDurability(base: Int, effect: FinishEffect): Int =
        (base.toDouble().coerceAtLeast(1.0) * (1.0 + effect.durability.coerceIn(-0.9, 1.0)))
            .coerceIn(1.0, Int.MAX_VALUE.toDouble()).roundToInt()
}
