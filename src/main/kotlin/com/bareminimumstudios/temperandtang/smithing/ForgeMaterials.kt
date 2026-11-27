package com.bareminimumstudios.temperandtang.smithing

import java.util.Locale


/** Small workshop-specific material differences, separate from the item's original tier and craftsmanship. */
enum class ForgeMaterial(val durability:Double,val damage:Double,val speed:Double,val armor:Double,val movement:Double) {
    NEUTRAL(0.0,0.0,0.0,0.0,0.0),
    IRON(0.0,0.0,0.0,0.0,0.0),
    BRONZE(.10,-.03,0.0,0.0,0.0),
    STEEL(.12,.06,-.04,.03,-.002),
    GOLD(-.12,0.0,.06,-.02,.002),
    COPPER(-.08,0.0,.05,-.02,.001),
    TIN(-.10,0.0,.03,-.03,.001),
    SILVER(0.0,0.0,0.0,0.0,0.0),
    DIAMOND(.06,0.0,-.02,0.0,-.001),
    NETHERITE(.08,0.0,-.03,.02,-.002);
    val key get()=name.lowercase(Locale.ROOT)
    companion object {
        fun parse(key:String)=entries.firstOrNull {it.key==key}?:NEUTRAL
    }
}
