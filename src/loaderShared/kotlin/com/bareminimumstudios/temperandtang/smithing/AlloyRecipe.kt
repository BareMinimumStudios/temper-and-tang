package com.bareminimumstudios.temperandtang.smithing

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.HolderLookup
import net.minecraft.core.NonNullList
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.*
import net.minecraft.world.level.Level

data class AlloyInput(val primary:ItemStack,val additive:ItemStack):RecipeInput {
    override fun size()=2
    override fun getItem(index:Int)=when(index){0->primary;1->additive;else->throw IndexOutOfBoundsException(index)}
}

/** Counted common-tag ingredients; no item replacement or hard dependency on another content mod. */
class AlloyRecipe(val primary:Ingredient,val primaryCount:Int,val additive:Ingredient,val additiveCount:Int,val result:ItemStack,val cookingTime:Int,val requiresAdvanced:Boolean=false):Recipe<AlloyInput> {
    override fun matches(input:AlloyInput,level:Level)=primary.test(input.primary) && additive.test(input.additive) && input.primary.count>=primaryCount && input.additive.count>=additiveCount
    override fun assemble(input:AlloyInput,provider:HolderLookup.Provider)=result.copy()
    override fun canCraftInDimensions(width:Int,height:Int)=width*height>=2
    override fun getResultItem(provider:HolderLookup.Provider)=result
    override fun getIngredients()=NonNullList.of(Ingredient.EMPTY,primary,additive)
    override fun getSerializer()=AlloyRecipes.SERIALIZER
    override fun getType()=AlloyRecipes.TYPE
    override fun isSpecial()=true
}

object AlloyRecipes {
    val TYPE:RecipeType<AlloyRecipe> = object:RecipeType<AlloyRecipe>{override fun toString()="temperandtang:alloying"}
    val SERIALIZER:RecipeSerializer<AlloyRecipe> = object:RecipeSerializer<AlloyRecipe> {
        override fun codec():MapCodec<AlloyRecipe> = RecordCodecBuilder.mapCodec {i->i.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("primary").forGetter(AlloyRecipe::primary),
            Codec.intRange(1,8).fieldOf("primary_count").forGetter(AlloyRecipe::primaryCount),
            Ingredient.CODEC_NONEMPTY.fieldOf("additive").forGetter(AlloyRecipe::additive),
            Codec.intRange(1,8).fieldOf("additive_count").forGetter(AlloyRecipe::additiveCount),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(AlloyRecipe::result),
            Codec.intRange(1,12000).optionalFieldOf("cookingtime",200).forGetter(AlloyRecipe::cookingTime),
            Codec.BOOL.optionalFieldOf("requires_advanced_furnace",false).forGetter(AlloyRecipe::requiresAdvanced)
        ).apply(i,::AlloyRecipe)}
        override fun streamCodec():StreamCodec<RegistryFriendlyByteBuf,AlloyRecipe> = StreamCodec.of({b,r->
            Ingredient.CONTENTS_STREAM_CODEC.encode(b,r.primary);b.writeVarInt(r.primaryCount)
            Ingredient.CONTENTS_STREAM_CODEC.encode(b,r.additive);b.writeVarInt(r.additiveCount)
            ItemStack.STREAM_CODEC.encode(b,r.result);b.writeVarInt(r.cookingTime);b.writeBoolean(r.requiresAdvanced)
        },{b->AlloyRecipe(Ingredient.CONTENTS_STREAM_CODEC.decode(b),b.readVarInt(),Ingredient.CONTENTS_STREAM_CODEC.decode(b),b.readVarInt(),ItemStack.STREAM_CODEC.decode(b),b.readVarInt(),b.readBoolean())})
    }
}
