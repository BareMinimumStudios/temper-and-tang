package com.bareminimumstudios.temperandtang.smithing
import kotlin.test.*
class RhythmLatencyTest {
    @Test fun `ping allowance is bounded and never accepts client timestamps`() {
        assertEquals(0,RhythmLatency.ticks(-10));assertEquals(0,RhythmLatency.ticks(0))
        assertEquals(1,RhythmLatency.ticks(100));assertEquals(2,RhythmLatency.ticks(200))
        assertEquals(3,RhythmLatency.ticks(10000));assertEquals(0,RhythmLatency.ticks(300,0))
        assertEquals(0L,RhythmLatency.evaluationTick(1,300))
    }
    @Test fun `compensated perfects still enforce real server arrival cooldown`() {
        val work=HammerWork(3,20,1,0){0};work.arm(100)
        val first=work.target
        assertEquals(1.0,work.strike(RhythmLatency.evaluationTick(first+3,300),8,first+3))
        val second=work.target
        assertNull(work.strike(second,8,first+7))
        assertEquals(1.0,work.strike(second,8,second+3))
        assertNull(work.strike(work.target,8,second+4))
        assertEquals(1.0,work.strike(work.target,8,work.target+3))
        assertEquals(1.0,work.accuracy);assertTrue(work.ready)
    }
}
