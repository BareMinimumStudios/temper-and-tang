package com.bareminimumstudios.temperandtang.content
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.SmithingTemplateItem

class DiamondUpgradeTemplateItem:SmithingTemplateItem(
    Component.translatable("temperandtang.template.applies").withStyle(ChatFormatting.BLUE),
    Component.translatable("temperandtang.template.addition").withStyle(ChatFormatting.BLUE),
    Component.translatable("temperandtang.template.title").withStyle(ChatFormatting.GRAY),
    Component.translatable("temperandtang.template.base_slot"),
    Component.translatable("temperandtang.template.addition_slot"),
    listOf("empty_slot_sword","empty_slot_pickaxe","empty_slot_chestplate").map {ResourceLocation.withDefaultNamespace("item/"+it)},
    listOf(ResourceLocation.withDefaultNamespace("item/empty_slot_diamond"))
)
