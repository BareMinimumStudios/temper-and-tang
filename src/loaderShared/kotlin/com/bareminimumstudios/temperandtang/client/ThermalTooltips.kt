package com.bareminimumstudios.temperandtang.client

import com.bareminimumstudios.temperandtang.smithing.*
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.contents.TranslatableContents
import net.minecraft.world.item.ItemStack
import net.minecraft.ChatFormatting

object ThermalTooltips {
    fun append(stack:ItemStack,lines:MutableList<Component>,details:Boolean=Screen.hasShiftDown()) {
        // Replace only our legacy lore display; leave foreign lore and saved components intact.
        lines.removeAll {(it.contents as? TranslatableContents)?.key?.startsWith("temperandtang.finish.")==true}
        val level=Minecraft.getInstance().level?:return
        if(ThermalState.hot(stack,level)) {
            val seconds=((ThermalState.data(stack).getLong("hot_until")-level.gameTime+19)/20).coerceAtLeast(0)
            lines.add(WorkshopText.style(Component.translatable("temperandtang.short.hot_item",WorkshopText.number(seconds)),WorkshopText.STAGE))
        }
        if(ThermalState.blank(stack))lines.add(Component.translatable("temperandtang.short.unquenched").withStyle(ChatFormatting.GRAY))
        if(ThermalState.blank(stack) && !details){lines.add(WorkshopText.style(Component.translatable("temperandtang.short.shift",WorkshopText.style(Component.literal("SHIFT"),WorkshopText.STAGE)),WorkshopText.MUTED));return}
        val shown=if(ThermalState.blank(stack))ItemStack.parseOptional(level.registryAccess(),ThermalState.data(stack).getCompound("weapon"))else stack
        val grade=Craftsmanship.grade(shown)
        if(grade!=null) {
            val color=when(grade) {CraftGrade.ROUGH->0xA29A91;CraftGrade.SERVICEABLE->0xE4E2DB;CraftGrade.FINE->0x7FC78C;CraftGrade.SUPERIOR->0x69B8EB;CraftGrade.MASTERWORK->0xE6BC63}
            lines.add(WorkshopText.style(Component.translatable("temperandtang.quality."+grade.key),color))
        }else if(GearSupport.supported(stack))lines.add(Component.translatable("temperandtang.craft.unworked").withStyle(ChatFormatting.DARK_GRAY))
        val finish=Finish.entries.firstOrNull {it.key==Craftsmanship.root(shown).getString("finish")}
        if(finish!=null)lines.add(WorkshopText.style(Component.translatable("temperandtang.tooltip.finish",
            WorkshopText.style(Component.translatable("temperandtang.finish_name."+finish.key),WorkshopText.QUANTITY),
            Component.translatable("temperandtang.finish_benefit."+finish.key)),WorkshopText.MUTED).withStyle {it.withItalic(false)})
        val material=CraftMaterials.recorded(shown)
        if(material!=ForgeMaterial.NEUTRAL)lines.add(if(details)WorkshopDescriptions.material(shown)else WorkshopText.style(Component.translatable("temperandtang.short.material",WorkshopText.style(Component.translatable("temperandtang.material.name."+material.key),WorkshopText.QUANTITY)),WorkshopText.MUTED))
        val trait=CraftTraits.recorded(shown)
        if(trait!=ForgeTrait.NONE)lines.add(if(details)WorkshopDescriptions.trait(shown)else WorkshopText.style(Component.translatable("temperandtang.short.trait",WorkshopText.style(Component.translatable("temperandtang.trait."+trait.key),WorkshopText.STAGE)),WorkshopText.MUTED))
        if(details && GearSupport.ranged(shown) && (grade!=null || Craftsmanship.root(shown).contains("finish")))lines.add(WorkshopDescriptions.ranged(shown))
        if(details && grade!=null) {
            val craft=Craftsmanship.data(shown)
            lines.add(Component.translatable("temperandtang.craft.item_accuracy",craft.getInt("accuracy")/10).withStyle(ChatFormatting.GRAY))
            lines.add(Component.translatable("temperandtang.craft.maker",craft.getString("maker"),craft.getInt("mastery")).withStyle(ChatFormatting.GRAY))
        }
        if(Blueprints.filled(stack)) {
            lines.add(Component.translatable("temperandtang.short.reusable").withStyle(ChatFormatting.GRAY))
            if(details)lines.add(Component.literal(Blueprints.pattern(stack)).withStyle(ChatFormatting.DARK_GRAY))
        }
        if(!details && (grade!=null || material!=ForgeMaterial.NEUTRAL || trait!=ForgeTrait.NONE))lines.add(WorkshopText.style(Component.translatable("temperandtang.short.shift",WorkshopText.style(Component.literal("SHIFT"),WorkshopText.STAGE)),WorkshopText.MUTED))
    }
}
