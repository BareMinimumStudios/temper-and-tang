package com.bareminimumstudios.temperandtang.smithing
import kotlin.test.*
class FinishRulesTest {
    @Test fun `finishes are sidegrades with bounded durability`() {
        assertEquals(115, FinishRules.maximumDurability(100, FinishRules.effect(Finish.TEMPERED)))
        val keen = FinishRules.effect(Finish.KEEN)
        assertTrue(keen.damage > 0.0 && keen.durability < 0.0)
        assertEquals(90, FinishRules.maximumDurability(100, keen))
        assertEquals(1, FinishRules.maximumDurability(0, FinishEffect(durability = -10.0)))
    }
    @Test fun `nonfinite and excessive settings cannot create infinite stats`() {
        val e = FinishRules.effect(Finish.KEEN, FinishBalance(keenDamage = Double.NaN, keenDurabilityCost = 8.0))
        assertEquals(0.0, e.damage)
        assertEquals(-1.0, e.durability)
        assertEquals(1, FinishRules.maximumDurability(10, e))
    }
    @Test fun `unknown serialized finishes do not silently become a valid finish`() {
        assertNull(Finish.fromKey("unknown"))
        Finish.entries.forEach { assertEquals(it, Finish.fromKey(it.key)) }
    }
}
