package com.bareminimumstudios.temperandtang.smithing
import kotlin.test.*
class ForgeTraitRulesTest {
    @Test fun `poor and invalid accuracy cannot roll a trait`() {
        for(a in listOf(-1.0,0.0,.39,Double.NaN))for(i in 0..999)assertEquals(ForgeTrait.NONE,ForgeTraitRules.roll(a,ForgeMaterial.STEEL,ForgeFamily.WEAPON,i/1000.0))
        assertEquals(ForgeTrait.NONE,ForgeTraitRules.roll(1.0,ForgeMaterial.IRON,ForgeFamily.WEAPON,Double.NaN))
    }
    @Test fun `accuracy raises bounded odds without guaranteeing a modifier`() {
        assertTrue(ForgeTraitRules.chance(.5)<ForgeTraitRules.chance(.9))
        assertTrue(ForgeTraitRules.chance(1.0)<=.8)
        assertEquals(ForgeTrait.NONE,ForgeTraitRules.roll(1.0,ForgeMaterial.GOLD,ForgeFamily.WEAPON,.99))
    }
    @Test fun `families exclude irrelevant traits and materials favor different handling`() {
        val armor=(0..999).map {ForgeTraitRules.roll(.9,ForgeMaterial.IRON,ForgeFamily.ARMOR,it/1000.0)}
        assertTrue(armor.all {it in setOf(ForgeTrait.NONE,ForgeTrait.STURDY,ForgeTrait.REINFORCED,ForgeTrait.SUPPLE)})
        val tools=(0..999).map {ForgeTraitRules.roll(.9,ForgeMaterial.IRON,ForgeFamily.TOOL,it/1000.0)}
        assertTrue(ForgeTrait.INDUSTRIOUS in tools && ForgeTrait.HONED !in tools)
        fun balanced(material:ForgeMaterial)=(0..999).count {ForgeTraitRules.roll(.9,material,ForgeFamily.WEAPON,it/1000.0)==ForgeTrait.BALANCED}
        assertTrue(balanced(ForgeMaterial.GOLD)>balanced(ForgeMaterial.STEEL))
    }
}
