package com.bareminimumstudios.temperandtang.smithing

import com.bareminimumstudios.temperandtang.TemperAndTang
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData

/** Tags are the integration contract. Unrecognized third-party materials keep their authored base stats. */
object CraftMaterials {
    fun recorded(stack:ItemStack)=ForgeMaterial.parse(Craftsmanship.root(stack).getString("material"))
    fun power(stack:ItemStack):Double {
        val root=Craftsmanship.root(stack)
        return (if(root.contains("material_power"))root.getDouble("material_power") else TemperAndTang.config.materialPower.get()).takeIf {it.isFinite()}?.coerceIn(0.0,1.0)?:0.0
    }
    fun identify(stack:ItemStack):ForgeMaterial {
        if(stack.isEmpty)return ForgeMaterial.NEUTRAL
        for(material in ForgeMaterial.entries.filter {it!=ForgeMaterial.NEUTRAL}) {
            if(stack.`is`(TagKey.create(Registries.ITEM,TemperAndTang.id("materials/"+material.key))))return material
        }
        val path=BuiltInRegistries.ITEM.getKey(stack.item).path
        return ForgeMaterial.entries.firstOrNull {it!=ForgeMaterial.NEUTRAL && (path.startsWith(it.key+"_") || it==ForgeMaterial.GOLD && path.startsWith("golden_"))}?:ForgeMaterial.NEUTRAL
    }
    fun choose(output:ItemStack,inputs:List<ItemStack>):ForgeMaterial {
        // An upgrade's actual finished tier wins over a carried lower-tier base item.
        val authored=identify(output)
        if(authored!=ForgeMaterial.NEUTRAL)return authored
        return inputs.filter {WorkshopPlans.stock(it)}.groupBy(::identify).filterKeys {it!=ForgeMaterial.NEUTRAL}
            .maxByOrNull {it.value.sumOf(ItemStack::getCount)}?.key?:recorded(output).takeUnless {it==ForgeMaterial.NEUTRAL}?:inputs.filter(GearSupport::supported).singleOrNull()?.let {recorded(it).takeUnless {m->m==ForgeMaterial.NEUTRAL}?:identify(it)}?:ForgeMaterial.NEUTRAL
    }
    fun stamp(input:ItemStack,material:ForgeMaterial):ItemStack {
        val result=input.copy();val all=result.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();val root=all.getCompound("temperandtang")
        root.putString("material",material.key);root.putDouble("material_power",TemperAndTang.config.materialPower.get());all.put("temperandtang",root);result.set(DataComponents.CUSTOM_DATA,CustomData.of(all))
        return result
    }
}
