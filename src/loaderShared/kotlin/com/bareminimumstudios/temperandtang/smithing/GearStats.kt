package com.bareminimumstudios.temperandtang.smithing

import com.bareminimumstudios.temperandtang.TemperAndTang
import net.minecraft.core.component.DataComponents
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.EquipmentSlotGroup
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.item.ArmorItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ItemAttributeModifiers

/** Rebuild owned bonuses from a raw durability baseline and untouched foreign attribute entries. */
object GearStats {
    fun rebuild(input:ItemStack):ItemStack {
        val result=input.copy();val root=Craftsmanship.root(result);val grade=Craftsmanship.grade(result)
        val finish=Finish.entries.firstOrNull {it.key==root.getString("finish")}
        val base=(if(root.contains("origin_durability"))root.getInt("origin_durability") else if(root.contains("base_durability"))root.getInt("base_durability") else input.getOrDefault(DataComponents.MAX_DAMAGE,input.maxDamage)).coerceAtLeast(1)
        val scale=TemperAndTang.config.qualityPower.get()
        val material=CraftMaterials.recorded(input);val materialPower=CraftMaterials.power(input)
        val craftMax=(base*(1+(grade?.durability?:0.0)*scale+material.durability*materialPower)).toLong().coerceIn(1,Int.MAX_VALUE.toLong()).toInt()
        val precision=if(root.contains("finish_accuracy"))root.getDouble("finish_accuracy").coerceIn(0.0,1.0) else 1.0
        val strength=0.5+precision*0.5
        val effect=finish?.let {FinishRules.effect(it,TemperAndTang.config.balance()).let {effect->FinishEffect(effect.damage*strength,effect.speed*strength,effect.durability*strength)}}
        val maximum=if(effect!=null)FinishRules.maximumDurability(craftMax,effect) else craftMax
        if(input.damageValue>=maximum)return ItemStack.EMPTY
        result.set(DataComponents.MAX_DAMAGE,maximum)
        val old=input.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,ItemAttributeModifiers.EMPTY)
        val original=old.modifiers().filterNot {it.modifier().id().namespace==TemperAndTang.MOD_ID && (it.modifier().id().path.startsWith("craft_") || it.modifier().id().path.startsWith("finish_"))}
        val builder=ItemAttributeModifiers.builder();original.forEach {builder.add(it.attribute(),it.modifier(),it.slot())}
        if(grade!=null || material!=ForgeMaterial.NEUTRAL) {
            val grouped=original.filter {it.modifier().operation()==AttributeModifier.Operation.ADD_VALUE}.groupBy {it.attribute() to it.slot()}
            for((key,entries) in grouped) {
                val (attribute,slot)=key
                val boost=when(attribute) {Attributes.ATTACK_DAMAGE->(grade?.damage?:0.0);Attributes.ARMOR->(grade?.armor?:0.0);Attributes.ARMOR_TOUGHNESS->(grade?.toughness?:0.0);else->0.0}*scale + when(attribute) {Attributes.ATTACK_DAMAGE->material.damage;Attributes.ARMOR->material.armor;else->0.0}*materialPower
                val amount=entries.sumOf {it.modifier().amount()}*boost
                if(amount!=0.0 && amount.isFinite())builder.add(attribute,AttributeModifier(TemperAndTang.id("craft_"+when(attribute) {Attributes.ATTACK_DAMAGE->"damage";Attributes.ARMOR->"armor";else->"toughness"}+"_"+slot.name.lowercase(java.util.Locale.ROOT)),amount,AttributeModifier.Operation.ADD_VALUE),slot)
            }
        }
        if(GearSupport.weapon(input) && material.speed!=0.0)builder.add(Attributes.ATTACK_SPEED,AttributeModifier(TemperAndTang.id("craft_material_speed"),material.speed*materialPower,AttributeModifier.Operation.ADD_MULTIPLIED_BASE),EquipmentSlotGroup.MAINHAND)
        if(input.item is ArmorItem && material.movement!=0.0) {
            val slot=when((input.item as ArmorItem).equipmentSlot) {EquipmentSlot.HEAD->EquipmentSlotGroup.HEAD;EquipmentSlot.CHEST->EquipmentSlotGroup.CHEST;EquipmentSlot.LEGS->EquipmentSlotGroup.LEGS;else->EquipmentSlotGroup.FEET}
            builder.add(Attributes.MOVEMENT_SPEED,AttributeModifier(TemperAndTang.id("craft_material_movement_"+slot.name.lowercase(java.util.Locale.ROOT)),material.movement*materialPower,AttributeModifier.Operation.ADD_MULTIPLIED_BASE),slot)
        }
        if(effect!=null && GearSupport.weapon(input)) {
            if(effect.damage!=0.0)builder.add(Attributes.ATTACK_DAMAGE,AttributeModifier(TemperAndTang.id("finish_damage"),effect.damage,AttributeModifier.Operation.ADD_MULTIPLIED_BASE),EquipmentSlotGroup.MAINHAND)
            if(effect.speed!=0.0)builder.add(Attributes.ATTACK_SPEED,AttributeModifier(TemperAndTang.id("finish_speed"),effect.speed,AttributeModifier.Operation.ADD_MULTIPLIED_BASE),EquipmentSlotGroup.MAINHAND)
        } else if(finish==Finish.LIGHTENED && input.item is ArmorItem) {
            val slot=when((input.item as ArmorItem).equipmentSlot) {EquipmentSlot.HEAD->EquipmentSlotGroup.HEAD;EquipmentSlot.CHEST->EquipmentSlotGroup.CHEST;EquipmentSlot.LEGS->EquipmentSlotGroup.LEGS;else->EquipmentSlotGroup.FEET}
            builder.add(Attributes.MOVEMENT_SPEED,AttributeModifier(TemperAndTang.id("finish_movement_"+slot.name.lowercase(java.util.Locale.ROOT)),TemperAndTang.config.lightenedArmorSpeed.get()*strength,AttributeModifier.Operation.ADD_MULTIPLIED_BASE),slot)
        }
        result.set(DataComponents.ATTRIBUTE_MODIFIERS,builder.build().withTooltip(old.showInTooltip()))
        return result
    }
}
