package com.bareminimumstudios.temperandtang.smithing

import kotlin.math.abs
import kotlin.math.sqrt

enum class CraftGrade(val durability:Double,val damage:Double,val armor:Double,val toughness:Double) {
    ROUGH(-0.25,-0.15,-0.15,0.0), SERVICEABLE(0.0,0.0,0.0,0.0),
    FINE(0.20,0.08,0.06,0.0), SUPERIOR(0.40,0.18,0.12,0.10),
    MASTERWORK(0.70,0.30,0.20,0.20), EXCEPTIONAL(1.0,0.45,0.28,0.35);
    val key get()=name.lowercase(java.util.Locale.ROOT)
}
data class CraftRoll(val grade:CraftGrade,val accuracy:Double,val effective:Double,val luck:Double,val mastery:Int)
object CraftRules {
    fun roll(accuracy:Double,mastery:Int,random:Double,luckWeight:Double=0.20,masteryWeight:Double=0.10,maxLevel:Int=20):CraftRoll {
        val skill=accuracy.takeIf {it.isFinite()}?.coerceIn(0.0,1.0)?:0.0
        val luck=random.takeIf {it.isFinite()}?.coerceIn(0.0,Math.nextDown(1.0))?:0.0
        val lw=luckWeight.coerceIn(0.0,0.35);val mw=masteryWeight.coerceIn(0.0,0.20)
        val level=mastery.coerceIn(0,maxLevel.coerceAtLeast(1))
        val effective=skill*(1-lw-mw)+luck*lw+level.toDouble()/maxLevel.coerceAtLeast(1)*mw
        val index=when {effective<0.12->0;effective<0.32->1;effective<0.52->2;effective<0.72->3;effective<0.92->4;else->5}
        return CraftRoll(CraftGrade.entries[index],skill,effective,luck,level)
    }
    fun level(xp:Long,maxLevel:Int=20)=sqrt(xp.coerceAtLeast(0).toDouble()/25.0).toInt().coerceIn(0,maxLevel.coerceAtLeast(1))
    fun nextLevelXp(level:Int)=25L*(level.coerceAtLeast(0)+1)*(level.coerceAtLeast(0)+1)
    fun earnedXp(accuracy:Double,reforge:Boolean)=if(reforge) 1+(accuracy.coerceIn(0.0,1.0)*4).toInt() else 10+(accuracy.coerceIn(0.0,1.0)*20).toInt()
}

/** Random target positions, scored only using server receipt time. Missing a beat hurts quality, never makes a free item. */
class HammerWork(private val required:Int,private val period:Int,private val baseWindow:Int,private val jitter:Int,private val random:(Int)->Int) {
    var target=0L;private set
    var window=3;private set
    val perfectWindow get()=(window/2+1).coerceAtMost(window)
    var hits=0;private set
    var misses=0;private set
    var perfect=0;private set
    var lastScore=0.0;private set
    private var points=0.0
    private var lastHit=Long.MIN_VALUE
    private var minimumGap=8
    var armed=false;private set
    val ready get()=hits>=required
    val accuracy get()=(points/(hits+misses).coerceAtLeast(1)).coerceIn(0.0,1.0)
    fun arm(now:Long) { if(!armed && !ready) {armed=true;next(now)} }
    private fun next(now:Long) {
        val span=jitter.coerceIn(0,12)
        target=now+(period.coerceIn(16,60)+random(span*2+1)-span).coerceAtLeast(maxOf(12,minimumGap))
        window=(baseWindow+random(3)-1).coerceIn(2,8)
    }
    fun tick(now:Long):Boolean {
        if(!armed || ready || now<=target+window*3L)return false
        if(hits>0)misses=(misses+1).coerceAtMost(10000)
        lastScore=0.0;next(now);return true
    }
    fun strike(now:Long,minGap:Int=8):Double? {
        if(!armed || ready || now<0 || lastHit!=Long.MIN_VALUE && (now<lastHit || now-lastHit<minGap.coerceAtLeast(1)))return null
        tick(now)
        val delta=abs(now-target)
        val score=when {delta<=perfectWindow.toLong()->1.0;delta<=window->0.75;delta<=window*2L->0.40;else->0.10}
        minimumGap=minGap.coerceIn(1,40)
        lastHit=now;lastScore=score;hits++;points+=score;if(score==1.0)perfect++
        if(!ready)next(now)
        return score
    }
    fun phase(now:Long):Float=((now-target).toFloat()/period.coerceAtLeast(1)+0.5f).coerceIn(0f,1f)
}
