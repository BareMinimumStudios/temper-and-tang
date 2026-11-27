package com.bareminimumstudios.temperandtang.smithing

import java.util.Locale


enum class ForgeTrait(val durability:Double,val damage:Double,val speed:Double,val armor:Double,val movement:Double,val mining:Double) {
    NONE(0.0,0.0,0.0,0.0,0.0,0.0),
    STURDY(.10,-.02,-.01,0.0,-.001,-.02),
    HONED(-.06,.04,0.0,0.0,0.0,0.0),
    BALANCED(-.03,-.01,.03,0.0,0.0,.02),
    REINFORCED(.04,0.0,0.0,.035,-.003,0.0),
    SUPPLE(-.05,0.0,0.0,0.0,.004,0.0),
    INDUSTRIOUS(-.06,0.0,-.01,0.0,0.0,.06);
    val key get()=name.lowercase(Locale.ROOT)
    companion object {fun parse(key:String)=entries.firstOrNull {it.key==key}?:NONE}
}
enum class ForgeFamily {WEAPON,TOOL,ARMOR,SHIELD}
object ForgeTraitRules {
    fun chance(accuracy:Double):Double {
        val skill=accuracy.takeIf {it.isFinite()}?.coerceIn(0.0,1.0)?:0.0
        return if(skill<.4)0.0 else .25+.55*skill
    }
    fun roll(accuracy:Double,material:ForgeMaterial,family:ForgeFamily,random:Double):ForgeTrait {
        val chance=chance(accuracy)
        val r=random.takeIf {it.isFinite()}?.coerceIn(0.0,Math.nextDown(1.0))?:1.0
        if(chance<=0 || r>=chance)return ForgeTrait.NONE
        val pool=when(family) {
            ForgeFamily.SHIELD->listOf(ForgeTrait.STURDY)
            ForgeFamily.ARMOR->listOf(ForgeTrait.STURDY,ForgeTrait.REINFORCED,ForgeTrait.SUPPLE,if(material==ForgeMaterial.GOLD)ForgeTrait.SUPPLE else ForgeTrait.REINFORCED)
            ForgeFamily.TOOL->listOf(ForgeTrait.STURDY,ForgeTrait.INDUSTRIOUS,ForgeTrait.BALANCED,if(material==ForgeMaterial.GOLD)ForgeTrait.BALANCED else ForgeTrait.STURDY)
            ForgeFamily.WEAPON->listOf(ForgeTrait.STURDY,ForgeTrait.HONED,ForgeTrait.BALANCED,when(material){ForgeMaterial.STEEL->ForgeTrait.HONED;ForgeMaterial.GOLD,ForgeMaterial.COPPER,ForgeMaterial.TIN->ForgeTrait.BALANCED;else->ForgeTrait.STURDY})
        }
        return pool[((r/chance)*pool.size).toInt().coerceIn(pool.indices)]
    }
}
