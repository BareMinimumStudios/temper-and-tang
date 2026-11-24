package com.bareminimumstudios.temperandtang.compat

import com.bareminimumstudios.temperandtang.TemperAndTang
import com.google.gson.*
import net.minecraft.resources.ResourceLocation

/** Transform only recipes actually present after datapacks; never restore a removed native pattern. */
object EquipmentProgression {
    private fun ingredient(id:String)=JsonObject().also {it.addProperty("item",id)}
    private fun upgrade(base:String,output:JsonObject,steel:Boolean)=JsonObject().also {
        it.addProperty("type",if(steel)"temperandtang:steel_upgrade" else "minecraft:smithing_transform")
        it.add("template",ingredient("temperandtang:diamond_upgrade_smithing_template"))
        it.add("base",ingredient(base));it.add("addition",ingredient("minecraft:diamond"));it.add("result",output.deepCopy())
    }
    fun apply(recipes:MutableMap<ResourceLocation,JsonElement>) {
        if(!TemperAndTang.config.diamondUpgrades.get())return
        for((id,raw) in recipes.toMap()) {
            val recipe=raw.takeIf {it.isJsonObject}?.asJsonObject?:continue
            if(recipe.get("type")?.asString !in listOf("minecraft:crafting_shaped","minecraft:crafting_shapeless","temperandtang:shaping","temperandtang:shaping_shapeless"))continue
            val output=recipe.get("result")?.takeIf {it.isJsonObject}?.asJsonObject?:continue
            val item=ResourceLocation.tryParse(output.get("id")?.asString?:continue)?:continue
            if(item.toString()=="minecraft:netherite_ingot" && recipe.toString().contains("minecraft:netherite_scrap")) {recipes.remove(id);continue}
            if((output.get("count")?.asInt?:1)!=1)continue
            if(item.namespace=="minecraft" && item.path in listOf("diamond_sword","diamond_pickaxe","diamond_axe","diamond_shovel","diamond_hoe","diamond_helmet","diamond_chestplate","diamond_leggings","diamond_boots")) {
                recipes[id]=upgrade("temperandtang:steel_"+item.path.removePrefix("diamond_"),output,false)
            } else if(item.namespace in setOf("simplyswords","simplymore","magistuarmory") && item.path.startsWith("diamond_") && enabled(item.namespace)) {
                val base=ResourceLocation.fromNamespaceAndPath(item.namespace,(if(item.namespace=="magistuarmory")"steel_"else"iron_")+item.path.removePrefix("diamond_"))
                if(net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(base))recipes[id]=upgrade(base.toString(),output,item.namespace!="magistuarmory")
            }
            if(item.namespace in setOf("simplyswords","simplymore") && item.path.startsWith("iron_") && enabled(item.namespace)) {
                val copy=recipe.deepCopy();var replaced=false
                fun steel(e:JsonElement):JsonElement {
                    if(e.isJsonArray)return JsonArray().also {out->e.asJsonArray.forEach {out.add(steel(it))}}
                    if(!e.isJsonObject)return e.deepCopy()
                    val o=e.asJsonObject
                    return if(o.get("item")?.asString=="minecraft:iron_ingot" || o.get("tag")?.asString=="c:ingots/iron") {
                        replaced=true;JsonObject().also {it.addProperty("tag","c:ingots/steel")}
                    } else o.deepCopy()
                }
                copy.getAsJsonObject("key")?.let {key->for((name,value) in key.entrySet().toList())key.add(name,steel(value))}
                copy.getAsJsonArray("ingredients")?.let {copy.add("ingredients",steel(it))}
                if(replaced) {
                    copy.addProperty("type",if(recipe.has("key"))"temperandtang:shaping" else "temperandtang:shaping_shapeless")
                    recipes[TemperAndTang.id("steel_core/"+id.namespace+"/"+id.path)]=copy
                }
            }
        }
    }
    private fun enabled(namespace:String)=when(namespace){"simplyswords"->TemperAndTang.config.simplySwords.get();"magistuarmory"->TemperAndTang.config.epicKnights.get();else->TemperAndTang.config.simplyMore.get()}
}
