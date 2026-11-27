package com.bareminimumstudios.temperandtang.smithing
import com.bareminimumstudios.temperandtang.TemperAndTang
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.item.component.ItemAttributeModifiers

/** Upgrade patterns retain names, enchantments, wear and foreign data without copying the old material's base stats. */
object GearTransfer {
    private val portable = setOf("custom_name", "enchantments", "repair_cost", "banner_patterns", "base_color", "dyed_color", "trim", "lore")

    @Suppress("UNCHECKED_CAST")
    fun inherit(result:ItemStack,base:ItemStack?):ItemStack {
        if(base==null || result.isEmpty)return result
        val authored=result.componentsPatch.entrySet().map {it.key}.toSet()
        for((type,value) in base.componentsPatch.entrySet()) {
            if(!value.isPresent || type in authored)continue
            val id=BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type)?:continue
            val shouldCopy = if (id.namespace == "minecraft") id.path in portable else id.namespace != TemperAndTang.MOD_ID
            if (shouldCopy)
                result.set(type as DataComponentType<Any>,value.get())
        }
        val foreign=base.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();foreign.remove("temperandtang")
        foreign.merge(result.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag())
        if(!foreign.isEmpty)result.set(DataComponents.CUSTOM_DATA,CustomData.of(foreign))
        val raw=Craftsmanship.root(base).let {root->when {
            root.contains("origin_durability")->root.getInt("origin_durability")
            root.contains("base_durability")->root.getInt("base_durability")
            else->base.maxDamage
        }}
        // Carry external durability adjustments, never the old item's craftsmanship multiplier.
        val adjustment=raw.toLong()-base.item.defaultInstance.maxDamage
        if(adjustment!=0L)result.set(DataComponents.MAX_DAMAGE,(result.maxDamage+adjustment).coerceIn(1,Int.MAX_VALUE.toLong()).toInt())
        val defaults=base.item.defaultInstance.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,ItemAttributeModifiers.EMPTY)
        val defaultIds=defaults.modifiers().map {it.attribute() to it.modifier().id()}.toSet()
        val native=result.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,ItemAttributeModifiers.EMPTY)
        val nativeIds=native.modifiers().map {it.attribute() to it.modifier().id()}.toSet()
        val additions=base.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,ItemAttributeModifiers.EMPTY).modifiers().filter {
            val id=it.modifier().id()
            (it.attribute() to id) !in defaultIds && (it.attribute() to id) !in nativeIds && !(id.namespace=="temperandtang" && (id.path.startsWith("craft_") || id.path.startsWith("finish_")))
        }
        if(additions.isNotEmpty()) {
            val builder=ItemAttributeModifiers.builder()
            (native.modifiers()+additions).forEach {builder.add(it.attribute(),it.modifier(),it.slot())}
            result.set(DataComponents.ATTRIBUTE_MODIFIERS,builder.build().withTooltip(native.showInTooltip()))
        }
        result.damageValue=base.damageValue
        return result
    }
    fun rebaseUpgrade(result:ItemStack,base:ItemStack):ItemStack {
        if(result.isEmpty || !GearSupport.supported(result))return result
        val root=Craftsmanship.root(result)
        if(!root.contains("craftsmanship") && !root.contains("finish"))return result
        val previous=base.item.defaultInstance
        val next=result.item.defaultInstance
        val oldDefault=previous.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,ItemAttributeModifiers.EMPTY)
        val defaults=next.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,ItemAttributeModifiers.EMPTY)
        val oldIds=oldDefault.modifiers().map {it.attribute() to it.modifier().id()}.toSet()
        val inherited=result.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,ItemAttributeModifiers.EMPTY)
        val builder=ItemAttributeModifiers.builder()
        defaults.modifiers().forEach {builder.add(it.attribute(),it.modifier(),it.slot())}
        inherited.modifiers().filterNot {
            (it.attribute() to it.modifier().id()) in oldIds || it.modifier().id().namespace=="temperandtang" && (it.modifier().id().path.startsWith("craft_") || it.modifier().id().path.startsWith("finish_"))
        }.forEach {builder.add(it.attribute(),it.modifier(),it.slot())}
        result.set(DataComponents.ATTRIBUTE_MODIFIERS,builder.build().withTooltip(inherited.showInTooltip()))
        val oldRaw=if(root.contains("origin_durability"))root.getInt("origin_durability") else root.getInt("base_durability")
        val foreignAdjustment=oldRaw-previous.getOrDefault(DataComponents.MAX_DAMAGE,previous.maxDamage)
        val newRaw=(next.getOrDefault(DataComponents.MAX_DAMAGE,next.maxDamage).toLong()+foreignAdjustment).coerceIn(1,Int.MAX_VALUE.toLong()).toInt()
        val nextMaterial=CraftMaterials.identify(result)
        if(root.contains("material") && nextMaterial!=ForgeMaterial.NEUTRAL)root.putString("material",nextMaterial.key)
        root.putInt("origin_durability",newRaw);root.putInt("base_durability",newRaw)
        val all=result.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();all.put("temperandtang",root);result.set(DataComponents.CUSTOM_DATA,CustomData.of(all))
        return GearStats.rebuild(result)
    }
}
