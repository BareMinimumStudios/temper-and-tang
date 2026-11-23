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
    fun accepts(stack:ItemStack)=GearSupport.supported(stack)
    fun ingredient(stack: ItemStack): Finish? = Finish.entries.firstOrNull {
        stack.`is`(TemperAndTangApi.finishingMaterialTag(it.key))
    }
    fun preview(input: ItemStack, finish: Finish, precision:Double=1.0): ItemStack {
        if (!accepts(input) || ThermalState.pending(input)) return ItemStack.EMPTY
        val custom = input.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()
        val prior = custom.getCompound(ROOT)
        if (prior.contains("finish") && !TemperAndTang.config.allowRefinishing.get()) return ItemStack.EMPTY
        if (prior.getString("finish") == finish.key) return ItemStack.EMPTY
        if(finish in listOf(Finish.KEEN,Finish.WEIGHTED) && !GearSupport.weapon(input))return ItemStack.EMPTY
        if(finish==Finish.LIGHTENED && !GearSupport.weapon(input) && !GearSupport.armor(input))return ItemStack.EMPTY
        val result=input.copy()
        val mark=prior.copy()
        val base=if(prior.contains("origin_durability"))prior.getInt("origin_durability") else if(prior.contains("base_durability"))prior.getInt("base_durability") else input.getOrDefault(DataComponents.MAX_DAMAGE,input.maxDamage)
        mark.putDouble("finish_accuracy",precision.takeIf {it.isFinite()}?.coerceIn(0.0,1.0)?:0.0)
        mark.putInt("origin_durability",base);mark.putInt("base_durability",base);mark.putInt("version",2);mark.putString("finish",finish.key)
        custom.put(ROOT,mark);result.set(DataComponents.CUSTOM_DATA,CustomData.of(custom))
        val rebuilt=GearStats.rebuild(result)
        if(rebuilt.isEmpty)return rebuilt
        val lore = input.getOrDefault(DataComponents.LORE, ItemLore.EMPTY).lines().filterNot {
            (it.contents as? TranslatableContents)?.key?.startsWith("temperandtang.finish.") == true
        }.toMutableList()
        lore.add(Component.translatable("temperandtang.finish." + finish.key))
        rebuilt.set(DataComponents.LORE, ItemLore(lore))
        return rebuilt
    }
}
