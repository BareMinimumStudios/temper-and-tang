package com.bareminimumstudios.temperandtang.smithing
import com.bareminimumstudios.temperandtang.TemperAndTang
import com.bareminimumstudios.temperandtang.api.TemperAndTangApi
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.contents.TranslatableContents
import net.minecraft.world.entity.EquipmentSlotGroup
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.SwordItem
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.item.component.ItemAttributeModifiers
import net.minecraft.world.item.component.ItemLore

object WeaponFinishes {
    private const val ROOT = "temperandtang"
    fun accepts(stack: ItemStack): Boolean {
        if (stack.isEmpty || !stack.isDamageableItem || stack.count != 1 || stack.`is`(TemperAndTangApi.EXCLUDED_WEAPONS)) return false
        val id = BuiltInRegistries.ITEM.getKey(stack.item)
        return when (id.namespace) {
            "simplyswords" -> TemperAndTang.config.simplySwords.get()
            "simplymore" -> TemperAndTang.config.simplyMore.get()
            else -> stack.item is SwordItem || stack.`is`(TemperAndTangApi.FINISHABLE_WEAPONS) || TemperAndTangApi.acceptsCustom(stack)
        }
    }
    fun ingredient(stack: ItemStack): Finish? = Finish.entries.firstOrNull {
        stack.`is`(TemperAndTangApi.finishingMaterialTag(it.key))
    }
    fun preview(input: ItemStack, finish: Finish): ItemStack {
        if (!accepts(input)) return ItemStack.EMPTY
        val custom = input.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()
        val prior = custom.getCompound(ROOT)
        if (prior.contains("finish") && !TemperAndTang.config.allowRefinishing.get()) return ItemStack.EMPTY
        if (prior.getString("finish") == finish.key) return ItemStack.EMPTY
        val base = if (prior.contains("base_durability")) prior.getInt("base_durability") else input.getOrDefault(DataComponents.MAX_DAMAGE, input.maxDamage)
        val effect = FinishRules.effect(finish, TemperAndTang.config.balance())
        val maximum = FinishRules.maximumDurability(base, effect)
        val result = input.copy()
        val builder = ItemAttributeModifiers.builder()
        val attributes = input.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY)
        attributes.modifiers().forEach {
            if (it.modifier().id().namespace != TemperAndTang.MOD_ID)
                builder.add(it.attribute(), it.modifier(), it.slot())
        }
        if (effect.damage != 0.0) builder.add(Attributes.ATTACK_DAMAGE,
            AttributeModifier(TemperAndTang.id("finish_damage"), effect.damage, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.MAINHAND)
        if (effect.speed != 0.0) builder.add(Attributes.ATTACK_SPEED,
            AttributeModifier(TemperAndTang.id("finish_speed"), effect.speed, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.MAINHAND)
        result.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build().withTooltip(attributes.showInTooltip()))
        result.set(DataComponents.MAX_DAMAGE, maximum)
        if (input.damageValue >= result.maxDamage) return ItemStack.EMPTY
        val mark = CompoundTag()
        mark.putInt("version", 1)
        mark.putString("finish", finish.key)
        mark.putInt("base_durability", base)
        custom.put(ROOT, mark)
        result.set(DataComponents.CUSTOM_DATA, CustomData.of(custom))
        val lore = input.getOrDefault(DataComponents.LORE, ItemLore.EMPTY).lines().filterNot {
            (it.contents as? TranslatableContents)?.key?.startsWith("temperandtang.finish.") == true
        }.toMutableList()
        lore.add(Component.translatable("temperandtang.finish." + finish.key))
        result.set(DataComponents.LORE, ItemLore(lore))
        return result
    }
}
