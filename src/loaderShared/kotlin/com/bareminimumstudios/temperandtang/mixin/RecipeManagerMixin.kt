package com.bareminimumstudios.temperandtang.mixin
import com.bareminimumstudios.temperandtang.compat.WorkshopRecipes
import com.google.gson.JsonElement
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod
import com.llamalad7.mixinextras.injector.wrapoperation.Operation
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.util.profiling.ProfilerFiller
import net.minecraft.world.item.crafting.RecipeManager
import org.spongepowered.asm.mixin.Mixin

@Mixin(RecipeManager::class)
abstract class RecipeManagerMixin {
    @WrapMethod(method = ["apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V"])
    private fun temperRecipes(recipes: Map<ResourceLocation, JsonElement>, resources: ResourceManager,
                              profiler: ProfilerFiller, original: Operation<Void>) {
        original.call(WorkshopRecipes.apply(recipes), resources, profiler)
    }
}
