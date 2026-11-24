package com.bareminimumstudios.temperandtang.station

import com.bareminimumstudios.temperandtang.TemperAndTang
import com.bareminimumstudios.temperandtang.content.TemperContent
import com.bareminimumstudios.temperandtang.smithing.*
import com.pockyl.diegesis.api.Diegesis
import com.pockyl.diegesis.api.state.*
import com.pockyl.diegesis.api.station.*
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

object ThermalStations {
    val STOCK = SlotRef.of("stock")
    val ADDITIVE = SlotRef.of("additive")
    val FUEL = SlotRef.of("fuel")
    val WATER = SlotRef.of("water")
    val HAMMER = SlotRef.of("hammer")
    val WORKPIECE = SlotRef.of("workpiece")
    val GEAR = SlotRef.of("gear")
    val FLUX = SlotRef.of("flux")
    val BLUEPRINT = SlotRef.of("blueprint")
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
        for(name in listOf("charcoal_hearth","alloy_furnace","nether_alloy_furnace")) Diegesis.station(TemperAndTang.id(name)).block { TemperContent.block(name) }
            .anchor(com.bareminimumstudios.temperandtang.content.WorkshopBlock::anchor)
            .schema(StationSchema.builder()
                .slots("stock",1,SlotRules.DEFAULT.maxStack(8).accepts { if(name=="charcoal_hearth")WorkshopPlans.heatable(it) else WorkshopPlans.stock(it) })
                .slots("fuel",1,SlotRules.DEFAULT.accepts(WorkshopFuel::accepts))
                .slots("additive",1,SlotRules.DEFAULT.maxStack(8).accepts {name!="charcoal_hearth" && it.`is`(com.bareminimumstudios.temperandtang.api.TemperAndTangApi.ALLOY_ADDITIVES)})
                .control("heat",ControlSpec.button()).control("progress",ControlSpec.range(0f,100f,0f).locked())
                .control("fuel_cost",ControlSpec.range(0f,64f,1f).locked()).output("result").build())
            .hidesBlocks(com.bareminimumstudios.temperandtang.content.WorkshopBlock::span).exclusive().openWhen { player,pos,state -> active() && com.bareminimumstudios.temperandtang.content.WorkshopBlock.ensureSpace(player.level(),pos,state,player) }.logic { Hearth() }.register()
        Diegesis.station(TemperAndTang.id("shaping_anvil")).block { TemperContent.block("shaping_anvil") }
            .blocks(net.minecraft.world.level.block.Blocks.ANVIL,net.minecraft.world.level.block.Blocks.CHIPPED_ANVIL,net.minecraft.world.level.block.Blocks.DAMAGED_ANVIL)
            .anchor(com.bareminimumstudios.temperandtang.content.WorkshopBlock::anchor)
            .schema(StationSchema.builder()
                .slots("grid",9,SlotRules.DEFAULT.maxStack(1))
                .slots("hammer",1,SlotRules.DEFAULT.maxStack(1).accepts(FinishingStation::isHammer))
                .slots("workpiece",1,SlotRules.DEFAULT.maxStack(8).accepts { false })
                .slots("gear",1,SlotRules.DEFAULT.maxStack(1).accepts(GearSupport::supported))
                .slots("flux",1,SlotRules.DEFAULT.accepts {it.item==TemperContent.item("smithing_flux")})
                .slots("blueprint",1,SlotRules.DEFAULT.maxStack(1).accepts {Blueprints.blank(it) || Blueprints.filled(it)})
                .control("scribe",ControlSpec.button()).control("category",ControlSpec.button()).control("mode",ControlSpec.button())
                .control("accuracy",ControlSpec.range(0f,100f,0f).locked()).control("misses",ControlSpec.range(0f,10000f,0f).locked())
                .control("target",ControlSpec.range(0f,120f,0f).locked())
                .control("period",ControlSpec.range(0f,120f,0f).locked())
                .control("perfect_window",ControlSpec.range(0f,8f,0f).locked())
                .control("phrase_size",ControlSpec.range(0f,3f,0f).locked())
                .control("phrase_step",ControlSpec.range(0f,3f,0f).locked())
                .control("next_gap",ControlSpec.range(0f,120f,0f).locked())
                .control("combo",ControlSpec.range(0f,40f,0f).locked()).control("window",ControlSpec.range(0f,10f,0f).locked())
                .control("phase",ControlSpec.range(0f,1f,0f).locked()).control("last_score",ControlSpec.range(0f,1f,0f).locked())
                .control("strike",ControlSpec.button()).control("next",ControlSpec.button()).control("previous",ControlSpec.button())
                .control("progress",ControlSpec.range(0f,40f,0f).locked()).control("required",ControlSpec.range(1f,40f,6f).locked())
                .control("clean",ControlSpec.range(0f,40f,0f).locked()).control("beat",ControlSpec.range(0f,1f,0f).locked())
                .output("result").build())
            .exclusive().openWhen { player,pos,state -> active() && com.bareminimumstudios.temperandtang.content.WorkshopBlock.ensureSpace(player.level(),pos,state,player) && (state.`is`(TemperContent.block("shaping_anvil")) || TemperAndTang.config.vanillaAnvilWorkshop.get() && player.isShiftKeyDown) }.logic { Shaper() }.register()
        Diegesis.station(TemperAndTang.id("quenching_trough")).block { TemperContent.block("quenching_trough") }
            .anchor(com.bareminimumstudios.temperandtang.content.WorkshopBlock::anchor)
            .schema(StationSchema.builder()
                .slots("stock",1,SlotRules.DEFAULT.maxStack(8).accepts { ThermalState.pending(it) || WorkshopPlans.metal(it) })
                .slots("water",1,SlotRules.DEFAULT.maxStack(1).accepts { it.`is`(Items.WATER_BUCKET) || it.`is`(Items.BUCKET) })
                .control("quench",ControlSpec.button()).control("progress",ControlSpec.range(0f,100f,0f).locked())
                .output("result").build())
            .exclusive().openWhen { player,pos,state -> active() && com.bareminimumstudios.temperandtang.content.WorkshopBlock.ensureSpace(player.level(),pos,state,player) }.logic { Trough() }.register()
    }
    private class Hearth : StationLogic {
        private fun resolve(s:StationContext,stock:ItemStack):WorkshopPlans.Heating? {
            if(s.blockState().`is`(TemperContent.block("charcoal_hearth")))return if(s.state().getItem(ADDITIVE).isEmpty)WorkshopPlans.heating(stock,s.level())else null
            return HearthProcesses.alloy(stock,s.state().getItem(ADDITIVE),s.level())?.takeIf {!it.requiresAdvanced || s.blockState().`is`(TemperContent.block("nether_alloy_furnace"))}
        }
        override fun onOpen(s:StationContext,player:Player)=WorkshopRecovery.opened(s,player)
        override fun onClose(s:StationContext,player:Player)=WorkshopRecovery.closed(s,player)
        private var start = -1L
        private var end = -1L
        private var plan: WorkshopPlans.Heating? = null
        private var input = ItemStack.EMPTY
        private var completed = false
        private var supplement=ItemStack.EMPTY
        private var previewSupplement=ItemStack.EMPTY
        private var committing=false
        private var previewStock = ItemStack.EMPTY
        private var preview: WorkshopPlans.Heating? = null
        private var previewTick = -20L
        private fun reset(s: StationContext) {
            start = -1; end = -1; completed = false; plan = null
            s.state().setControl("progress",0f); s.state().setOutput("result",ItemStack.EMPTY)
        }
        override fun onSlotChanged(s: StationContext, slot: SlotRef) {
            if (!committing && (slot == STOCK || slot == ADDITIVE)) {
                reset(s)
                val stamped = ThermalState.normalize(s.state().getItem(STOCK),s.level())
                if(!snapshot(stamped,s.state().getItem(STOCK)))s.state().setItem(STOCK,stamped)
            }
        }
        override fun onCustomAction(s: StationContext, player: Player, action: String, data: CompoundTag) { if (action == "heat") onButton(s,player,action) }
        override fun onButton(s: StationContext, player: Player, control: String) {
            if (s.isClientSide || control != "heat" || !active() || player.isSpectator || start >= 0 || completed) return
            val stock = s.state().getItem(STOCK)
            if (stock.isEmpty || ThermalState.hot(stock,s.level()) && s.state().getItem(ADDITIVE).isEmpty) { updateOutputs(s); return }
            val candidate = resolve(s,stock) ?: return
            val duration = HearthProcesses.duration(candidate,stock.count)
            val fuel = s.state().getItem(FUEL)
            if (!WorkshopFuel.sufficient(fuel,duration) || !WorkshopFuel.remainder(fuel).isEmpty && fuel.count!=1) { note(s,"fuel"); return }
            plan = candidate; input = stock.copy(); supplement=s.state().getItem(ADDITIVE).copy(); start = s.level().gameTime; end = start + duration
            s.state().setItem(FUEL,WorkshopFuel.spend(fuel,duration))
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
                if (candidate == null || !snapshot(stock,input) || !snapshot(s.state().getItem(ADDITIVE),supplement) || candidate.nativeRecipe?.let { s.level().recipeManager.byKey(it.id()).orElse(null)?.value() !== it.value() } == true) {
                    reset(s)
                } else if (s.level().gameTime >= end) {
                    val hot = ThermalState.heated(candidate.output,s.level())
                    committing=true
                    try {
                        if(candidate.additiveCost>0)s.state().setItem(ADDITIVE,s.state().getItem(ADDITIVE).copy().also {it.shrink(candidate.additiveCost)})
                        s.state().setItem(STOCK,hot)
                    } finally {committing=false}
                    input = hot.copy(); supplement=s.state().getItem(ADDITIVE).copy(); completed = true; start = -1
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
                if (!snapshot(stock,previewStock) || !snapshot(s.state().getItem(ADDITIVE),previewSupplement) || s.level().gameTime-previewTick >= 20) {
                    previewStock=stock.copy(); previewSupplement=s.state().getItem(ADDITIVE).copy(); preview=resolve(s,stock); previewTick=s.level().gameTime
                }
                preview
            } else plan
            s.state().setControl("fuel_cost",candidate?.let { WorkshopFuel.cost(s.state().getItem(FUEL),HearthProcesses.duration(it,stock.count)).toFloat() } ?: 0f)
            note(s,when { !active()->"disabled"; ready->"ready"; start>=0->"heating"; stock.isEmpty->"stock"; ThermalState.hot(stock,s.level()) && s.state().getItem(ADDITIVE).isEmpty->"already_hot"; candidate != null && !WorkshopFuel.sufficient(s.state().getItem(FUEL),HearthProcesses.duration(candidate,stock.count))->"fuel"; candidate==null->"recipe"; else->"idle" },candidate?.output?:stock)
            val metadata=s.state().data().copy()
            val process=candidate?.process ?: when {
                !s.state().getItem(ADDITIVE).isEmpty -> "alloy"
                HearthProcesses.refinable(stock) -> "refine"
                else -> "heat"
            }
            metadata.putString("process",process)
            metadata.putInt("yield",candidate?.output?.count?:0)
            if(metadata!=s.state().data())s.state().setData(metadata)
        }
        override fun takeOutput(s: StationContext, player: Player, output: String): ItemStack {
            val stock = s.state().getItem(STOCK)
            if (s.isClientSide || player.isSpectator || output != "result" || !active() || !completed || !snapshot(stock,input) || !ThermalState.hot(stock,s.level())) return ItemStack.EMPTY
            val result = stock.copy(); s.state().setItem(STOCK,ItemStack.EMPTY); reset(s)
            return result
        }
    }
    private class Shaper : StationLogic {
        private val timing=WorkshopRhythm.Measurement()
        private data class RefiningAttempt(val plan:WorkshopPlans.Forming,val inputs:List<ItemStack>)
        private var refiningAttempt:RefiningAttempt?=null
        private var plans=emptyList<WorkshopPlans.Forming>()
        private var index=0
        private var category=0
        private var reforgeMode=false
        private var committedTarget=ItemStack.EMPTY
        private var committedCost=0
        private var work:HammerWork?=null
        private fun stocks(s:StationContext)=(0..8).map {s.state().getItem(grid(it))}
        private fun player(s:StationContext)=s.users().firstOrNull()
        private fun plan()=plans.getOrNull(index)
        private fun reset(s:StationContext) {
            val attempt=refiningAttempt
            refiningAttempt=null
            if(attempt!=null && s.state().getItem(WORKPIECE).isEmpty && attempt.inputs.zip(stocks(s)).all {(before,current)->snapshot(before,current)}) {
                val recipe=attempt.plan.recipe as PrecisionRefiningRecipe
                val remaining=attempt.plan.remainders(attempt.inputs)
                val wear=work?.hits?:0
                for(i in 0..8)s.state().setItem(grid(i),remaining.getOrNull(i)?.copy()?:ItemStack.EMPTY)
                val hammer=s.state().getItem(HAMMER).copy().also {it.damageValue+=wear}
                s.state().setItem(HAMMER,if(hammer.damageValue>=hammer.maxDamage)ItemStack.EMPTY else hammer)
                s.state().setItem(WORKPIECE,recipe.failure.copy())
                committedTarget=recipe.failure.copy();committedCost=wear
            }
            work=null
            for(key in listOf("progress","clean","accuracy","misses","beat","target","phase","last_score"))s.state().setControl(key,0f)
        }
        private fun refresh(s:StationContext) {
            val previous=plan()?.id?:net.minecraft.resources.ResourceLocation.tryParse(s.state().data().getString("pattern_id"))
            plans=WorkshopPlans.forming(s.level())
            index=plans.indexOfFirst {it.id==previous}.takeIf {it>=0}?:plans.indexOfFirst {it.id==TemperAndTang.id("bronze_sword")}.takeIf {it>=0}?:0
            reset(s)
        }
        private fun target(s:StationContext)=if(!s.state().getItem(WORKPIECE).isEmpty && !committedTarget.isEmpty)committedTarget else if(!s.state().getItem(GEAR).isEmpty)s.state().getItem(GEAR) else plan()?.recipe?.getResultItem(s.level().registryAccess())?:ItemStack.EMPTY
        private fun required(s:StationContext)=if(!s.state().getItem(WORKPIECE).isEmpty && committedCost>0)committedCost else if(!s.state().getItem(GEAR).isEmpty) TemperAndTang.config.shapingStrikes.get()+if(GearSupport.armor(s.state().getItem(GEAR)))2 else 0 else plan()?.required()?:6
        private fun status(s:StationContext):String {
            if(!active())return "disabled"
            if(!s.state().getItem(WORKPIECE).isEmpty)return "ready"
            val gear=s.state().getItem(GEAR)
            if(reforgeMode && gear.isEmpty)return "insert_gear"
            if(!gear.isEmpty) {
                if(stocks(s).any {!it.isEmpty})return "clear_pattern"
                if(!Craftsmanship.canReforge(ThermalState.airCooled(gear,s.level())))return "repair"
                if(!ThermalState.hot(gear,s.level()))return "hot_gear"
                if(s.state().getItem(FLUX).item!=TemperContent.item("smithing_flux"))return "flux"
            } else {
                val selected=plan()?:return "unsupported"
                if(!selected.current(s.level()))return "reload"
                val target=selected.recipe.getResultItem(s.level().registryAccess())
                if(GearSupport.blueprint(target) && Blueprints.pattern(s.state().getItem(BLUEPRINT))!=selected.id.toString())return "blueprint"
                if(!selected.matches(stocks(s),s.level()))return "pattern"
                if(!selected.hot(stocks(s),s.level()))return "hot"
                if(work==null && !selected.polishing && !Craftsmanship.canReforge(selected.assemble(stocks(s),s.level())))return "repair"
            }
            val hammer=s.state().getItem(HAMMER)
            if(!FinishingStation.isHammer(hammer) || hammer.maxDamage-hammer.damageValue<required(s))return "hammer"
            return "work"
        }
        override fun mayPlace(s:StationContext,player:Player,slot:SlotRef,stack:ItemStack)=refiningAttempt==null
        override fun mayTake(s:StationContext,player:Player,slot:SlotRef)=refiningAttempt==null
        override fun onClose(s:StationContext,player:Player) {if(!s.isClientSide){reset(s);WorkshopRecovery.closed(s,player)}}
        override fun onOpen(s:StationContext,player:Player) {WorkshopRecovery.opened(s,player);refresh(s);timing.begin(s);updateOutputs(s)}
        override fun onSlotChanged(s:StationContext,slot:SlotRef) {
            if(slot==GEAR && !s.state().getItem(GEAR).isEmpty)reforgeMode=true
            if(slot==BLUEPRINT) {
                val match=plans.indexOfFirst {it.id.toString()==Blueprints.pattern(s.state().getItem(BLUEPRINT))}
                if(match>=0) {index=match;category=0}
            }
            if(s.state().getItem(WORKPIECE).isEmpty) {
                if(work==null && !reforgeMode && s.state().getItem(GEAR).isEmpty && plan()?.matches(stocks(s),s.level())!=true) {
                    val matching=plans.indexOfFirst {it.matches(stocks(s),s.level())}
                    if(matching>=0){index=matching;category=0}
                }
                reset(s)
            }
        }
        override fun onCustomAction(s:StationContext,player:Player,action:String,data:CompoundTag) {
            if(action=="timing_ack"){timing.acknowledge(s,player,data);return}
            if(action=="select_pattern") {
                if(s.isClientSide || !active() || player.isSpectator || reforgeMode || work!=null || !s.state().getItem(WORKPIECE).isEmpty)return
                val chosen=plans.indexOfFirst {it.id.toString()==data.getString("pattern")}
                if(chosen<0 || !plans[chosen].current(s.level()))return
                index=chosen;category=0;reset(s);updateOutputs(s);return
            }
            if(action in setOf("strike","next","previous","scribe","category","mode"))onButton(s,player,action)
        }
        override fun onButton(s:StationContext,player:Player,control:String) {
            if(s.isClientSide || !active() || player.isSpectator || !s.state().getItem(WORKPIECE).isEmpty || !player.inventoryMenu.carried.isEmpty)return
            if(refiningAttempt!=null && control!="strike")return
            if(plans.isEmpty())refresh(s)
            if(control=="mode") {
                if(stocks(s).any {!it.isEmpty} || !s.state().getItem(GEAR).isEmpty) {
                    player.displayClientMessage(net.minecraft.network.chat.Component.translatable("temperandtang.craft.clear_mode"),true);return
                }
                reforgeMode=!reforgeMode;reset(s);updateOutputs(s);return
            }
            if(reforgeMode && control in setOf("next","previous","scribe","category"))return
            if(control=="category" || control=="next" || control=="previous") {
                if(control=="category")category=(category+1)%3
                if(plans.isNotEmpty()) {
                    val direction=if(control=="previous")-1 else 1
                    for(i in 1..plans.size) {
                        val candidate=Math.floorMod(index+direction*i,plans.size)
                        val armor=GearSupport.armor(plans[candidate].recipe.getResultItem(s.level().registryAccess()))
                        if(category==0 || category==1 && !armor || category==2 && armor) {index=candidate;break}
                    }
                }
                reset(s);updateOutputs(s);return
            }
            if(control=="scribe") {
                val selected=plan()?:return
                if(!Blueprints.blank(s.state().getItem(BLUEPRINT)))return
                val mastery=player.server?.let {SmithingProgress.get(it).level(player.uuid)}?:0
                if(mastery<TemperAndTang.config.blueprintMastery.get()) {
                    player.displayClientMessage(net.minecraft.network.chat.Component.translatable("temperandtang.thermal.blueprint_mastery",TemperAndTang.config.blueprintMastery.get()),true);return
                }
                s.state().setItem(BLUEPRINT,Blueprints.make(selected.id.toString(),selected.recipe.getResultItem(s.level().registryAccess()).hoverName))
                sound(s,SoundEvents.BOOK_PAGE_TURN,0.6f,1f);reset(s);updateOutputs(s);return
            }
            if(control!="strike")return
            updateOutputs(s)
            if(status(s)!="work")return
            val job=work?:return
            val score=job.strike(WorkshopRhythm.evaluationTick(s.level().gameTime,player),TemperAndTang.config.strikeIntervalTicks.get(),s.level().gameTime)?:return
            if(plan()?.polishing==true && refiningAttempt==null)refiningAttempt=RefiningAttempt(plan()!!,stocks(s).map(ItemStack::copy))
            SmithingFeedback.strike(s,"grid_4",score)
            if(job.ready) {
                val gear=s.state().getItem(GEAR)
                val reforging=!gear.isEmpty
                val hammerCost=required(s)
                val resultTarget=target(s).copy()
                val result=if(reforging) {
                    val forged=Craftsmanship.forge(ThermalState.airCooled(gear,s.level()),job.accuracy,player,"reforge:"+BuiltInRegistries.ITEM.getKey(gear.item))
                    if(forged.isEmpty)forged else ThermalState.unquenched(forged,s.level())
                } else plan()?.output(stocks(s),s.level(),player,job.perfect,job.accuracy)?:ItemStack.EMPTY
                if(result.isEmpty || result.count !in 1..(if(plan()?.polishing==true)8 else 1))return
                refiningAttempt=null
                if(reforging) {
                    s.state().setItem(GEAR,ItemStack.EMPTY)
                    s.state().setItem(FLUX,s.state().getItem(FLUX).copy().also {it.shrink(1)})
                } else {
                    val remaining=plan()!!.remainders(stocks(s))
                    for(i in 0..8)s.state().setItem(grid(i),remaining.getOrNull(i)?.copy()?:ItemStack.EMPTY)
                }
                val worn=s.state().getItem(HAMMER).copy().also {it.damageValue+=hammerCost}
                s.state().setItem(HAMMER,if(worn.damageValue>=worn.maxDamage)ItemStack.EMPTY else worn)
                committedTarget=if(plan()?.polishing==true)result.copy()else resultTarget;committedCost=hammerCost
                s.state().setItem(WORKPIECE,result)
                player.server?.let {SmithingProgress.get(it).award(player.uuid,CraftRules.earnedXp(job.accuracy,reforging))}

            }
            updateOutputs(s)
        }
        override fun tick(s:StationContext) {
            if(s.isClientSide)return
            if(plans.isNotEmpty() && s.level().gameTime%20L==0L && plan()?.current(s.level())==false)refresh(s)
            if(refiningAttempt!=null && stocks(s).filter(WorkshopPlans::metal).any {!ThermalState.hot(it,s.level())})reset(s)
            for(slot in (0..8).map(::grid)+GEAR) {
                val stack=s.state().getItem(slot)
                if(ThermalState.data(stack).getLong("hot_until")>0 && !ThermalState.hot(stack,s.level())) {s.state().setItem(slot,ThermalState.airCooled(stack,s.level()));reset(s)}
            }
            if(status(s)!="work" && s.state().getItem(WORKPIECE).isEmpty && work!=null)reset(s)
            work?.tick(WorkshopRhythm.evaluationTick(s.level().gameTime,player(s)))
            updateOutputs(s)
        }
        override fun updateOutputs(s:StationContext) {
            val state=s.state();val actual=state.getItem(WORKPIECE);val target=target(s)
            state.setOutput("result",if(active())actual.copy() else ItemStack.EMPTY)
            val status=status(s)
            if(status=="work" && work==null && !s.isClientSide) {
                work=WorkshopRhythm.create(required(s),WorkshopRhythm.profile(target,stocks(s)),s.level().gameTime)
            }
            val job=work
            WorkshopRhythm.sync(state,job,WorkshopRhythm.profile(target,stocks(s)))
            state.setControl("required",required(s).toFloat())
            state.setControl("progress",job?.hits?.toFloat()?:0f);state.setControl("clean",job?.perfect?.toFloat()?:0f)
            state.setControl("accuracy",((job?.accuracy?:0.0)*100).toFloat());state.setControl("misses",job?.misses?.toFloat()?:0f)
            state.setControl("target",((job?.target?:s.level().gameTime)-s.level().gameTime).toFloat().coerceIn(0f,120f))
            state.setControl("window",job?.window?.toFloat()?:0f);state.setControl("phase",job?.phase(s.level().gameTime)?:0f)
            state.setControl("beat",if(job!=null && kotlin.math.abs(s.level().gameTime-job.target)<=job.perfectWindow)1f else 0f)
            state.setControl("last_score",job?.lastScore?.toFloat()?:0f)
            note(s,status,target,if(!state.getItem(GEAR).isEmpty || !actual.isEmpty)List(9){ItemStack.EMPTY}else plan()?.ghosts)
            val data=state.data().copy();val before=data.copy()
            data.putInt("category",category);data.putString("pattern_id",plan()?.id.toString());data.putBoolean("reforging",reforgeMode)
            val refining=plan()?.recipe as? PrecisionRefiningRecipe
            data.putBoolean("precision_refining",refining!=null)
            data.putInt("refining_accuracy",((refining?.minimumAccuracy?:0.0)*100).toInt())
            data.putBoolean("refining_failed",refining!=null && !actual.isEmpty && actual.item!=refining.getResultItem(s.level().registryAccess()).item)
            player(s)?.let {p->p.server?.let {server->val mastery=SmithingProgress.get(server);data.putInt("mastery",mastery.level(p.uuid));data.putLong("mastery_xp",mastery.xp(p.uuid))}}
            if(data!=before)state.setData(data)
        }
        override fun takeOutput(s:StationContext,player:Player,output:String):ItemStack {
            if(s.isClientSide || player.isSpectator || output!="result" || !active())return ItemStack.EMPTY
            val result=s.state().getItem(WORKPIECE).copy()
            if(result.isEmpty)return result
            s.state().setItem(WORKPIECE,ItemStack.EMPTY);reset(s);return result
        }
    }
    private class Trough : StationLogic {
        override fun onOpen(s:StationContext,player:Player)=WorkshopRecovery.opened(s,player)
        override fun onClose(s:StationContext,player:Player)=WorkshopRecovery.closed(s,player)
        private var start = -1L
        private var end = -1L
        private var input = ItemStack.EMPTY
        private var completed = false
        private fun reset(s: StationContext) { start=-1; end=-1; completed=false; s.state().setControl("progress",0f); s.state().setOutput("result",ItemStack.EMPTY) }
        override fun onSlotChanged(s: StationContext, slot: SlotRef) {
            if (slot == STOCK) { reset(s); s.state().setItem(STOCK,ThermalState.normalize(s.state().getItem(STOCK),s.level())) }
        }
        override fun onCustomAction(s: StationContext, player: Player, action: String, data: CompoundTag) { if (action == "quench") onButton(s,player,action) }
        override fun onButton(s: StationContext, player: Player, control: String) {
            if (s.isClientSide || !active() || player.isSpectator || control != "quench" || start>=0 || completed) return
            val stock = s.state().getItem(STOCK)
            if (!ThermalState.hot(stock,s.level()) || !s.state().getItem(WATER).`is`(Items.WATER_BUCKET)) { updateOutputs(s); return }
            if (ThermalState.quenched(stock,s.level()).isEmpty) { note(s,"invalid"); return }
            input=stock.copy(); start=s.level().gameTime; end=start+TemperAndTang.config.quenchingTicks.get()
            s.state().setItem(WATER,ItemStack(Items.BUCKET))
            s.effect(TemperAndTang.id("quench_started"),"quench",CompoundTag().also {it.putInt("ticks",TemperAndTang.config.quenchingTicks.get())})
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
