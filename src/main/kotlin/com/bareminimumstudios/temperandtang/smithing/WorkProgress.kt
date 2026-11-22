package com.bareminimumstudios.temperandtang.smithing

/** Server ticks prevent a burst of client clicks from instantly finishing work. */
class WorkProgress {
    var strikes: Int = 0
        private set
    private var lastStrike: Long? = null
    fun strike(tick: Long, interval: Int, required: Int): Boolean {
        val previous = lastStrike
        if (previous != null && (tick < previous || tick - previous < interval.coerceAtLeast(1))) return false
        if (strikes >= required.coerceAtLeast(1)) return false
        lastStrike = tick
        strikes++
        return true
    }
    fun ready(required: Int) = strikes >= required.coerceAtLeast(1)
    fun reset() { strikes = 0; lastStrike = null }
}
