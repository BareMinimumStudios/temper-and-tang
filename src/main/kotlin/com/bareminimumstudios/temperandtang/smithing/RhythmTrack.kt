package com.bareminimumstudios.temperandtang.smithing

/** One time scale for every incoming strike; changing the active note never moves its successors. */
object RhythmTrack {
    const val HORIZON=60f
    const val TARGET=4f
    const val SPEED=12f/HORIZON
    fun position(ticksUntil:Float)=TARGET-ticksUntil*SPEED
    fun bandWidth(perfectTicks:Float)=2f*perfectTicks*SPEED
    fun visible(x:Float)=x in -6f..6f
}
