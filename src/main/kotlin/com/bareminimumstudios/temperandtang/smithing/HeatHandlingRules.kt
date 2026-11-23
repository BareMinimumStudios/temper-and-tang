package com.bareminimumstudios.temperandtang.smithing

object HeatHandlingRules {
    fun due(now:Long,last:Long,interval:Int):Boolean = last==Long.MIN_VALUE || now>=last && now-last>=interval.coerceAtLeast(1)
}
