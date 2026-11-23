package com.bareminimumstudios.temperandtang.compat
import com.bareminimumstudios.temperandtang.TemperAndTang
import com.bareminimumstudios.temperandtang.smithing.GearSupport
import com.google.gson.JsonElement
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack

/** Standalone recipe routing; item identities, authored patterns and load conditions remain with their owners. */
object WorkshopRecipes {
    fun apply(source:Map<ResourceLocation,JsonElement>):Map<ResourceLocation,JsonElement> {
        val result=source.filterNot {(id,element)-> id.namespace=="simplyswords" && element.isJsonObject &&
            element.asJsonObject.getAsJsonArray("fabric:load_conditions")?.any {raw->raw.isJsonObject && raw.asJsonObject.get("condition")?.asString=="fabric:all_mods_loaded" && raw.asJsonObject.getAsJsonArray("values")?.any {!TemperAndTang.modLoaded(it.asString)}==true}==true
        }.toMutableMap()
        if(!TemperAndTang.config.thermalEnabled.get() || !TemperAndTang.config.recipeIntegration.get())return result
        var count=0
        for((id,element) in result.toMap()) {
            if(!element.isJsonObject)continue
            val recipe=element.asJsonObject;val type=recipe.get("type")?.asString?:continue
            if(type !in listOf("minecraft:crafting_shaped","minecraft:crafting_shapeless"))continue
            val output=recipe.getAsJsonObject("result")?:continue
            val item=ResourceLocation.tryParse(output.get("id")?.asString?:continue)?:continue
            val stack=ItemStack(BuiltInRegistries.ITEM.get(item),output.get("count")?.asInt?:1)
            if(!GearSupport.supported(stack))continue
            val ingredients=if(recipe.has("key"))recipe.getAsJsonObject("key").entrySet().map {it.value} else recipe.getAsJsonArray("ingredients")?.toList()?:continue
            if(ingredients.none(::workable))continue
            result[id]=recipe.deepCopy().also {it.addProperty("type",if(type=="minecraft:crafting_shaped")"temperandtang:shaping" else "temperandtang:shaping_shapeless")};count++
        }
        TemperAndTang.logger.info("Routed {} equipment recipes through standalone shaping",count)
        return result
    }
    private fun workable(raw:JsonElement):Boolean {
        if(raw.isJsonArray)return raw.asJsonArray.any(::workable)
        if(!raw.isJsonObject)return false
        val obj=raw.asJsonObject
        val tag=obj.get("tag")?.asString
        if(tag!=null)return tag.startsWith("c:ingots/") || tag.startsWith("c:plates/") || tag.startsWith("c:nuggets/") || tag=="c:gems/diamond" || tag=="temperandtang:heatable_stock" || tag in setOf("magistuarmory:small_plates/steel","magistuarmory:chains/steel","magistuarmory:chainmails/steel","magistuarmory:rings/steel","magistuarmory:lamellar_rows/steel")
        val id=ResourceLocation.tryParse(obj.get("item")?.asString?:return false)?:return false
        val stack=ItemStack(BuiltInRegistries.ITEM.get(id))
        return GearSupport.supported(stack) || com.bareminimumstudios.temperandtang.smithing.WorkshopPlans.stock(stack) || id.toString() in setOf("minecraft:iron_ingot","minecraft:gold_ingot","minecraft:diamond","minecraft:netherite_ingot")
    }
}
