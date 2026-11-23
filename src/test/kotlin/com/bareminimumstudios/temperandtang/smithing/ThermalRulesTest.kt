package com.bareminimumstudios.temperandtang.smithing
import kotlin.test.*
class ThermalRulesTest {
    @Test fun `batch heat pays whole coal units without overflow`() {
        assertEquals(800,ThermalRules.heatingTicks(100,8))
        assertEquals(1,ThermalRules.coalCost(1600))
        assertEquals(2,ThermalRules.coalCost(1601))
        assertEquals(96000,ThermalRules.heatingTicks(Int.MAX_VALUE,8))
    }
    @Test fun `clean strikes stay within the central beat window`() {
        assertFalse(ThermalRules.cleanBeat(99,100,20,3))
        assertFalse(ThermalRules.cleanBeat(100,100,20,3))
        assertTrue(ThermalRules.cleanBeat(107,100,20,3))
        assertTrue(ThermalRules.cleanBeat(113,100,20,3))
        assertFalse(ThermalRules.cleanBeat(114,100,20,3))
        assertTrue(ThermalRules.cleanBeat(130,100,20,3))
    }
}
