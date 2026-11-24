package com.bareminimumstudios.temperandtang.smithing
import kotlin.math.roundToInt

/** A bounded one-way estimate from server-measured round-trip ping, never a client-supplied strike time. */
object RhythmLatency {
    fun ticks(roundTripMillis:Int,maximum:Int=3)=(roundTripMillis.coerceAtLeast(0)/100.0).roundToInt().coerceIn(0,maximum.coerceIn(0,6))
    fun evaluationTick(arrivalTick:Long,roundTripMillis:Int,maximum:Int=3)=(arrivalTick-ticks(roundTripMillis,maximum)).coerceAtLeast(0)
}
