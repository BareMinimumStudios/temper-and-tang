package com.bareminimumstudios.temperandtang.mixin
import com.bareminimumstudios.temperandtang.compat.WorkshopRecipes
import com.google.gson.JsonElement
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
import com.llamalad7.mixinextras.injector.wrapoperation.Operation
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.RecipeManager
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At

@Mixin(RecipeManager::class)
abstract class RecipeManagerMixin {
    // Read the final JSON map after HEAD modifiers (including pack recipe editors), just before parsing.
    @WrapOperation(method = ["apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V"],
        at = [At(value = "INVOKE", target = "Ljava/util/Map;entrySet()Ljava/util/Set;")])
    private fun temperRecipes(recipes: Map<ResourceLocation, JsonElement>, original: Operation<Set<Map.Entry<ResourceLocation,JsonElement>>>):Set<Map.Entry<ResourceLocation,JsonElement>> =
        original.call(WorkshopRecipes.apply(recipes))
}
