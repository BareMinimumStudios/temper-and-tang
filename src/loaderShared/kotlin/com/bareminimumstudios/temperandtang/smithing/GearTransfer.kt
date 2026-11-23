package com.bareminimumstudios.temperandtang.smithing
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData

/** Upgrade patterns retain names, enchantments, wear and foreign data without copying the old material's base stats. */
object GearTransfer {
    fun inherit(result:ItemStack,base:ItemStack?):ItemStack {
        if(base==null || result.isEmpty)return result
        base.get(DataComponents.CUSTOM_NAME)?.let {result.set(DataComponents.CUSTOM_NAME,it)}
        base.get(DataComponents.ENCHANTMENTS)?.let {result.set(DataComponents.ENCHANTMENTS,it)}
        base.get(DataComponents.REPAIR_COST)?.let {result.set(DataComponents.REPAIR_COST,it)}
        val foreign=base.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();foreign.remove("temperandtang")
        val own=result.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();own.merge(foreign)
        if(!own.isEmpty)result.set(DataComponents.CUSTOM_DATA,CustomData.of(own))
        result.damageValue=base.damageValue
        return result
    }
    fun rebaseUpgrade(result:ItemStack,base:ItemStack):ItemStack {
        if(result.isEmpty || !GearSupport.supported(result))return result
        val root=Craftsmanship.root(result)
        if(!root.contains("craftsmanship") && !root.contains("finish"))return result
        val previous=base.item.defaultInstance
        val next=result.item.defaultInstance
        val oldDefault=previous.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY)
        val defaults=next.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY)
        val oldIds=oldDefault.modifiers().map {it.attribute() to it.modifier().id()}.toSet()
        val inherited=result.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY)
        val builder=net.minecraft.world.item.component.ItemAttributeModifiers.builder()
        defaults.modifiers().forEach {builder.add(it.attribute(),it.modifier(),it.slot())}
        inherited.modifiers().filterNot {
            (it.attribute() to it.modifier().id()) in oldIds || it.modifier().id().namespace=="temperandtang" && (it.modifier().id().path.startsWith("craft_") || it.modifier().id().path.startsWith("finish_"))
        }.forEach {builder.add(it.attribute(),it.modifier(),it.slot())}
        result.set(DataComponents.ATTRIBUTE_MODIFIERS,builder.build().withTooltip(inherited.showInTooltip()))
        val oldRaw=if(root.contains("origin_durability"))root.getInt("origin_durability") else root.getInt("base_durability")
        val foreignAdjustment=oldRaw-previous.getOrDefault(DataComponents.MAX_DAMAGE,previous.maxDamage)
        val newRaw=(next.getOrDefault(DataComponents.MAX_DAMAGE,next.maxDamage).toLong()+foreignAdjustment).coerceIn(1,Int.MAX_VALUE.toLong()).toInt()
        if(root.contains("material"))root.putString("material",CraftMaterials.identify(result).key)
        root.putInt("origin_durability",newRaw);root.putInt("base_durability",newRaw)
        val all=result.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();all.put("temperandtang",root);result.set(DataComponents.CUSTOM_DATA,CustomData.of(all))
        return GearStats.rebuild(result)
    }
}
