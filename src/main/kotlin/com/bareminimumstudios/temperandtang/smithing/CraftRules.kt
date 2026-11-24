package com.bareminimumstudios.temperandtang.smithing

import kotlin.math.abs
import kotlin.math.sqrt

enum class CraftGrade(val durability:Double,val damage:Double,val armor:Double,val toughness:Double,val speed:Double,val mining:Double) {
    ROUGH(-0.20,-0.10,-0.10,0.0,-0.04,-0.12), SERVICEABLE(0.0,0.0,0.0,0.0,0.0,0.0),
    FINE(0.15,0.06,0.05,0.0,0.02,0.08), SUPERIOR(0.30,0.12,0.10,0.08,0.04,0.16),
    MASTERWORK(0.50,0.20,0.16,0.15,0.06,0.25);
    val key get()=name.lowercase(java.util.Locale.ROOT)
    companion object {
        /** Existing exceptional pieces migrate to the highest of the five grades. */
        fun parse(value:String)=if(value=="EXCEPTIONAL") MASTERWORK else entries.firstOrNull {it.name==value}
    }
}

data class CraftRoll(val grade:CraftGrade,val accuracy:Double,val effective:Double,val luck:Double,val mastery:Int)
object CraftRules {
    fun roll(accuracy:Double,mastery:Int,random:Double,luckWeight:Double=0.20,masteryWeight:Double=0.10,maxLevel:Int=20):CraftRoll {
        val skill=accuracy.takeIf {it.isFinite()}?.coerceIn(0.0,1.0)?:0.0
        val luck=random.takeIf {it.isFinite()}?.coerceIn(0.0,Math.nextDown(1.0))?:0.0
        val lw=luckWeight.coerceIn(0.0,0.35);val mw=masteryWeight.coerceIn(0.0,0.20)
        val level=mastery.coerceIn(0,maxLevel.coerceAtLeast(1))
        val effective=skill*(1-lw-mw)+luck*lw+level.toDouble()/maxLevel.coerceAtLeast(1)*mw
        val index=when {effective<0.22->0;effective<0.48->1;effective<0.72->2;effective<0.90->3;else->4}
        return CraftRoll(CraftGrade.entries[index],skill,effective,luck,level)
    }
    fun level(xp:Long,maxLevel:Int=20)=sqrt(xp.coerceAtLeast(0).toDouble()/25.0).toInt().coerceIn(0,maxLevel.coerceAtLeast(1))
    fun nextLevelXp(level:Int)=25L*(level.coerceAtLeast(0)+1)*(level.coerceAtLeast(0)+1)
    fun earnedXp(accuracy:Double,reforge:Boolean)=if(reforge) 1+(accuracy.coerceIn(0.0,1.0)*4).toInt() else 10+(accuracy.coerceIn(0.0,1.0)*20).toInt()
}

/** Server-timed rhythm phrases. Future notes stay fixed even if a preceding strike is early or late. */
class HammerWork(private val required:Int,private val period:Int,private val baseWindow:Int,private val jitter:Int,private val random:(Int)->Int) {
    val requiredBlows get()=required.coerceIn(1,40)
    var target=0L;private set
    var window=3;private set
    private var precision=0
    val perfectWindow get()=if(precision>0)precision else ((window+1)/2).coerceAtLeast(1)
    var hits=0;private set
    var misses=0;private set
    var perfect=0;private set
    var combo=0;private set
    var lastScore=0.0;private set
    var lastOffset=0;private set
    private var points=0.0
    private var lastHit=Long.MIN_VALUE
    private var minimumGap=8
    private var phraseChance=0
    private var tripleChance=0
    private var burstGap=12
    private var notes=longArrayOf()
    private var noteIndex=0
    private var spacing=12
    val phraseSize get()=notes.size.coerceAtLeast(1)
    val phraseStep get()=noteIndex+1
    val nextGap get()=if(noteIndex+1<notes.size)(notes[noteIndex+1]-target).toInt()else 0
    val tempo get()=if(noteIndex>0)spacing else period.coerceIn(12,80)
    var armed=false;private set
    val ready get()=hits>=requiredBlows
    val accuracy get()=(points/(hits+misses).coerceAtLeast(1)).coerceIn(0.0,1.0)
    fun configureRhythm(chance:Int,triples:Int,gap:Int,perfectTicks:Int,minGap:Int):HammerWork {
        check(!armed)
        phraseChance=chance.coerceIn(0,100);tripleChance=triples.coerceIn(0,100);burstGap=gap.coerceIn(6,80)
        precision=perfectTicks.coerceIn(1,4);minimumGap=minGap.coerceIn(1,40)
        return this
    }
    fun arm(now:Long) {if(!armed && !ready){armed=true;newPhrase(now)}}
    private fun newPhrase(now:Long) {
        val remaining=(requiredBlows-hits).coerceAtLeast(1)
        val size=if(remaining>=2 && phraseChance>0 && random(100)<phraseChance) {if(remaining>=3 && tripleChance>0 && random(100)<tripleChance)3 else 2}else 1
        val span=jitter.coerceIn(0,12)
        window=(baseWindow+random(3)-1).coerceIn(perfectWindow.coerceAtMost(8),8)
        spacing=maxOf(burstGap,minimumGap+perfectWindow*2)
        val lead=(period.coerceIn(12,80)+random(span*2+1)-span).coerceAtLeast(maxOf(12,minimumGap+perfectWindow*2))
        notes=LongArray(size){now+lead+it*spacing};noteIndex=0;target=notes[0]
    }
    private fun advance(now:Long) {
        if(noteIndex+1<notes.size){noteIndex++;target=notes[noteIndex]}else newPhrase(now)
    }
    fun tick(now:Long):Boolean {
        if(!armed || ready)return false
        var changed=false
        // At most one short phrase can expire before a fresh, readable lead-in is scheduled.
        repeat(4) {
            val lateLimit=if(noteIndex+1<notes.size)minOf(window*3L,spacing/2L)else window*3L
            if(now<=target+lateLimit)return changed
            if(hits>0)misses=(misses+1).coerceAtMost(10000)
            lastScore=0.0;combo=0;advance(now);changed=true
        }
        return changed
    }
    fun strike(now:Long,minGap:Int=8):Double? {
        if(!armed || ready || now<0 || lastHit!=Long.MIN_VALUE && (now<lastHit || now-lastHit<minGap.coerceAtLeast(1)))return null
        tick(now)
        val delta=abs(now-target)
        val score=when {delta<=perfectWindow.toLong()->1.0;delta<=window->.75;delta<=window*2L->.40;else->.10}
        lastOffset=(now-target).coerceIn(-120,120).toInt()
        minimumGap=minGap.coerceIn(1,40)
        lastHit=now;lastScore=score;hits++;points+=score;if(score==1.0)perfect++
        combo=if(score>=.75)combo+1 else 0
        if(!ready)advance(now)
        return score
    }
    fun phase(now:Long):Float=((now-target).toFloat()/tempo+.5f).coerceIn(0f,1f)
}
