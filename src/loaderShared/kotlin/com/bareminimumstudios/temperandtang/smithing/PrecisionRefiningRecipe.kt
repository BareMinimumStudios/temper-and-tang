package com.bareminimumstudios.temperandtang.smithing
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.*

/** Counted, hot-stock refinement with explicit precision and recoverable failure output. */
class PrecisionRefiningRecipe(base:CraftingRecipe,val minimumAccuracy:Double,val failure:ItemStack,val strikes:Int):ShapingRecipe(base) {
    override fun getSerializer():RecipeSerializer<*> = PrecisionRefining.SERIALIZER
}
object PrecisionRefining {
    val SERIALIZER:RecipeSerializer<PrecisionRefiningRecipe> = object:RecipeSerializer<PrecisionRefiningRecipe> {
        override fun codec():MapCodec<PrecisionRefiningRecipe> = RecordCodecBuilder.mapCodec {i->i.group(
            RecipeSerializer.SHAPED_RECIPE.codec().forGetter<PrecisionRefiningRecipe> {it.base as ShapedRecipe},
            Codec.doubleRange(0.0,1.0).fieldOf("minimum_accuracy").forGetter(PrecisionRefiningRecipe::minimumAccuracy),
            ItemStack.STRICT_CODEC.fieldOf("failure_result").forGetter(PrecisionRefiningRecipe::failure),
            Codec.intRange(1,40).fieldOf("strikes").forGetter(PrecisionRefiningRecipe::strikes)
        ).apply(i,::PrecisionRefiningRecipe)}
        override fun streamCodec():StreamCodec<RegistryFriendlyByteBuf,PrecisionRefiningRecipe> = StreamCodec.of({buffer,recipe->
            RecipeSerializer.SHAPED_RECIPE.streamCodec().encode(buffer,recipe.base as ShapedRecipe)
            buffer.writeDouble(recipe.minimumAccuracy);ItemStack.STREAM_CODEC.encode(buffer,recipe.failure);buffer.writeVarInt(recipe.strikes)
        },{buffer->PrecisionRefiningRecipe(RecipeSerializer.SHAPED_RECIPE.streamCodec().decode(buffer),buffer.readDouble(),ItemStack.STREAM_CODEC.decode(buffer),buffer.readVarInt())})
    }
}
