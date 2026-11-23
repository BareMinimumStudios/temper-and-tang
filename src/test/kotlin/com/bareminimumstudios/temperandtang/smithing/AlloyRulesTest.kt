package com.bareminimumstudios.temperandtang.smithing
import kotlin.test.*
class AlloyRulesTest {
    @Test fun `bronze conserves metal through complete batches and leaves surplus additive`() {
        assertEquals(AlloyBatch(1,1,4),AlloyRules.batch(3,1,3,1,4))
        assertEquals(AlloyBatch(2,2,8),AlloyRules.batch(6,3,3,1,4))
        for(copper in 1..8)for(tin in 1..8)AlloyRules.batch(copper,tin,3,1,4)?.let {assertEquals(copper+it.additiveCost,it.outputCount)}
    }
    @Test fun `steel has no metal multiplication and requires its carbon additive`() {
        for(iron in 1..8)for(carbon in 1..8)AlloyRules.batch(iron,carbon,1,1,1)?.let {assertEquals(iron,it.outputCount);assertEquals(iron,it.additiveCost)}
        assertNull(AlloyRules.batch(2,1,1,1,1))
    }
    @Test fun `partial batches overflow invalid counts and excessive yields are refused`() {
        assertNull(AlloyRules.batch(4,2,3,1,4))
        assertNull(AlloyRules.batch(8,8,1,1,4))
        assertNull(AlloyRules.batch(Int.MAX_VALUE,8,1,1,1))
        assertNull(AlloyRules.batch(3,1,0,1,4))
        assertNull(AlloyRules.batch(3,1,3,1,9))
        assertNull(AlloyRules.batch(3,1,3,1,4,3))
    }
}
