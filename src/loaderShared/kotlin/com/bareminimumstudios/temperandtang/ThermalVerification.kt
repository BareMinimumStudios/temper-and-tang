package com.bareminimumstudios.temperandtang

import com.bareminimumstudios.temperandtang.content.TemperContent
import com.bareminimumstudios.temperandtang.compat.NativeSmithing
import com.bareminimumstudios.temperandtang.smithing.*
import com.bareminimumstudios.temperandtang.station.ThermalStations as T
import com.mojang.authlib.GameProfile
import com.pockyl.diegesis.api.Diegesis
import com.pockyl.diegesis.api.state.*
import com.pockyl.diegesis.api.station.StationContext
import com.pockyl.diegesis.impl.action.ActionHandler
import com.pockyl.diegesis.impl.action.StationAction
import net.minecraft.commands.CommandSourceStack
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.*
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.item.enchantment.Enchantments
import java.util.UUID

/** Isolated operator fixtures exercise the registered Diegesis controllers on real server ticks. */
object ThermalVerification {
    private data class Job(val source: CommandSourceStack,val step: ()->Boolean)
    private val jobs=mutableMapOf<MinecraftServer,Job>()
    private class Fixture(val source: CommandSourceStack,name:String) {
        val type=Diegesis.stationType(TemperAndTang.id(name)).orElseThrow()
        val state=StationState(type.schema())
        val player=object:Player(source.level,BlockPos.ZERO,0f,GameProfile(UUID.randomUUID(),"ThermalFixture")) {
            override fun isSpectator()=false
            override fun isCreative()=false
        }
        val context=object:StationContext {
            override fun type()=type
            override fun level()=source.level
            override fun pos()=BlockPos.containing(source.position)
            override fun blockState()=TemperContent.block(name).defaultBlockState()
            override fun facing()=Direction.NORTH
            override fun state()=state
            override fun users()=listOf(player)
            override fun effect(effect:ResourceLocation,node:String?,data:CompoundTag) {}
        }
        val logic=type.logic().create(context)
        init { logic.onOpen(context,player) }
        fun put(slot:SlotRef,stack:ItemStack) { state.setItem(slot,stack.copy());logic.onSlotChanged(context,slot);logic.updateOutputs(context) }
        fun press(key:String,keyboard:Boolean=true) { ActionHandler.apply(context,logic,player,if(keyboard) StationAction.Custom(key,CompoundTag()) else StationAction.Press(key)) }
        fun take()=logic.takeOutput(context,player,"result")
        fun tick()=logic.tick(context)
        fun close()=ActionHandler.returnItems(context,logic,player,true)
        fun count(item:Item)=player.inventory.items.filter { it.item==item }.sumOf { it.count }
    }
    fun start(source:CommandSourceStack) {
        check(!jobs.containsKey(source.server)) { "Thermal verification is already running" }
        val level=source.level
        val plans=WorkshopPlans.forming(level)
        val plan=plans.firstOrNull { it.recipe.getResultItem(level.registryAccess()).item==TemperContent.item("bronze_sword") }
            ?: error("The bronze sword is not reachable through the shaping station")
        val probe=Fixture(source,"shaping_anvil")
        for(candidate in plans) {
            val grid=candidate.ghosts.map { if(WorkshopPlans.metal(it)) ThermalState.heated(it,level) else it.copy() }
            check(candidate.matches(grid,level) && candidate.hot(grid,level)) { "Unreachable thermal pattern: "+candidate.id }
            val output=candidate.output(grid,level,probe.player,candidate.required())
            check(!output.isEmpty && output.count==1 && ThermalState.hot(output,level)) { "Invalid thermal output: "+candidate.id }
            val saved=ItemStack.parseOptional(level.registryAccess(),output.save(level.registryAccess()) as CompoundTag)
            val cooled=ThermalState.quenched(saved,level)
            check(WorkshopPlans.basicWeapon(cooled) && !ThermalState.pending(cooled)) { "Unquenchable basic pattern: "+candidate.id }
            if(candidate.native) check(NativeSmithing.quality(cooled) in listOf(null,"NONE","POOR","WELL"))
        }
        if(!NativeSmithing.loaded()) {
            for(id in listOf("minecraft:iron_sword","minecraft:golden_sword","temperandtang:bronze_sword"))
                check(level.recipeManager.byKey(ResourceLocation.parse(id)).orElseThrow().value() is ShapingRecipe) { "Ordinary grid bypass: "+id }
        }
        val iron=ItemStack(Items.IRON_INGOT,8)
        val heating=WorkshopPlans.heating(iron,level)?:error("Iron cannot be heated")
        val cooledIron=ThermalState.quenched(ThermalState.heated(heating.output,level),level)
        check(cooledIron.`is`(Items.IRON_INGOT) && cooledIron.count==8 && !ThermalState.hot(cooledIron,level)) { "Native hot ingot cooling lost batch count" }
        TemperAndTang.logger.info("TEMPER_THERMAL_PATTERNS_PASSED: {} heatable, matchable, serialized and quenchable basic patterns",plans.size)
        val hearth=Fixture(source,"charcoal_hearth")
        val shaper=Fixture(source,"shaping_anvil")
        val trough=Fixture(source,"quenching_trough")
        val bronze=TemperContent.item("bronze_ingot")
        check(!WorkshopPlans.heatable(ItemStack(Items.RAW_IRON))) { "Hearth smelts raw ores" }
        hearth.put(T.STOCK,ItemStack(bronze,8));hearth.press("heat")
        check(hearth.state.getControl("progress")==0f && hearth.take().isEmpty)
        hearth.put(T.FUEL,ItemStack(Items.CHARCOAL,4));hearth.press("heat",false)
        val spent=4-hearth.state.getItem(T.FUEL).count
        check(spent==ThermalRules.coalCost(ThermalRules.heatingTicks(TemperAndTang.config.heatingTicks.get(),8))) { "Wrong batch fuel charge" }
        repeat(10) { hearth.press("heat") }
        check(hearth.state.getItem(T.FUEL).count==4-spent) { "Duplicate heat action consumed extra fuel" }
        var phase=0
        var blows=0
        var next=level.gameTime
        var shaped=ItemStack.EMPTY
        var deadline=level.gameTime+4000
        jobs[source.server]=Job(source) {
            check(level.gameTime<deadline) { "Thermal fixture timed out at phase $phase" }
            hearth.tick();shaper.tick();trough.tick()
            when(phase) {
                0 -> if(!hearth.state.getOutput("result").isEmpty) {
                    val hot=hearth.take()
                    check(hot.count==8 && ThermalState.hot(hot,level)) { "Hot batch count/state lost" }
                    check(hearth.take().isEmpty) { "Heated output duplicated" }
                    check(ThermalState.hot(ItemStack.parseOptional(level.registryAccess(),hot.save(level.registryAccess()) as CompoundTag),level)) { "Heat failed save/reload" }
                    // The native recipe pattern remains authoritative; the browser must select the same recipe.
                    repeat(plans.size) { if(shaper.state.data().getString("target")!="temperandtang:bronze_sword") shaper.press("next") }
                    check(shaper.state.data().getString("target")=="temperandtang:bronze_sword")
                    plan.ghosts.forEachIndexed { i,stack -> shaper.put(T.grid(i),stack) }
                    shaper.put(T.HAMMER,ItemStack(TemperContent.item("smithing_hammer")))
                    shaper.press("strike")
                    check(shaper.state.getControl("progress")==0f) { "Cold stock forged" }
                    plan.ghosts.forEachIndexed { i,stack -> shaper.put(T.grid(i),if(WorkshopPlans.metal(stack)) ThermalState.heated(stack,level) else stack) }
                    next=level.gameTime+TemperAndTang.config.shapingBeatTicks.get()/2
                    phase=1
                }
                1 -> if(level.gameTime>=next) {
                    shaper.press("strike",blows%2==0);blows++
                    check(shaper.state.getControl("progress").toInt()==blows) { "Shaping blow ignored" }
                    repeat(5) { shaper.press("strike") }
                    check(shaper.state.getControl("progress").toInt()==blows) { "Strike spam bypassed beat" }
                    if(blows==plan.required()) {
                        shaped=shaper.state.getItem(T.WORKPIECE).copy()
                        check(!shaped.isEmpty && ThermalState.hot(shaped,level)) { "No hot shaped weapon" }
                        check((0..8).all { shaper.state.getItem(T.grid(it)).isEmpty }) { "Shaping ingredients not consumed" }
                        check(shaper.state.getItem(T.HAMMER).damageValue==plan.required()) { "Hammer wear incorrect" }
                        if(NativeSmithing.loaded()) check(NativeSmithing.quality(shaped) in listOf(null,"NONE","POOR","WELL")) { "Blueprint quality bypassed" }
                        check(WeaponFinishes.preview(shaped,Finish.TEMPERED).isEmpty) { "Hot weapon can be finished" }
                        shaper.close()
                        check(shaper.count(shaped.item)==1) { "Completed workpiece lost on close" }
                        check(shaper.state.getItem(T.WORKPIECE).isEmpty)
                        shaped=ItemStack.parseOptional(level.registryAccess(),shaped.save(level.registryAccess()) as CompoundTag)
                        shaped.set(DataComponents.CUSTOM_NAME,Component.literal("Quench preservation"))
                        shaped.set(DataComponents.CUSTOM_DATA,CustomData.of(shaped.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().also { it.putString("foreign_mod","preserved") }))
                        if(!ThermalState.blank(shaped)) {
                            shaped.damageValue=7
                            shaped.enchant(level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.UNBREAKING),2)
                        }
                        trough.put(T.STOCK,shaped);trough.press("quench")
                        check(trough.take().isEmpty) { "Quenched without water" }
                        trough.put(T.WATER,ItemStack(Items.WATER_BUCKET));trough.press("quench",false)
                        check(trough.state.getItem(T.WATER).`is`(Items.BUCKET)) { "Water not consumed" }
                        repeat(5) { trough.press("quench") }
                        phase=2
                    } else next+=TemperAndTang.config.shapingBeatTicks.get()
                }
                2 -> if(!trough.state.getOutput("result").isEmpty) {
                    val result=trough.take()
                    check(result.item==TemperContent.item("bronze_sword") && result.count==1 && !ThermalState.hot(result,level)) { "Quench did not produce cooled weapon" }
                    check(!ThermalState.pending(result))
                    check(result.get(DataComponents.CUSTOM_NAME)==shaped.get(DataComponents.CUSTOM_NAME))
                    check(result.get(DataComponents.CUSTOM_DATA)?.copyTag()?.getString("foreign_mod")=="preserved")
                    if(!ThermalState.blank(shaped)) {
                        check(result.damageValue==7 && result.get(DataComponents.ENCHANTMENTS)==shaped.get(DataComponents.ENCHANTMENTS))
                        check(NativeSmithing.quality(result)==NativeSmithing.quality(shaped))
                    }
                    check(!WeaponFinishes.preview(result,Finish.TEMPERED).isEmpty) { "Cooled weapon cannot be finished" }
                    check(trough.take().isEmpty) { "Quenched output duplicated" }
                    trough.close();check(trough.count(Items.BUCKET)==1)
                    // Closing active work returns materials but never refunds already-spent heat fuel.
                    val cancel=Fixture(source,"charcoal_hearth")
                    cancel.put(T.STOCK,ItemStack(bronze,3));cancel.put(T.FUEL,ItemStack(Items.CHARCOAL,2));cancel.press("heat")
                    val left=cancel.state.getItem(T.FUEL).count
                    cancel.close();check(cancel.count(bronze)==3 && cancel.count(Items.CHARCOAL)==left)
                    val cancelledQuench=Fixture(source,"quenching_trough")
                    cancelledQuench.put(T.STOCK,ThermalState.heated(ItemStack(bronze,8),level))
                    cancelledQuench.put(T.WATER,ItemStack(Items.WATER_BUCKET));cancelledQuench.press("quench")
                    cancelledQuench.close()
                    check(cancelledQuench.count(bronze)==8 && cancelledQuench.count(Items.BUCKET)==1 && cancelledQuench.count(Items.WATER_BUCKET)==0) { "Quench cancellation refunded water or lost stock" }
                    val partial=Fixture(source,"shaping_anvil")
                    plan.ghosts.forEachIndexed { i,stack -> partial.put(T.grid(i),if(WorkshopPlans.metal(stack)) ThermalState.heated(stack,level) else stack) }
                    partial.put(T.HAMMER,ItemStack(TemperContent.item("smithing_hammer")));partial.press("strike")
                    partial.close()
                    check(partial.player.inventory.items.filter {it.item==TemperContent.item("smithing_hammer")}.single().damageValue==0) { "Partial shape charged tool wear" }
                    check(partial.count(bronze)==plan.ghosts.filter { it.item==bronze }.sumOf { it.count }) { "Partial shape lost ingredients" }
                    val cool=ThermalState.airCooled(ThermalState.heated(ItemStack(bronze,8),level),level)
                    check(cool.count==8 && !ThermalState.hot(cool,level))
                    TemperAndTang.logger.info("TEMPER_THERMAL_VERIFY_PASSED: batch heating, fuel and water charges, cold/spam gates, native patterns/quality, committed cancellation, components and serialized heat")
                    phase=3
                }
            }
            phase==3
        }
        source.sendSuccess({Component.literal("Running isolated heating, shaping and quenching scenarios...")},false)
    }
    fun tick(server:MinecraftServer) {
        val job=jobs[server]?:return
        try { if(job.step()) { jobs.remove(server);job.source.sendSuccess({Component.literal("Thermal workshop scenarios passed.")},false) } }
        catch(failure:Exception) { jobs.remove(server);job.source.sendFailure(Component.literal("Thermal verification failed: "+failure.message));TemperAndTang.logger.error("TEMPER_THERMAL_VERIFY_FAILED",failure) }
    }
    fun stop(server:MinecraftServer) { jobs.remove(server) }
}
