package com.bareminimumstudios.temperandtang.smithing

/** Pure rules shared by the server stations and verification fixtures. */
object ThermalRules {
    fun heatingTicks(perItem: Int, count: Int): Int = (perItem.toLong().coerceAtLeast(1) * count.coerceIn(1, 8)).coerceAtMost(96000).toInt()
    fun coalCost(ticks: Int): Int = ((ticks.toLong().coerceAtLeast(1) + 1599) / 1600).toInt()
    fun cleanBeat(tick: Long, origin: Long, period: Int, window: Int): Boolean {
        if (tick < origin) return false
        val cycle = period.coerceIn(16, 60)
        val phase = ((tick - origin) % cycle).toInt()
        return kotlin.math.abs(phase - cycle / 2) <= window.coerceIn(1, 4)
    }
}
