package com.bareminimumstudios.temperandtang.smithing
import kotlin.test.*
class RhythmRulesTest {
    @Test fun `hard materials demand tighter faster strikes`() {
        val bronze=RhythmRules.profile(ForgeMaterial.BRONZE,20,3,3)
        val iron=RhythmRules.profile(ForgeMaterial.IRON,20,3,3)
        val steel=RhythmRules.profile(ForgeMaterial.STEEL,20,3,3)
        val diamond=RhythmRules.profile(ForgeMaterial.DIAMOND,20,3,3)
        assertTrue(bronze.period>iron.period && iron.period>steel.period && steel.period>diamond.period)
        assertTrue(bronze.perfect>iron.perfect && diamond.window<=iron.window)
        assertTrue(bronze.doubles<steel.doubles && steel.doubles<diamond.doubles)
        assertEquals(24f*iron.perfect/iron.period,RhythmRules.bandWidth(iron.period,iron.perfect))
    }
    @Test fun `double and triple notes remain fixed after early input`() {
        val work=HammerWork(6,24,3,0){0}.configureRhythm(100,100,12,1,8)
        work.arm(100)
        assertEquals(3,work.phraseSize)
        val first=work.target
        work.strike(first-1)
        assertEquals(first+12,work.target)
        assertEquals(2,work.phraseStep)
        assertNull(work.strike(first+6))
        work.strike(first+12)
        assertEquals(first+24,work.target)
        assertEquals(2,work.combo)
        work.strike(first+24)
        assertTrue(work.target>=first+48)
    }
    @Test fun `expired burst notes recover and preparation has no penalty`() {
        val work=HammerWork(5,20,3,0){0}.configureRhythm(100,100,12,1,8)
        work.arm(0);work.tick(1000)
        assertEquals(0,work.misses)
        work.strike(work.target)
        val target=work.target
        work.tick(target+7)
        assertEquals(1,work.misses)
        assertEquals(0,work.combo)
        assertEquals(target+12,work.target)
        assertEquals(1.0,work.strike(work.target))
    }
}
