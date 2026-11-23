package com.bareminimumstudios.temperandtang.smithing

import com.bareminimumstudios.temperandtang.TemperAndTang
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Rarity
import net.minecraft.world.item.component.CustomData

object Craftsmanship {
    fun root(stack:ItemStack)=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getCompound("temperandtang")
    fun data(stack:ItemStack)=root(stack).getCompound("craftsmanship")
    fun grade(stack:ItemStack)=runCatching {CraftGrade.valueOf(data(stack).getString("grade"))}.getOrNull()
    fun apply(input:ItemStack,roll:CraftRoll,maker:String,recipe:String):ItemStack {
        if(!GearSupport.supported(input))return ItemStack.EMPTY
        val result=input.copy()
        val all=result.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();val root=all.getCompound("temperandtang")
        if(!root.contains("origin_durability"))root.putInt("origin_durability",if(root.contains("base_durability")) root.getInt("base_durability") else input.getOrDefault(DataComponents.MAX_DAMAGE,input.maxDamage))
        root.put("craftsmanship",CompoundTag().also {
            it.putInt("version",1);it.putString("grade",roll.grade.name);it.putInt("accuracy",(roll.accuracy*1000).toInt())
            it.putDouble("effective",roll.effective);it.putDouble("luck",roll.luck);it.putInt("mastery",roll.mastery)
            it.putString("maker",maker.take(64));it.putString("pattern",recipe)
        })
        all.put("temperandtang",root);result.set(DataComponents.CUSTOM_DATA,CustomData.of(all))
        if(!root.contains("origin_rarity"))root.putString("origin_rarity",input.getOrDefault(DataComponents.RARITY,Rarity.COMMON).name)
        all.put("temperandtang",root);result.set(DataComponents.CUSTOM_DATA,CustomData.of(all))
        val origin=runCatching {Rarity.valueOf(root.getString("origin_rarity"))}.getOrDefault(Rarity.COMMON)
        val rarity=when(roll.grade) {CraftGrade.FINE->Rarity.UNCOMMON;CraftGrade.SUPERIOR->Rarity.RARE;CraftGrade.MASTERWORK,CraftGrade.EXCEPTIONAL->Rarity.EPIC;else->Rarity.COMMON}
        result.set(DataComponents.RARITY,if(rarity.ordinal>origin.ordinal)rarity else origin)
        return GearStats.rebuild(result)
    }
    fun forge(input:ItemStack,accuracy:Double,player:Player,recipe:String,inputs:List<ItemStack> = emptyList()):ItemStack {
        val level=player.server?.let {SmithingProgress.get(it).level(player.uuid)}?:0
        val roll=CraftRules.roll(accuracy,level,ForgeRandom.nextDouble(),TemperAndTang.config.qualityLuck.get(),TemperAndTang.config.qualityMastery.get())
        return apply(CraftMaterials.stamp(input,CraftMaterials.choose(input,inputs)),roll,player.scoreboardName,recipe)
    }
    fun canReforge(stack:ItemStack):Boolean {
        if(!GearSupport.supported(stack))return false
        return !apply(CraftMaterials.stamp(stack,CraftMaterials.choose(stack,emptyList())),CraftRules.roll(0.0,0,0.0),"", "").isEmpty
    }
}
