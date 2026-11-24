package com.bareminimumstudios.temperandtang.smithing
import kotlin.test.*
class CraftRulesTest {
    @Test fun `quality is randomized but accuracy and mastery dominate its distribution`() {
        val rng=java.util.Random(144L)
        var poor=0.0;var good=0.0;var master=0.0;var masterworks=0
        repeat(20000) {
            val roll=rng.nextDouble()
            val p=CraftRules.roll(0.1,0,roll);val g=CraftRules.roll(0.95,0,roll);val m=CraftRules.roll(0.95,20,roll)
            assertTrue(p.grade.ordinal<=1);assertTrue(g.grade.ordinal<=m.grade.ordinal)
            poor+=p.grade.ordinal;good+=g.grade.ordinal;master+=m.grade.ordinal
            if(m.grade==CraftGrade.MASTERWORK)masterworks++
        }
        assertTrue(poor<good && good<master);assertTrue(masterworks in 100..10000)
        assertEquals(CraftGrade.ROUGH,CraftRules.roll(Double.NaN,0,Double.NaN).grade)
    }
    @Test fun `five ordered grades keep rough usable and masterwork requires strong workmanship`() {
        assertEquals(5,CraftGrade.entries.size)
        assertEquals(CraftGrade.MASTERWORK,CraftGrade.parse("EXCEPTIONAL"))
        assertNull(CraftGrade.parse("unknown"))
        for((low,high) in CraftGrade.entries.zipWithNext()) {
            assertTrue(low.durability<high.durability && low.damage<high.damage)
            assertTrue(low.speed<high.speed && low.mining<high.mining)
        }
        assertTrue(CraftGrade.ROUGH.durability>=-.25 && CraftGrade.ROUGH.damage>=-.15)
        repeat(101) {i->assertTrue(CraftRules.roll(.70,20,i/100.0).grade!=CraftGrade.MASTERWORK)}
        assertTrue(CraftRules.roll(1.0,20,.99).grade==CraftGrade.MASTERWORK)
    }
    @Test fun `missed and spammed strikes cannot produce free progress or perfect quality`() {
        val work=HammerWork(3,20,3,4){0}
        work.arm(100);val first=work.target
        assertEquals(1.0,work.strike(first));assertNull(work.strike(first));assertNull(work.strike(first-1))
        assertTrue(work.tick(work.target+30));assertEquals(1,work.hits);assertEquals(1,work.misses)
        assertEquals(1.0,work.strike(work.target));assertEquals(1.0,work.strike(work.target))
        assertTrue(work.ready);assertEquals(0.75,work.accuracy);assertNull(work.strike(work.target+100))
    }
    @Test fun `waiting before the first blow is free and custom cooldowns leave reachable targets`() {
        val work=HammerWork(3,16,3,0){0}
        work.arm(100)
        repeat(4) {work.tick(work.target+30)}
        assertEquals(0,work.misses)
        val first=work.target
        assertEquals(1.0,work.strike(first,40))
        assertTrue(work.target-first>=40)
        assertNull(work.strike(first+39,40))
        assertEquals(1.0,work.strike(work.target,40))
    }
    @Test fun `mastery grows with successful work and reforging gives less experience`() {
        assertEquals(0,CraftRules.level(0));assertEquals(1,CraftRules.level(25));assertEquals(20,CraftRules.level(Long.MAX_VALUE))
        assertTrue(CraftRules.earnedXp(1.0,false)>CraftRules.earnedXp(1.0,true))
    }
}
