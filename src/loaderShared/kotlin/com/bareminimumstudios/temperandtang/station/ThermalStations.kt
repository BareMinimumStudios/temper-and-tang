package com.bareminimumstudios.temperandtang.station

import com.bareminimumstudios.temperandtang.TemperAndTang
import com.bareminimumstudios.temperandtang.compat.NativeSmithing
import com.bareminimumstudios.temperandtang.content.TemperContent
import com.bareminimumstudios.temperandtang.smithing.*
import com.pockyl.diegesis.api.Diegesis
import com.pockyl.diegesis.api.state.*
import com.pockyl.diegesis.api.station.*
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.tags.ItemTags
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

object ThermalStations {
    val STOCK = SlotRef.of("stock")
    val FUEL = SlotRef.of("fuel")
    val WATER = SlotRef.of("water")
    val HAMMER = SlotRef.of("hammer")
    val WORKPIECE = SlotRef.of("workpiece")
    fun grid(index: Int) = SlotRef.of("grid", index)
    private fun active() = TemperAndTang.config.thermalEnabled.get()
    private fun snapshot(a: ItemStack, b: ItemStack) = a.count == b.count && ItemStack.isSameItemSameComponents(a,b)
    private fun sound(s: StationContext, event: net.minecraft.sounds.SoundEvent, volume: Float, pitch: Float) =
        s.level().playSound(null,s.pos(),event,SoundSource.BLOCKS,volume,pitch)
    private fun note(s: StationContext, status: String, target: ItemStack = ItemStack.EMPTY, ghosts: List<ItemStack>? = null) {
        val before = s.state().data()
        val data = before.copy()
        data.putString("status", status)
        if (!target.isEmpty) data.putString("target", BuiltInRegistries.ITEM.getKey(target.item).toString())
        if (ghosts != null) for (i in 0..8) data.putString("ghost_$i", if (ghosts[i].isEmpty) "" else BuiltInRegistries.ITEM.getKey(ghosts[i].item).toString())
        if (data != before) s.state().setData(data)
    }
    fun register() {
        Diegesis.station(TemperAndTang.id("charcoal_hearth")).block { TemperContent.block("charcoal_hearth") }
            .schema(StationSchema.builder()
                .slots("stock",1,SlotRules.DEFAULT.maxStack(8).accepts(WorkshopPlans::heatable))
                .slots("fuel",1,SlotRules.DEFAULT.accepts { it.`is`(ItemTags.COALS) })
                .control("heat",ControlSpec.button()).control("progress",ControlSpec.range(0f,100f,0f).locked())
                .control("fuel_cost",ControlSpec.range(0f,64f,1f).locked()).output("result").build())
            .exclusive().openWhen { _,_,_ -> active() }.logic { Hearth() }.register()
        Diegesis.station(TemperAndTang.id("shaping_anvil")).block { TemperContent.block("shaping_anvil") }
            .schema(StationSchema.builder()
                .slots("grid",9,SlotRules.DEFAULT.maxStack(1))
                .slots("hammer",1,SlotRules.DEFAULT.maxStack(1).accepts(FinishingStation::isHammer))
                .slots("workpiece",1,SlotRules.DEFAULT.maxStack(1).accepts { false })
                .control("strike",ControlSpec.button()).control("next",ControlSpec.button()).control("previous",ControlSpec.button())
                .control("progress",ControlSpec.range(0f,40f,0f).locked()).control("required",ControlSpec.range(1f,40f,6f).locked())
                .control("clean",ControlSpec.range(0f,40f,0f).locked()).control("beat",ControlSpec.range(0f,1f,0f).locked())
                .output("result").build())
            .exclusive().openWhen { _,_,_ -> active() }.logic { Shaper() }.register()
        Diegesis.station(TemperAndTang.id("quenching_trough")).block { TemperContent.block("quenching_trough") }
            .schema(StationSchema.builder()
                .slots("stock",1,SlotRules.DEFAULT.maxStack(8).accepts { ThermalState.pending(it) || WorkshopPlans.metal(it) })
                .slots("water",1,SlotRules.DEFAULT.maxStack(1).accepts { it.`is`(Items.WATER_BUCKET) || it.`is`(Items.BUCKET) })
                .control("quench",ControlSpec.button()).control("progress",ControlSpec.range(0f,100f,0f).locked())
                .output("result").build())
            .exclusive().openWhen { _,_,_ -> active() }.logic { Trough() }.register()
    }
    private class Hearth : StationLogic {
        private var start = -1L
        private var end = -1L
        private var plan: WorkshopPlans.Heating? = null
        private var input = ItemStack.EMPTY
        private var completed = false
        private var previewStock = ItemStack.EMPTY
        private var preview: WorkshopPlans.Heating? = null
        private var previewTick = -20L
        private fun reset(s: StationContext) {
            start = -1; end = -1; completed = false; plan = null
            s.state().setControl("progress",0f); s.state().setOutput("result",ItemStack.EMPTY)
        }
        override fun onSlotChanged(s: StationContext, slot: SlotRef) {
            if (slot == STOCK) {
                reset(s)
                val stamped = ThermalState.stampNativeHeat(s.state().getItem(STOCK),s.level())
                s.state().setItem(STOCK,stamped)
            }
        }
        override fun onCustomAction(s: StationContext, player: Player, action: String, data: CompoundTag) { if (action == "heat") onButton(s,player,action) }
        override fun onButton(s: StationContext, player: Player, control: String) {
            if (s.isClientSide || control != "heat" || !active() || player.isSpectator || start >= 0 || completed) return
            val stock = s.state().getItem(STOCK)
            if (stock.isEmpty || ThermalState.hot(stock,s.level())) { updateOutputs(s); return }
            val candidate = WorkshopPlans.heating(stock,s.level()) ?: return
            val duration = ThermalRules.heatingTicks(candidate.perItem,stock.count)
            val cost = ThermalRules.coalCost(duration)
            val fuel = s.state().getItem(FUEL)
            if (!fuel.`is`(ItemTags.COALS) || fuel.count < cost) { note(s,"fuel"); return }
            plan = candidate; input = stock.copy(); start = s.level().gameTime; end = start + duration
            s.state().setItem(FUEL,fuel.copy().also { it.shrink(cost) })
            sound(s,SoundEvents.FIRECHARGE_USE,0.45f,0.85f)
            updateOutputs(s)
        }
        override fun tick(s: StationContext) {
            if (s.isClientSide) return
            val stock = s.state().getItem(STOCK)
            if (!active()) { reset(s); note(s,"disabled"); return }
            if (completed && !ThermalState.hot(stock,s.level())) {
                s.state().setItem(STOCK,ThermalState.airCooled(stock,s.level())); reset(s)
            }
            if (start >= 0) {
                val candidate = plan
                if (candidate == null || !snapshot(stock,input) || candidate.nativeRecipe?.let { s.level().recipeManager.byKey(it.id()).orElse(null)?.value() !== it.value() } == true) {
                    reset(s)
                } else if (s.level().gameTime >= end) {
                    val hot = ThermalState.heated(candidate.output,s.level())
                    s.state().setItem(STOCK,hot); input = hot.copy(); completed = true; start = -1
                    s.state().setControl("progress",100f)
                    sound(s,SoundEvents.FURNACE_FIRE_CRACKLE,0.5f,1.2f)
                } else {
                    s.state().setControl("progress",100f * (s.level().gameTime-start).toFloat()/(end-start).toFloat())
                    if (s.level().gameTime % 40L == 0L) sound(s,SoundEvents.FURNACE_FIRE_CRACKLE,0.25f,1f)
                }
            }
            updateOutputs(s)
        }
        override fun updateOutputs(s: StationContext) {
            val stock = s.state().getItem(STOCK)
            val ready = active() && completed && snapshot(stock,input) && ThermalState.hot(stock,s.level())
            s.state().setOutput("result",if (ready) stock.copy() else ItemStack.EMPTY)
            val candidate = if (start < 0 && !completed) {
                if (!snapshot(stock,previewStock) || s.level().gameTime-previewTick >= 20) {
                    previewStock=stock.copy(); preview=WorkshopPlans.heating(stock,s.level()); previewTick=s.level().gameTime
                }
                preview
            } else plan
            s.state().setControl("fuel_cost",candidate?.let { ThermalRules.coalCost(ThermalRules.heatingTicks(it.perItem,stock.count)).toFloat() } ?: 1f)
            note(s,when { !active()->"disabled"; ready->"ready"; start>=0->"heating"; stock.isEmpty->"stock"; ThermalState.hot(stock,s.level())->"already_hot"; candidate != null && s.state().getItem(FUEL).count < s.state().getControl("fuel_cost").toInt()->"fuel"; else->"idle" },stock)
        }
        override fun takeOutput(s: StationContext, player: Player, output: String): ItemStack {
            val stock = s.state().getItem(STOCK)
            if (s.isClientSide || player.isSpectator || output != "result" || !active() || !completed || !snapshot(stock,input) || !ThermalState.hot(stock,s.level())) return ItemStack.EMPTY
            val result = stock.copy(); s.state().setItem(STOCK,ItemStack.EMPTY); reset(s)
            return result
        }
    }
    private class Shaper : StationLogic {
        private var plans = emptyList<WorkshopPlans.Forming>()
        private var index = 0
        private var origin = 0L
        private var lastCycle = -1L
        private var hits = 0
        private var clean = 0
        private fun stocks(s: StationContext) = (0..8).map { s.state().getItem(grid(it)) }
        private fun reset(s: StationContext) {
            hits = 0; clean = 0; lastCycle = -1; origin = s.level().gameTime
            s.state().setControl("progress",0f); s.state().setControl("clean",0f)
        }
        private fun refresh(s: StationContext) {
            val previous = plans.getOrNull(index)?.id
            plans = WorkshopPlans.forming(s.level())
            index = plans.indexOfFirst { it.id == previous }.takeIf { it >= 0 }
                ?: plans.indexOfFirst { it.id == TemperAndTang.id("bronze_sword") }.takeIf { it >= 0 } ?: 0
            reset(s)
        }
        override fun onOpen(s: StationContext, player: Player) { refresh(s); updateOutputs(s) }
        override fun onSlotChanged(s: StationContext, slot: SlotRef) {
            if (slot.group() == "grid") s.state().setItem(slot,ThermalState.stampNativeHeat(s.state().getItem(slot),s.level()))
            if (s.state().getItem(WORKPIECE).isEmpty) reset(s)
        }
        override fun onCustomAction(s: StationContext, player: Player, action: String, data: CompoundTag) {
            if (action in setOf("strike","next","previous")) onButton(s,player,action)
        }
        override fun onButton(s: StationContext, player: Player, control: String) {
            if (s.isClientSide || !active() || player.isSpectator || !s.state().getItem(WORKPIECE).isEmpty) return
            if (plans.isEmpty()) refresh(s)
            if (control == "next" || control == "previous") {
                if (plans.isNotEmpty()) index = Math.floorMod(index + if (control == "next") 1 else -1,plans.size)
                reset(s); updateOutputs(s); return
            }
            if (control != "strike") return
            val plan = plans.getOrNull(index) ?: return
            val material = stocks(s)
            val hammer = s.state().getItem(HAMMER)
            if (!plan.current(s.level()) || !plan.matches(material,s.level()) || !plan.hot(material,s.level()) || !FinishingStation.isHammer(hammer) ||
                hammer.maxDamage-hammer.damageValue < plan.required() || !NativeSmithing.hammerAllowed(hammer)) { updateOutputs(s); return }
            val now = s.level().gameTime
            val period = TemperAndTang.config.shapingBeatTicks.get()
            val cycle = (now-origin)/period
            if (now < origin || cycle == lastCycle) return
            lastCycle = cycle; hits++
            if (ThermalRules.cleanBeat(now,origin,period,TemperAndTang.config.shapingWindowTicks.get())) clean++
            s.state().setControl("progress",hits.toFloat()); s.state().setControl("clean",clean.toFloat())
            sound(s,SoundEvents.ANVIL_HIT,0.8f,1.05f + (hits%3)*0.05f)
            if (hits >= plan.required()) {
                // Commit on the last blow: closing a ready station returns the actual workpiece, not free reroll stock.
                val result = plan.output(material,s.level(),player,clean)
                if (result.isEmpty || result.count != 1) return
                val remaining = plan.remainders(material)
                val worn = hammer.copy().also { it.damageValue += plan.required() }
                for (i in 0..8) s.state().setItem(grid(i),remaining.getOrNull(i)?.copy() ?: ItemStack.EMPTY)
                s.state().setItem(HAMMER,if (worn.damageValue >= worn.maxDamage) ItemStack.EMPTY else worn)
                s.state().setItem(WORKPIECE,result)
                sound(s,SoundEvents.ANVIL_USE,0.55f,1.1f)
            }
            updateOutputs(s)
        }
        override fun tick(s: StationContext) {
            if (s.isClientSide) return
            if (!active()) { note(s,"disabled"); s.state().setOutput("result",ItemStack.EMPTY); return }
            if (plans.isNotEmpty() && s.level().gameTime % 20L == 0L && plans.getOrNull(index)?.current(s.level()) == false) refresh(s)
            var changed = false
            for (i in 0..8) {
                val stack = s.state().getItem(grid(i))
                if (ThermalState.data(stack).getLong("hot_until") > 0 && !ThermalState.hot(stack,s.level())) {
                    s.state().setItem(grid(i),ThermalState.airCooled(stack,s.level())); changed = true
                }
            }
            if (changed) reset(s)
            val cue = ThermalRules.cleanBeat(s.level().gameTime,origin,TemperAndTang.config.shapingBeatTicks.get(),TemperAndTang.config.shapingWindowTicks.get())
            s.state().setControl("beat",if (cue) 1f else 0f)
            if (changed || s.level().gameTime % 10L == 0L) updateOutputs(s)
        }
        override fun updateOutputs(s: StationContext) {
            val actual = s.state().getItem(WORKPIECE)
            s.state().setOutput("result",if (active()) actual.copy() else ItemStack.EMPTY)
            val plan = plans.getOrNull(index)
            if (plan == null) { note(s,"unsupported"); return }
            val target = plan.recipe.getResultItem(s.level().registryAccess())
            s.state().setControl("required",plan.required().toFloat())
            val material = stocks(s)
            val hammer = s.state().getItem(HAMMER)
            val status = when {
                !active()->"disabled"
                !actual.isEmpty->"ready"
                !plan.matches(material,s.level())->"pattern"
                !plan.hot(material,s.level())->"hot"
                !FinishingStation.isHammer(hammer) || hammer.maxDamage-hammer.damageValue < plan.required()->"hammer"
                !NativeSmithing.hammerAllowed(hammer)->"tier"
                else->"work"
            }
            note(s,status,target,plan.ghosts)
        }
        override fun takeOutput(s: StationContext, player: Player, output: String): ItemStack {
            if (s.isClientSide || player.isSpectator || output != "result" || !active()) return ItemStack.EMPTY
            val result = s.state().getItem(WORKPIECE).copy()
            if (result.isEmpty) return result
            s.state().setItem(WORKPIECE,ItemStack.EMPTY); reset(s)
            return result
        }
    }
    private class Trough : StationLogic {
        private var start = -1L
        private var end = -1L
        private var input = ItemStack.EMPTY
        private var completed = false
        private fun reset(s: StationContext) { start=-1; end=-1; completed=false; s.state().setControl("progress",0f); s.state().setOutput("result",ItemStack.EMPTY) }
        override fun onSlotChanged(s: StationContext, slot: SlotRef) {
            if (slot == STOCK) { reset(s); s.state().setItem(STOCK,ThermalState.stampNativeHeat(s.state().getItem(STOCK),s.level())) }
        }
        override fun onCustomAction(s: StationContext, player: Player, action: String, data: CompoundTag) { if (action == "quench") onButton(s,player,action) }
        override fun onButton(s: StationContext, player: Player, control: String) {
            if (s.isClientSide || !active() || player.isSpectator || control != "quench" || start>=0 || completed) return
            val stock = s.state().getItem(STOCK)
            if (!ThermalState.hot(stock,s.level()) || !s.state().getItem(WATER).`is`(Items.WATER_BUCKET)) { updateOutputs(s); return }
            if (ThermalState.quenched(stock,s.level()).isEmpty) { note(s,"invalid"); return }
            input=stock.copy(); start=s.level().gameTime; end=start+TemperAndTang.config.quenchingTicks.get()
            s.state().setItem(WATER,ItemStack(Items.BUCKET))
            sound(s,SoundEvents.FIRE_EXTINGUISH,0.7f,1f)
            updateOutputs(s)
        }
        override fun tick(s: StationContext) {
            if (s.isClientSide) return
            if (!active()) { reset(s); note(s,"disabled"); return }
            val stock=s.state().getItem(STOCK)
            if (start>=0) {
                if (!snapshot(stock,input)) reset(s)
                else if (s.level().gameTime>=end) {
                    val cold=ThermalState.quenched(stock,s.level())
                    if (!cold.isEmpty) {
                        s.state().setItem(STOCK,cold); input=cold.copy(); completed=true; start=-1
                        s.state().setControl("progress",100f)
                        sound(s,SoundEvents.BUCKET_EMPTY,0.4f,0.9f)
                    } else reset(s)
                } else s.state().setControl("progress",100f*(s.level().gameTime-start).toFloat()/(end-start).toFloat())
            } else if (!completed && ThermalState.data(stock).getLong("hot_until")>0 && !ThermalState.hot(stock,s.level())) {
                s.state().setItem(STOCK,ThermalState.airCooled(stock,s.level()))
            }
            updateOutputs(s)
        }
        override fun updateOutputs(s: StationContext) {
            val stock=s.state().getItem(STOCK)
            val ready=active() && completed && snapshot(stock,input)
            s.state().setOutput("result",if (ready) stock.copy() else ItemStack.EMPTY)
            note(s,when { !active()->"disabled";ready->"ready";start>=0->"quenching";stock.isEmpty->"stock";!ThermalState.hot(stock,s.level())->"cold";!s.state().getItem(WATER).`is`(Items.WATER_BUCKET)->"water";else->"idle" },stock)
        }
        override fun takeOutput(s: StationContext, player: Player, output: String): ItemStack {
            val stock=s.state().getItem(STOCK)
            if (s.isClientSide || !active() || player.isSpectator || output!="result" || !completed || !snapshot(stock,input)) return ItemStack.EMPTY
            val result=stock.copy(); s.state().setItem(STOCK,ItemStack.EMPTY); reset(s)
            return result
        }
    }
}
