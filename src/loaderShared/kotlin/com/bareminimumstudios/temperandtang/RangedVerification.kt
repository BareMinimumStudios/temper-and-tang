package com.bareminimumstudios.temperandtang

import com.bareminimumstudios.temperandtang.smithing.*
import com.mojang.authlib.GameProfile
import net.minecraft.commands.CommandSourceStack
import net.minecraft.core.BlockPos
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.Arrow
import net.minecraft.world.item.*
import net.minecraft.world.item.enchantment.Enchantments
import java.util.UUID
import kotlin.math.abs

/** Local unattached projectiles only; no connected player or world inventory is changed. */
object RangedVerification {
    fun run(source:CommandSourceStack) {
        val level=source.level
        val vanilla=ItemStack(Items.CROSSBOW)
        check(RangedCraftsmanship.damageMultiplier(vanilla)==1.0)
        check(RangedCraftsmanship.chargingSeconds(2f,vanilla)==2f)
        val id=ResourceLocation.fromNamespaceAndPath("magistuarmory","heavy_crossbow")
        if(!BuiltInRegistries.ITEM.containsKey(id) || !TemperAndTang.config.epicKnights.get())return
        val raw=ItemStack(BuiltInRegistries.ITEM.get(id));check(GearSupport.supported(raw))
        check(!GearSupport.supported(ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("magistuarmory","longbow")))))
        check(RangedCraftsmanship.damageMultiplier(raw)==1.0)
        val owner=object:Player(level,BlockPos(0,500,0),0f,GameProfile(UUID.randomUUID(),"RangedFixture")) {
            override fun isSpectator()=false
            override fun isCreative()=false
        }
        val pull=raw.item.javaClass.getMethod("getPullTime",ItemStack::class.java,net.minecraft.world.entity.LivingEntity::class.java)
        val values=mutableListOf<Double>();val timings=mutableListOf<Int>()
        for(grade in CraftGrade.entries) {
            val gear=Craftsmanship.apply(CraftMaterials.stamp(raw,ForgeMaterial.STEEL),CraftRoll(grade,.9,.85,.5,10),"RangedFixture","fixture")
            check(!gear.isEmpty && gear.item===raw.item)
            val mods=gear.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY).modifiers()
            check(mods.none {it.modifier().id().namespace==TemperAndTang.MOD_ID && it.attribute() in listOf(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE,net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED)})
            val multiplier=RangedCraftsmanship.damageMultiplier(gear);values+=multiplier
            val duration=pull.invoke(raw.item,gear,owner) as Int;timings+=duration
            val original=pull.invoke(raw.item,raw,owner) as Int
            check(duration==kotlin.math.floor(RangedCraftsmanship.chargingSeconds(original/20f,gear)*20).toInt())
            val enchanted=gear.copy();enchanted.enchant(level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.QUICK_CHARGE),1)
            check((pull.invoke(raw.item,enchanted,owner) as Int)<duration)
            val arrow=Arrow(level,0.0,500.0,0.0,ItemStack(Items.ARROW),gear)
            arrow.baseDamage=3.0;arrow.isCritArrow=true
            val seed=CompoundTag();arrow.saveWithoutId(seed);seed.putByte("PierceLevel",2.toByte());arrow.load(seed)
            arrow.tick();check(abs(arrow.baseDamage-3.0*multiplier)<.000001)
            val once=arrow.baseDamage;arrow.tick();check(arrow.baseDamage==once && arrow.pierceLevel.toInt()==2 && arrow.isCritArrow)
            val saved=CompoundTag();arrow.saveWithoutId(saved)
            val loaded=Arrow(EntityType.ARROW,level);loaded.load(saved);loaded.tick();check(loaded.baseDamage==once && loaded.pierceLevel.toInt()==2)
            // Saved-before-first-tick projectiles still receive exactly one quality adjustment.
            val pending=Arrow(level,0.0,500.0,0.0,ItemStack(Items.ARROW),gear);pending.baseDamage=3.0
            val before=CompoundTag();pending.saveWithoutId(before);val resumed=Arrow(EntityType.ARROW,level);resumed.load(before);resumed.tick();check(abs(resumed.baseDamage-3.0*multiplier)<.000001)
            val legacy=saved.copy();legacy.remove("temperandtang_power_applied");val old=Arrow(EntityType.ARROW,level);old.load(legacy);old.tick();check(old.baseDamage==once)
            for(finish in Finish.entries) {
                val finished=WeaponFinishes.preview(gear,finish);check(!finished.isEmpty)
                check(Craftsmanship.grade(finished)==grade)
            }
        }
        if(TemperAndTang.config.qualityPower.get()>0) {
            check(values.zipWithNext().all {(a,b)->a<b})
            check(timings.zipWithNext().all {(a,b)->a>=b} && timings.first()>timings.last())
        }
        TemperAndTang.logger.info("RANGED_QUALITY_PASSED: five heavy-crossbow grades, native loading and Quick Charge, all finishes, single arrow scaling, before/after-tick persistence, legacy safety and untouched vanilla; power={}, loading={}",values,timings)
    }
}

