package com.bareminimumstudios.temperandtang.smithing
import kotlin.test.*
class HeatHandlingRulesTest {
    @Test fun `first contact is immediate but duplicate and backwards ticks cannot charge again`() {
        assertTrue(HeatHandlingRules.due(10,Long.MIN_VALUE,40))
        assertFalse(HeatHandlingRules.due(10,10,40))
        assertFalse(HeatHandlingRules.due(9,10,40))
        assertFalse(HeatHandlingRules.due(49,10,40))
        assertTrue(HeatHandlingRules.due(50,10,40))
    }
}
