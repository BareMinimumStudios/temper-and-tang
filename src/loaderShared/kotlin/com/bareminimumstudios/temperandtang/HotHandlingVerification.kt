package com.bareminimumstudios.temperandtang

import com.bareminimumstudios.temperandtang.content.TemperContent
import com.bareminimumstudios.temperandtang.smithing.*
import com.mojang.authlib.GameProfile
import net.minecraft.commands.CommandSourceStack
import net.minecraft.core.BlockPos
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.CustomData
import java.util.UUID

/** Inventories are isolated from connected players and never added to the world. */
object HotHandlingVerification {
    fun verify(source:CommandSourceStack) {
        if(!TemperAndTang.config.hotMetalHandling.get() || !TemperAndTang.config.thermalEnabled.get()) return
        val level=source.level
        fun player(creative:Boolean=false)=object:Player(level,BlockPos.ZERO,0f,GameProfile(UUID.randomUUID(),"HandlingFixture")) {
            override fun isSpectator()=false
            override fun isCreative()=creative
        }.also { it.health=it.maxHealth }
        fun tongs()=ItemStack(TemperContent.item("smithing_tongs"))
        fun hot(count:Int=8)=ThermalState.heated(ItemStack(Items.IRON_INGOT,count),level)
        fun expired(stack:ItemStack):ItemStack=stack.copy().also {
            val all=it.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag()
            all.getCompound("temperandtang").getCompound("thermal").putLong("hot_until",(level.gameTime-1).coerceAtLeast(1))
            it.set(DataComponents.CUSTOM_DATA,CustomData.of(all))
        }
        check(HotMetalHandling.isTongs(tongs())) { "Own tongs tag is not bound" }
        for(hand in listOf(EquipmentSlot.MAINHAND,EquipmentSlot.OFFHAND)) {
            val p=player();p.inventory.setItem(1,hot());p.inventory.setItem(2,hot())
            p.setItemSlot(hand,tongs());val health=p.health
            HotMetalHandling.process(p)
            check(p.health==health && p.getItemBySlot(hand).damageValue==1) { "Tongs failed or wear multiplied by stacks" }
            repeat(5) { HotMetalHandling.process(p) }
            check(p.health==health && p.getItemBySlot(hand).damageValue==1) { "Repeated processing charged more wear" }
        }
        val cursor=player();cursor.setItemSlot(EquipmentSlot.OFFHAND,tongs());cursor.inventoryMenu.setCarried(hot())
        HotMetalHandling.process(cursor)
        check(cursor.health==cursor.maxHealth && cursor.offhandItem.damageValue==1 && cursor.inventoryMenu.carried.count==8) { "Cursor bypass or item loss" }
        val bare=player();bare.inventory.setItem(1,hot());val before=bare.health
        HotMetalHandling.process(bare)
        check(bare.health==before-TemperAndTang.config.hotMetalDamage.get().toFloat()) { "Bare-hand contact did not burn once" }
        val after=bare.health;repeat(5) { HotMetalHandling.process(bare) }
        check(bare.health==after && !bare.isOnFire) { "Duplicate burns or persistent fire" }
        val immune=player();immune.addEffect(MobEffectInstance(MobEffects.FIRE_RESISTANCE,200));immune.inventory.setItem(1,hot())
        HotMetalHandling.process(immune);check(immune.health==immune.maxHealth)
        val creative=player(true);creative.inventory.setItem(1,hot());creative.setItemSlot(EquipmentSlot.OFFHAND,tongs())
        HotMetalHandling.process(creative);check(creative.health==creative.maxHealth && creative.offhandItem.damageValue==0)
        val cold=player();val named=ThermalState.heated(ItemStack(Items.IRON_SWORD).also {
            it.damageValue=7;it.set(DataComponents.CUSTOM_NAME,Component.literal("Cooling preservation"))
            it.set(DataComponents.CUSTOM_DATA,CustomData.of(CompoundTag().also {tag->tag.putString("foreign_mod","keep")}))
        },level)
        cold.inventory.setItem(1,expired(named));cold.inventoryMenu.setCarried(expired(hot()))
        HotMetalHandling.process(cold)
        val result=cold.inventory.getItem(1)
        check(!ThermalState.hot(result,level) && !ThermalState.pending(result) && result.damageValue==7 && result.hoverName.string=="Cooling preservation")
        check(result.get(DataComponents.CUSTOM_DATA)?.copyTag()?.getString("foreign_mod")=="keep")
        check(cold.inventoryMenu.carried.count==8 && !ThermalState.hot(cold.inventoryMenu.carried,level) && cold.health==cold.maxHealth)
        val blank=player();blank.inventory.setItem(1,expired(ThermalState.unquenched(ItemStack(Items.IRON_SWORD),level)))
        HotMetalHandling.process(blank)
        check(ThermalState.blank(blank.inventory.getItem(1)) && !ThermalState.hot(blank.inventory.getItem(1),level)) { "Air cooling bypassed standalone quenching" }
        val breaking=player();breaking.inventory.setItem(1,hot());breaking.setItemSlot(EquipmentSlot.OFFHAND,tongs().also {it.damageValue=it.maxDamage-1})
        HotMetalHandling.process(breaking);check(breaking.offhandItem.isEmpty && breaking.health==breaking.maxHealth)
        val repaired=tongs();check(repaired.item.isValidRepairItem(repaired,ItemStack(Items.IRON_INGOT)))
        val saved=ItemStack.parseOptional(level.registryAccess(),tongs().also {it.damageValue=17}.save(level.registryAccess()) as CompoundTag)
        check(saved.damageValue==17 && HotMetalHandling.isTongs(saved))
        TemperAndTang.logger.info("TEMPER_HANDLING_VERIFY_PASSED: owned main/offhand tongs, stack/cursor protection, bounded wear/burns, immunity, cooling, blank preservation, repair and persistence")
    }
}
