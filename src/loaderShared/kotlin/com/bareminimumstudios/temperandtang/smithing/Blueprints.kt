package com.bareminimumstudios.temperandtang.smithing
import com.bareminimumstudios.temperandtang.content.TemperContent
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData

object Blueprints {
    fun blank(stack:ItemStack)=!stack.isEmpty && stack.item==TemperContent.item("blank_blueprint")
    fun filled(stack:ItemStack)=!stack.isEmpty && stack.item==TemperContent.item("smithing_blueprint")
    fun pattern(stack:ItemStack)=if(filled(stack)) stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getString("temperandtang_pattern") else ""
    fun make(id:String,name:Component)=ItemStack(TemperContent.item("smithing_blueprint")).also {
        it.set(DataComponents.CUSTOM_DATA,CustomData.of(CompoundTag().also {tag->tag.putString("temperandtang_pattern",id)}))
        it.set(DataComponents.ITEM_NAME,Component.translatable("item.temperandtang.blueprint_name",name))
    }
}
