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
        Diegesis.station(TemperAndTang.id("charcoal_hearth")).block { TemperContent.block("charcoal_hearth") }
            .schema(StationSchema.builder()
                .slots("stock",1,SlotRules.DEFAULT.maxStack(8).accepts(WorkshopPlans::heatable))
                .slots("fuel",1,SlotRules.DEFAULT.accepts { it.`is`(ItemTags.COALS) })
                .control("heat",ControlSpec.button()).control("progress",ControlSpec.range(0f,100f,0f).locked())
                .control("fuel_cost",ControlSpec.range(0f,64f,1f).locked()).output("result").build())
            .exclusive().openWhen { _,_,_ -> active() }.logic { Hearth() }.register()
        Diegesis.station(TemperAndTang.id("shaping_anvil")).block { TemperContent.block("shaping_anvil") }
            .blocks(net.minecraft.world.level.block.Blocks.ANVIL,net.minecraft.world.level.block.Blocks.CHIPPED_ANVIL,net.minecraft.world.level.block.Blocks.DAMAGED_ANVIL)
            .schema(StationSchema.builder()
                .slots("grid",9,SlotRules.DEFAULT.maxStack(1))
                .slots("hammer",1,SlotRules.DEFAULT.maxStack(1).accepts(FinishingStation::isHammer))
                .slots("workpiece",1,SlotRules.DEFAULT.maxStack(1).accepts { false })
                .slots("gear",1,SlotRules.DEFAULT.maxStack(1).accepts(GearSupport::supported))
                .slots("flux",1,SlotRules.DEFAULT.accepts {it.item==TemperContent.item("smithing_flux")})
                .slots("blueprint",1,SlotRules.DEFAULT.maxStack(1).accepts {Blueprints.blank(it) || Blueprints.filled(it)})
                .control("scribe",ControlSpec.button()).control("category",ControlSpec.button())
                .control("accuracy",ControlSpec.range(0f,100f,0f).locked()).control("misses",ControlSpec.range(0f,10000f,0f).locked())
                .control("target",ControlSpec.range(0f,60f,0f).locked()).control("window",ControlSpec.range(0f,10f,0f).locked())
                .control("phase",ControlSpec.range(0f,1f,0f).locked()).control("last_score",ControlSpec.range(0f,1f,0f).locked())
                .control("strike",ControlSpec.button()).control("next",ControlSpec.button()).control("previous",ControlSpec.button())
                .control("progress",ControlSpec.range(0f,40f,0f).locked()).control("required",ControlSpec.range(1f,40f,6f).locked())
                .control("clean",ControlSpec.range(0f,40f,0f).locked()).control("beat",ControlSpec.range(0f,1f,0f).locked())
                .output("result").build())
            .exclusive().openWhen { player,pos,state -> active() && (state.`is`(TemperContent.block("shaping_anvil")) || TemperAndTang.config.vanillaAnvilWorkshop.get() && player.isShiftKeyDown) }.logic { Shaper() }.register()
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
        private var plans=emptyList<WorkshopPlans.Forming>()
        private var index=0
        private var category=0
        private var work:HammerWork?=null
        private fun stocks(s:StationContext)=(0..8).map {s.state().getItem(grid(it))}
        private fun player(s:StationContext)=s.users().firstOrNull()
        private fun plan()=plans.getOrNull(index)
        private fun reset(s:StationContext) {
            work=null
            for(key in listOf("progress","clean","accuracy","misses","beat","target","phase","last_score"))s.state().setControl(key,0f)
        }
        private fun refresh(s:StationContext) {
            val previous=plan()?.id
            plans=WorkshopPlans.forming(s.level())
            index=plans.indexOfFirst {it.id==previous}.takeIf {it>=0}?:plans.indexOfFirst {it.id==TemperAndTang.id("bronze_sword")}.takeIf {it>=0}?:0
            reset(s)
        }
        private fun target(s:StationContext)=if(!s.state().getItem(GEAR).isEmpty)s.state().getItem(GEAR) else plan()?.recipe?.getResultItem(s.level().registryAccess())?:ItemStack.EMPTY
        private fun required(s:StationContext)=if(!s.state().getItem(GEAR).isEmpty) TemperAndTang.config.shapingStrikes.get()+if(GearSupport.armor(s.state().getItem(GEAR)))2 else 0 else plan()?.required()?:6
        private fun status(s:StationContext):String {
            if(!active())return "disabled"
            if(!s.state().getItem(WORKPIECE).isEmpty)return "ready"
            val gear=s.state().getItem(GEAR)
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
                if(work==null && !Craftsmanship.canReforge(selected.assemble(stocks(s),s.level())))return "repair"
            }
            val hammer=s.state().getItem(HAMMER)
            if(!FinishingStation.isHammer(hammer) || hammer.maxDamage-hammer.damageValue<required(s))return "hammer"
            return "work"
        }
        override fun onOpen(s:StationContext,player:Player) {refresh(s);updateOutputs(s)}
        override fun onSlotChanged(s:StationContext,slot:SlotRef) {
            if(slot==BLUEPRINT) {
                val match=plans.indexOfFirst {it.id.toString()==Blueprints.pattern(s.state().getItem(BLUEPRINT))}
                if(match>=0) {index=match;category=0}
            }
            if(s.state().getItem(WORKPIECE).isEmpty)reset(s)
        }
        override fun onCustomAction(s:StationContext,player:Player,action:String,data:CompoundTag) {if(action in setOf("strike","next","previous","scribe","category"))onButton(s,player,action)}
        override fun onButton(s:StationContext,player:Player,control:String) {
            if(s.isClientSide || !active() || player.isSpectator || !s.state().getItem(WORKPIECE).isEmpty || !player.inventoryMenu.carried.isEmpty)return
            if(plans.isEmpty())refresh(s)
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
            val score=job.strike(s.level().gameTime)?:return
            SmithingFeedback.strike(s,"strike",score)
            if(job.ready) {
                val gear=s.state().getItem(GEAR)
                val reforging=!gear.isEmpty
                val hammerCost=required(s)
                val result=if(reforging) {
                    val forged=Craftsmanship.forge(ThermalState.airCooled(gear,s.level()),job.accuracy,player,"reforge:"+BuiltInRegistries.ITEM.getKey(gear.item))
                    if(forged.isEmpty)forged else ThermalState.unquenched(forged,s.level())
                } else plan()?.output(stocks(s),s.level(),player,job.perfect,job.accuracy)?:ItemStack.EMPTY
                if(result.isEmpty || result.count!=1)return
                if(reforging) {
                    s.state().setItem(GEAR,ItemStack.EMPTY)
                    s.state().setItem(FLUX,s.state().getItem(FLUX).copy().also {it.shrink(1)})
                } else {
                    val remaining=plan()!!.remainders(stocks(s))
                    for(i in 0..8)s.state().setItem(grid(i),remaining.getOrNull(i)?.copy()?:ItemStack.EMPTY)
                }
                val worn=s.state().getItem(HAMMER).copy().also {it.damageValue+=hammerCost}
                s.state().setItem(HAMMER,if(worn.damageValue>=worn.maxDamage)ItemStack.EMPTY else worn)
                s.state().setItem(WORKPIECE,result)
                player.server?.let {SmithingProgress.get(it).award(player.uuid,CraftRules.earnedXp(job.accuracy,reforging))}
                sound(s,SoundEvents.ANVIL_USE,0.6f,1.1f)
            }
            updateOutputs(s)
        }
        override fun tick(s:StationContext) {
            if(s.isClientSide)return
            if(plans.isNotEmpty() && s.level().gameTime%20L==0L && plan()?.current(s.level())==false)refresh(s)
            for(slot in (0..8).map(::grid)+GEAR) {
                val stack=s.state().getItem(slot)
                if(ThermalState.data(stack).getLong("hot_until")>0 && !ThermalState.hot(stack,s.level())) {s.state().setItem(slot,ThermalState.airCooled(stack,s.level()));reset(s)}
            }
            if(status(s)!="work" && s.state().getItem(WORKPIECE).isEmpty && work!=null)reset(s)
            work?.tick(s.level().gameTime)
            updateOutputs(s)
        }
        override fun updateOutputs(s:StationContext) {
            val state=s.state();val actual=state.getItem(WORKPIECE);val target=target(s)
            state.setOutput("result",if(active())actual.copy() else ItemStack.EMPTY)
            val status=status(s)
            if(status=="work" && work==null && !s.isClientSide) {
                work=HammerWork(required(s),TemperAndTang.config.shapingBeatTicks.get(),TemperAndTang.config.shapingWindowTicks.get(),TemperAndTang.config.timingJitter.get()) {ForgeRandom.nextInt(it)}.also {it.arm(s.level().gameTime)}
            }
            val job=work
            state.setControl("required",required(s).toFloat())
            state.setControl("progress",job?.hits?.toFloat()?:0f);state.setControl("clean",job?.perfect?.toFloat()?:0f)
            state.setControl("accuracy",((job?.accuracy?:0.0)*100).toFloat());state.setControl("misses",job?.misses?.toFloat()?:0f)
            state.setControl("target",((job?.target?:s.level().gameTime)-s.level().gameTime).toFloat().coerceIn(0f,60f))
            state.setControl("window",job?.window?.toFloat()?:0f);state.setControl("phase",job?.phase(s.level().gameTime)?:0f)
            state.setControl("beat",if(job!=null && kotlin.math.abs(s.level().gameTime-job.target)<=job.window/2)1f else 0f)
            state.setControl("last_score",job?.lastScore?.toFloat()?:0f)
            note(s,status,target,plan()?.ghosts)
            val data=state.data().copy();val before=data.copy()
            data.putInt("category",category);data.putString("pattern_id",plan()?.id.toString());data.putBoolean("reforging",!state.getItem(GEAR).isEmpty)
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
