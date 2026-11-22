package com.bareminimumstudios.temperandtang.smithing
import kotlin.test.*
class WorkProgressTest {
    @Test fun `duplicate packets and backwards ticks cannot speed up work`() {
        val w = WorkProgress()
        assertTrue(w.strike(20, 8, 3))
        repeat(20) { assertFalse(w.strike(20, 8, 3)) }
        assertFalse(w.strike(19, 8, 3))
        assertTrue(w.strike(28, 8, 3))
        assertTrue(w.strike(36, 8, 3))
        assertTrue(w.ready(3))
        assertFalse(w.strike(44, 8, 3))
        w.reset()
        assertFalse(w.ready(3))
        assertTrue(w.strike(44, 8, 3))
    }
}
