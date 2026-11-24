package com.bareminimumstudios.temperandtang.smithing
import kotlin.test.*
class RhythmTrackTest {
    @Test fun `all notes share the target and constant travel speed`() {
        assertEquals(RhythmTrack.TARGET,RhythmTrack.position(0f))
        val current=RhythmTrack.position(6f)
        val following=RhythmTrack.position(18f)
        assertTrue(following<current)
        assertEquals(current-following,12f*RhythmTrack.SPEED,.00001f)
        assertEquals(following,RhythmTrack.position(18f))
        assertEquals(RhythmTrack.TARGET-RhythmTrack.position(2f),RhythmTrack.bandWidth(2f)/2,.00001f)
    }
    @Test fun `clock corrections keep the marker moving forward at different frame rates`() {
        for(fps in listOf(30,60,144))for(error in listOf(-8f,-2f,0f,2f,8f)) {
            val dt=1f/fps
            val next=RhythmTrack.advance(15f,15f-20f*dt+error,dt)
            assertTrue(next<15f,"Clock jitter reversed the strike marker")
            assertTrue(next>=15f-26.001f*dt && next<=15f-13.999f*dt)
        }
    }
    @Test fun `perfect phrases earn no quality bonus over equally accurate single strikes`() {
        val single=HammerWork(6,20,3,0){0}.configureRhythm(0,0,12,1,8)
        val triple=HammerWork(6,20,3,0){0}.configureRhythm(100,100,12,1,8)
        single.arm(0);triple.arm(0)
        repeat(6){single.strike(single.target);triple.strike(triple.target)}
        assertEquals(single.accuracy,triple.accuracy)
        assertEquals(CraftRules.roll(single.accuracy,0,.5),CraftRules.roll(triple.accuracy,0,.5))
        val missed=HammerWork(6,20,3,0){0}.configureRhythm(100,100,12,1,8)
        missed.arm(0);missed.strike(missed.target);missed.tick(missed.target+7)
        while(!missed.ready)missed.strike(missed.target)
        assertTrue(missed.accuracy<triple.accuracy)
        assertTrue(CraftRules.roll(missed.accuracy,0,.5).effective<CraftRules.roll(triple.accuracy,0,.5).effective)
    }
}
