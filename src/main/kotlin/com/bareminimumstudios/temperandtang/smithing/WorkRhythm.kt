package com.bareminimumstudios.temperandtang.smithing

import kotlin.math.roundToInt

data class WorkRhythm(val period:Int,val window:Int,val perfect:Int,val jitter:Int,val doubles:Int,val triples:Int,val gap:Int,val extraStrikes:Int,val key:String)
object RhythmRules {
    fun extraStrikes(material:ForgeMaterial)=when(material){ForgeMaterial.STEEL->1;ForgeMaterial.DIAMOND,ForgeMaterial.NETHERITE->2;else->0}
    fun profile(material:ForgeMaterial,period:Int,window:Int,jitter:Int,materialDifficulty:Boolean=true):WorkRhythm {
        if(!materialDifficulty)return WorkRhythm(period.coerceIn(12,80),window.coerceIn(1,8),((window+1)/2).coerceIn(1,4),jitter.coerceIn(0,12),30,10,12,0,"steady")
        val tier=when(material){ForgeMaterial.GOLD,ForgeMaterial.COPPER,ForgeMaterial.TIN,ForgeMaterial.SILVER->0;ForgeMaterial.BRONZE->1;ForgeMaterial.IRON,ForgeMaterial.NEUTRAL->2;ForgeMaterial.STEEL->3;else->4}
        val multiplier=listOf(1.1,1.05,.9,.8,.7)[tier]
        val good=(window+when(tier){0->1;1->0;2,3->-1;else->-2}).coerceIn(1,8)
        val perfect=((good+1)/2).coerceIn(1,4)
        return WorkRhythm((period*multiplier).roundToInt().coerceIn(12,80),good,perfect,(jitter+(tier-1).coerceAtLeast(0)).coerceIn(0,12),listOf(20,25,35,50,65)[tier],listOf(0,0,15,30,40)[tier],if(tier<2)12 else 10,extraStrikes(material),when(tier){0,1->"forgiving";2->"steady";3->"demanding";else->"expert"})
    }
    /** The band covers exactly the ±perfect-tick range around the target, in scene pixels. */
    fun bandWidth(period:Int,perfect:Int)=24f*perfect.coerceAtLeast(1)/period.coerceAtLeast(1)
}
