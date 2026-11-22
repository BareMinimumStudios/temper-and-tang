package com.bareminimumstudios.temperandtang.compat
import com.bareminimumstudios.temperandtang.TemperAndTang
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.minecraft.resources.ResourceLocation

/** Preserve patterns and upgrades; only adapt untouched basic iron/gold grid recipes. */
object OvergearedRecipes {
    fun apply(source: Map<ResourceLocation, JsonElement>): Map<ResourceLocation, JsonElement> {
        // Some upstream NeoForge recipes still use Fabric/legacy Forge conditions.
        // Honor explicit absent-mod guards, scoped to Simply Swords, before parsing.
        val result = source.filterNot { (id, element) ->
            id.namespace == "simplyswords" && element.isJsonObject &&
                element.asJsonObject.getAsJsonArray("fabric:load_conditions")?.any { raw ->
                    raw.isJsonObject && raw.asJsonObject.get("condition")?.asString == "fabric:all_mods_loaded" &&
                        raw.asJsonObject.getAsJsonArray("values")?.any { !TemperAndTang.modLoaded(it.asString) } == true
                } == true
        }.toMutableMap()
        if (!TemperAndTang.modLoaded("overgeared") || !TemperAndTang.config.overgearedRecipes.get() || !TemperAndTang.config.simplySwords.get()) return result
        var changed = 0
        for ((id, element) in result.toMap()) {
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
            forge.add("blueprint", com.google.gson.JsonArray().also { it.add("sword") })
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
