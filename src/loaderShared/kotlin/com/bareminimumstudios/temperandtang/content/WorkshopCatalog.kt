package com.bareminimumstudios.temperandtang.content

import com.bareminimumstudios.temperandtang.TemperAndTang
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvents
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.EquipmentSlotGroup
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.item.*
import net.minecraft.world.item.crafting.Ingredient

object SteelTier : Tier by Tiers.IRON {
    override fun getUses()=400
    override fun getSpeed()=7f
    override fun getAttackDamageBonus()=2.5f
    override fun getRepairIngredient()=Ingredient.of(TagKey.create(Registries.ITEM,ResourceLocation.fromNamespaceAndPath("c","ingots/steel")))
}

/** Familiar tool and plate families; optional weapon mods retain their own identities and statistics. */
object WorkshopCatalog {
    val armorMaterials:Map<String,()->ArmorMaterial> = linkedMapOf(
        "bronze" to {armor("bronze",listOf(2,5,4,2),"gold",0f)},
        "steel" to {armor("steel",listOf(3,7,5,3),"iron",1f)}
    )
    private fun armor(name:String,values:List<Int>,texture:String,toughness:Float)=ArmorMaterial(
        mapOf(ArmorItem.Type.HELMET to values[0],ArmorItem.Type.CHESTPLATE to values[1],ArmorItem.Type.LEGGINGS to values[2],ArmorItem.Type.BOOTS to values[3]),
        9,SoundEvents.ARMOR_EQUIP_IRON,{Ingredient.of(TagKey.create(Registries.ITEM,ResourceLocation.fromNamespaceAndPath("c","ingots/$name")))},
        listOf(ArmorMaterial.Layer(ResourceLocation.withDefaultNamespace(texture))),toughness,0f)
    private fun material(name:String)=BuiltInRegistries.ARMOR_MATERIAL.getHolderOrThrow(ResourceKey.create(Registries.ARMOR_MATERIAL,TemperAndTang.id(name)))
    fun tab(item:Item)=when(item) {
        is DiggerItem -> CreativeModeTabs.TOOLS_AND_UTILITIES
        is SwordItem,is ArmorItem,is ShieldItem -> CreativeModeTabs.COMBAT
        is BlockItem -> if(item.block is WorkshopBlock)CreativeModeTabs.FUNCTIONAL_BLOCKS else CreativeModeTabs.BUILDING_BLOCKS
        else -> CreativeModeTabs.INGREDIENTS
    }
    val items:Map<String,()->Item> = buildMap {
        for((name,tier) in listOf("bronze" to BronzeTier,"steel" to SteelTier)) {
            if(name!="bronze")put("${name}_sword") {SwordItem(tier,Item.Properties().attributes(SwordItem.createAttributes(tier,3,-2.4f)))}
            put("${name}_pickaxe") {PickaxeItem(tier,Item.Properties().attributes(PickaxeItem.createAttributes(tier,1f,-2.8f)))}
            put("${name}_axe") {AxeItem(tier,Item.Properties().attributes(AxeItem.createAttributes(tier,6f,-3.1f)))}
            put("${name}_shovel") {ShovelItem(tier,Item.Properties().attributes(ShovelItem.createAttributes(tier,1.5f,-3f)))}
            put("${name}_hoe") {HoeItem(tier,Item.Properties().attributes(HoeItem.createAttributes(tier,-tier.attackDamageBonus,-1f)))}
            for((part,type) in listOf("helmet" to ArmorItem.Type.HELMET,"chestplate" to ArmorItem.Type.CHESTPLATE,"leggings" to ArmorItem.Type.LEGGINGS,"boots" to ArmorItem.Type.BOOTS)) {
                put("${name}_$part") {ArmorItem(material(name),type,Item.Properties().durability(type.getDurability(if(name=="bronze")13 else 20)))}
            }
        }
        for((name,tier) in listOf("bronze" to BronzeTier,"iron" to Tiers.IRON,"steel" to SteelTier)) {
            put("${name}_dagger") {SwordItem(tier,Item.Properties().attributes(SwordItem.createAttributes(tier,2,-1.8f)))}
            put("${name}_spear") {
                val attrs=SwordItem.createAttributes(tier,3,-2.8f).withModifierAdded(Attributes.ENTITY_INTERACTION_RANGE,
                    AttributeModifier(TemperAndTang.id("spear_reach"),1.0,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND)
                SwordItem(tier,Item.Properties().attributes(attrs))
            }
            put("${name}_warhammer") {SwordItem(tier,Item.Properties().attributes(SwordItem.createAttributes(tier,5,-3.2f)))}
        }
    }
}
