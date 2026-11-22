package com.bareminimumstudios.temperandtang.compat
import com.bareminimumstudios.temperandtang.TemperAndTang
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.minecraft.resources.ResourceLocation

/** Preserve patterns and upgrades; only adapt untouched basic iron/gold grid recipes. */
object OvergearedRecipes {
    fun apply(source: Map<ResourceLocation, JsonElement>): Map<ResourceLocation, JsonElement> {
        if (!TemperAndTang.modLoaded("overgeared") || !TemperAndTang.config.overgearedRecipes.get() || !TemperAndTang.config.simplySwords.get()) return source
        val result = source.toMutableMap()
        var changed = 0
        for ((id, element) in source) {
            if (id.namespace != "simplyswords" || !element.isJsonObject) continue
            val recipe = element.asJsonObject
            if (recipe.get("type")?.asString != "minecraft:crafting_shaped") continue
            val output = recipe.getAsJsonObject("result") ?: continue
            val item = output.get("id")?.asString ?: continue
            if (!item.startsWith("simplyswords:iron_") && !item.startsWith("simplyswords:gold_")) continue
            val key = recipe.getAsJsonObject("key") ?: continue
            if (!recipe.has("pattern")) continue
            val forge = JsonObject()
            forge.addProperty("type", "overgeared:forging")
            forge.add("pattern", recipe.get("pattern").deepCopy())
            val newKey = key.deepCopy()
            for ((_, value) in newKey.entrySet()) {
                if (value.isJsonObject && value.asJsonObject.get("item")?.asString == "minecraft:iron_ingot")
                    value.asJsonObject.addProperty("item", "overgeared:heated_iron_ingot")
            }
            forge.add("key", newKey)
            forge.add("result", output.deepCopy())
            forge.addProperty("tier", "stone")
            forge.addProperty("hammering", 4)
            forge.addProperty("has_quality", true)
            forge.addProperty("requires_blueprint", false)
            forge.addProperty("need_quenching", item.startsWith("simplyswords:iron_"))
            for (field in listOf("fabric:load_conditions", "neoforge:conditions"))
                if (recipe.has(field)) forge.add(field, recipe.get(field).deepCopy())
            result[id] = forge
            changed++
        }
        TemperAndTang.logger.info("Adapted {} basic Simply Swords recipes to Overgeared", changed)
        return result
    }
}
