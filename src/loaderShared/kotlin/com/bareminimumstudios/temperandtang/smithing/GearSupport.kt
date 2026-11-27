package com.bareminimumstudios.temperandtang.smithing

import com.bareminimumstudios.temperandtang.TemperAndTang
import com.bareminimumstudios.temperandtang.api.TemperAndTangApi
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.item.*
import net.minecraft.world.item.component.ItemAttributeModifiers

object GearSupport {
    val INCLUDED=TagKey.create(Registries.ITEM,TemperAndTang.id("forgeable_equipment"))
    val EXCLUDED=TagKey.create(Registries.ITEM,TemperAndTang.id("excluded_equipment"))
    fun armor(stack:ItemStack)=stack.item is ArmorItem && stack.item !is AnimalArmorItem
    fun ranged(stack:ItemStack)=stack.item is CrossbowItem && (BuiltInRegistries.ITEM.getKey(stack.item).toString()=="magistuarmory:heavy_crossbow" || stack.`is`(INCLUDED))
    fun weapon(stack:ItemStack)=ranged(stack) || stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,ItemAttributeModifiers.EMPTY).modifiers().any {it.attribute()==Attributes.ATTACK_DAMAGE && it.modifier().amount()>0}
    fun supported(stack:ItemStack):Boolean {
        if(stack.isEmpty || stack.count!=1 || !stack.isDamageableItem || stack.`is`(EXCLUDED) || stack.`is`(TemperAndTangApi.EXCLUDED_WEAPONS))return false
        val id=BuiltInRegistries.ITEM.getKey(stack.item)
        if(id.namespace=="simplyswords" && !TemperAndTang.config.simplySwords.get() || id.namespace=="simplymore" && !TemperAndTang.config.simplyMore.get() || id.namespace=="magistuarmory" && !TemperAndTang.config.epicKnights.get())return false
        // Ordinary metal families only. Unique/runic identities keep their own acquisition and powers.
        if(id.namespace in setOf("simplyswords","simplymore") && !id.path.matches(Regex("(iron|gold|golden|diamond|netherite)_.+")))return false
        if(stack.`is`(INCLUDED))return true
        if(id.path.startsWith("wooden_") || id.path.startsWith("stone_"))return false
        val item=stack.item
        if(item is TieredItem && item.tier in listOf(Tiers.WOOD,Tiers.STONE))return false
        if(item is ArmorItem && item.material==ArmorMaterials.LEATHER)return false
        if(id.toString() in setOf("minecraft:turtle_helmet","minecraft:wolf_armor"))return false
        return ranged(stack) || item is TieredItem || armor(stack) || weapon(stack) || item is ShieldItem || stack.`is`(TemperAndTangApi.FINISHABLE_WEAPONS) || TemperAndTangApi.acceptsCustom(stack)
    }
    fun steelCore(stack:ItemStack):Boolean {
        val id=BuiltInRegistries.ITEM.getKey(stack.item)
        return id.namespace!="minecraft" && id.path.startsWith("iron_") && supported(stack) &&
            !BuiltInRegistries.ITEM.containsKey(ResourceLocation.fromNamespaceAndPath(id.namespace,"steel_"+id.path.removePrefix("iron_")))
    }
    fun blueprint(stack:ItemStack)=BuiltInRegistries.ITEM.getKey(stack.item).namespace=="magistuarmory"
    fun category(stack:ItemStack)=if(armor(stack)) 1 else if(stack.item is ShieldItem) 2 else 0
}
