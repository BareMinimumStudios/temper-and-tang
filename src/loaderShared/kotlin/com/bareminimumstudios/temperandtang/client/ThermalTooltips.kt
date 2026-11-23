package com.bareminimumstudios.temperandtang.client
import com.bareminimumstudios.temperandtang.smithing.*
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.ChatFormatting

object ThermalTooltips {
    fun append(stack:ItemStack,lines:MutableList<Component>) {
        val level=Minecraft.getInstance().level?:return
        if(ThermalState.hot(stack,level)) {
            val until=ThermalState.data(stack).getLong("hot_until")
            val seconds=((until-level.gameTime+19)/20).coerceAtLeast(0)
            lines.add((if(until>0)Component.translatable("temperandtang.thermal.tooltip.hot",seconds)else Component.translatable("temperandtang.thermal.tooltip.native_hot")).withStyle(ChatFormatting.GOLD))
            lines.add(Component.translatable("temperandtang.thermal.tooltip.handle").withStyle(ChatFormatting.GRAY))
        } else if(ThermalState.blank(stack))lines.add(Component.translatable("temperandtang.thermal.tooltip.blank").withStyle(ChatFormatting.GRAY))
        val shown=if(ThermalState.blank(stack))ItemStack.parseOptional(level.registryAccess(),ThermalState.data(stack).getCompound("weapon"))else stack
        val material=CraftMaterials.recorded(shown)
        if(material!=ForgeMaterial.NEUTRAL)lines.add(WorkshopDescriptions.material(shown).copy().withStyle(ChatFormatting.AQUA))
        if(CraftTraits.recorded(shown)!=ForgeTrait.NONE)lines.add(WorkshopDescriptions.trait(shown).copy().withStyle(ChatFormatting.LIGHT_PURPLE))
        val grade=Craftsmanship.grade(shown)
        if(grade!=null) {
            val color=when(grade) {CraftGrade.ROUGH->ChatFormatting.GRAY;CraftGrade.SERVICEABLE->ChatFormatting.WHITE;CraftGrade.FINE->ChatFormatting.GREEN;CraftGrade.SUPERIOR->ChatFormatting.BLUE;CraftGrade.MASTERWORK->ChatFormatting.LIGHT_PURPLE;CraftGrade.EXCEPTIONAL->ChatFormatting.GOLD}
            lines.add(Component.translatable("temperandtang.quality."+grade.key).withStyle(color))
            val craft=Craftsmanship.data(shown)
            lines.add(Component.translatable("temperandtang.craft.item_accuracy",craft.getInt("accuracy")/10).withStyle(ChatFormatting.GRAY))
            lines.add(Component.translatable("temperandtang.craft.maker",craft.getString("maker"),craft.getInt("mastery")).withStyle(ChatFormatting.GRAY))
        } else if(GearSupport.supported(stack))lines.add(Component.translatable("temperandtang.craft.unworked").withStyle(ChatFormatting.DARK_GRAY))
        if(Blueprints.filled(stack)) {
            lines.add(Component.literal(Blueprints.pattern(stack)).withStyle(ChatFormatting.DARK_GRAY))
            lines.add(Component.translatable("temperandtang.craft.blueprint_reusable").withStyle(ChatFormatting.GRAY))
        }
        if(ThermalState.blank(stack))lines.add(Component.translatable("temperandtang.thermal.tooltip.quench").withStyle(ChatFormatting.GRAY))
    }
}
