package com.bareminimumstudios.temperandtang.smithing

import kotlin.math.exp

/** One time scale for every incoming strike; changing the active note never moves its successors. */
object RhythmTrack {
    const val HORIZON=60f
    const val TARGET=4f
    const val SPEED=12f/HORIZON
    fun position(ticksUntil:Float)=TARGET-ticksUntil*SPEED
    fun bandWidth(perfectTicks:Float)=2f*perfectTicks*SPEED
    /** Follow the server clock without reversing or snapping between 20 Hz updates. */
    fun advance(rendered:Float,estimate:Float,seconds:Float):Float {
        val dt=seconds.coerceAtLeast(0f)
        val advanced=rendered-20f*dt
        val correction=((estimate-advanced)*(1f-exp(-dt*8f))).coerceIn(-6f*dt,6f*dt)
        return advanced+correction
    }
    fun visible(x:Float)=x in -6f..6f
}
