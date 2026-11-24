package com.bareminimumstudios.temperandtang.station

import com.bareminimumstudios.temperandtang.TemperAndTang
import com.bareminimumstudios.temperandtang.content.TemperContent
import com.bareminimumstudios.temperandtang.smithing.*
import com.pockyl.diegesis.api.Diegesis
import com.pockyl.diegesis.api.state.*
import com.pockyl.diegesis.api.station.*
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

object FinishingStation {
    val WEAPON=SlotRef.of("weapon");val MATERIAL=SlotRef.of("material");val HAMMER=SlotRef.of("hammer");val FINISHED=SlotRef.of("finished")
    private val hammerTag=TagKey.create(Registries.ITEM,TemperAndTang.id("smithing_hammers"))
    fun isHammer(stack:ItemStack)=!stack.isEmpty && stack.isDamageableItem && stack.`is`(hammerTag)
    fun hammerReady(stack:ItemStack)=isHammer(stack) && stack.maxDamage-stack.damageValue>=TemperAndTang.config.requiredStrikes.get()
    fun register() {
        Diegesis.station(TemperAndTang.id("finishing_anvil")).block {TemperContent.block("finishing_anvil")}
            .schema(StationSchema.builder()
                .slots("weapon",1,SlotRules.DEFAULT.maxStack(1).accepts(WeaponFinishes::accepts))
                .slots("material",1,SlotRules.DEFAULT.accepts {WeaponFinishes.ingredient(it)!=null})
                .slots("hammer",1,SlotRules.DEFAULT.maxStack(1).accepts(::isHammer))
                .slots("finished",1,SlotRules.DEFAULT.maxStack(1).accepts {false})
                .control("strike",ControlSpec.button()).control("progress",ControlSpec.range(0f,40f,0f).locked())
                .control("target",ControlSpec.range(0f,120f,0f).locked())
                .control("period",ControlSpec.range(0f,120f,0f).locked())
                .control("perfect_window",ControlSpec.range(0f,8f,0f).locked())
                .control("phrase_size",ControlSpec.range(0f,3f,0f).locked())
                .control("phrase_step",ControlSpec.range(0f,3f,0f).locked())
                .control("next_gap",ControlSpec.range(0f,120f,0f).locked())
                .control("combo",ControlSpec.range(0f,40f,0f).locked()).control("phase",ControlSpec.range(0f,1f,0f).locked())
                .control("beat",ControlSpec.range(0f,1f,0f).locked()).control("accuracy",ControlSpec.range(0f,100f,0f).locked())
                .control("window",ControlSpec.range(0f,10f,0f).locked())
                .control("last_score",ControlSpec.range(0f,1f,0f).locked()).output("result").build())
            .exclusive().openWhen {_,_,_->TemperAndTang.config.enabled.get()}.logic {Logic()}.register()
    }
    private class Logic:StationLogic {
        private val timing=WorkshopRhythm.Measurement()
        override fun onOpen(s:StationContext,player:Player) {WorkshopRecovery.opened(s,player);timing.begin(s)}
        override fun onClose(s:StationContext,player:Player)=WorkshopRecovery.closed(s,player)
        private var work:HammerWork?=null
        private fun valid(s:StationContext):Boolean {
            val state=s.state();val finish=WeaponFinishes.ingredient(state.getItem(MATERIAL))?:return false
            return hammerReady(state.getItem(HAMMER)) && !WeaponFinishes.preview(state.getItem(WEAPON),finish).isEmpty
        }
        override fun onSlotChanged(s:StationContext,slot:SlotRef) {
            if(s.state().getItem(FINISHED).isEmpty) {work=null;s.state().setControl("progress",0f)}
        }
        override fun onCustomAction(s:StationContext,player:Player,action:String,data:CompoundTag) {if(action=="timing_ack")timing.acknowledge(s,player,data)else if(action=="strike")onButton(s,player,action)}
        override fun onButton(s:StationContext,player:Player,control:String) {
            if(s.isClientSide || control!="strike" || player.isSpectator || !player.inventoryMenu.carried.isEmpty || !TemperAndTang.config.enabled.get() || !s.state().getItem(FINISHED).isEmpty)return
            updateOutputs(s)
            if(!valid(s))return
            val job=work?:return
            if(job.ready) {commit(s,player);updateOutputs(s);return}
            val score=job.strike(WorkshopRhythm.evaluationTick(s.level().gameTime,player),TemperAndTang.config.strikeIntervalTicks.get(),s.level().gameTime)?:return
            SmithingFeedback.strike(s,"weapon",score)
            if(job.ready)commit(s,player)
            updateOutputs(s)
        }
        private fun commit(s:StationContext,player:Player):Boolean {
            val state=s.state();val job=work?:return false
            if(!state.getItem(FINISHED).isEmpty || !job.ready || !valid(s))return false
            val config=TemperAndTang.config
            if(!player.isCreative && player.experienceLevel<config.experienceLevels.get()) {
                player.displayClientMessage(Component.translatable("temperandtang.station.experience",config.experienceLevels.get()),true);return false
            }
            val finish=WeaponFinishes.ingredient(state.getItem(MATERIAL))?:return false
            val result=WeaponFinishes.preview(state.getItem(WEAPON),finish,job.accuracy)
            if(result.isEmpty)return false
            if(!player.isCreative)player.giveExperienceLevels(-config.experienceLevels.get())
            state.setItem(WEAPON,ItemStack.EMPTY);state.setItem(MATERIAL,state.getItem(MATERIAL).copy().also {it.shrink(1)})
            val hammer=state.getItem(HAMMER).copy().also {it.damageValue+=config.requiredStrikes.get()}
            state.setItem(HAMMER,if(hammer.damageValue>=hammer.maxDamage)ItemStack.EMPTY else hammer)
            state.setItem(FINISHED,result)

            return true
        }
        override fun tick(s:StationContext) {
            if(s.isClientSide)return
            if(!valid(s) && s.state().getItem(FINISHED).isEmpty)work=null
            work?.tick(WorkshopRhythm.evaluationTick(s.level().gameTime,s.users().firstOrNull()));updateOutputs(s)
        }
        override fun updateOutputs(s:StationContext) {
            val state=s.state();val actual=state.getItem(FINISHED)
            if(work==null && actual.isEmpty && TemperAndTang.config.enabled.get() && valid(s) && !s.isClientSide)
                work=WorkshopRhythm.create(TemperAndTang.config.requiredStrikes.get(),WorkshopRhythm.profile(state.getItem(WEAPON)),s.level().gameTime)
            val job=work
            WorkshopRhythm.sync(state,job,WorkshopRhythm.profile(state.getItem(WEAPON)))
            val finish=WeaponFinishes.ingredient(state.getItem(MATERIAL))
            state.setOutput("result",if(!actual.isEmpty)actual.copy() else if(job?.ready==true && finish!=null)WeaponFinishes.preview(state.getItem(WEAPON),finish,job.accuracy) else ItemStack.EMPTY)
            state.setControl("progress",job?.hits?.toFloat()?:0f);state.setControl("accuracy",((job?.accuracy?:0.0)*100).toFloat())
            state.setControl("window",job?.window?.toFloat()?:0f)
            state.setControl("target",((job?.target?:s.level().gameTime)-s.level().gameTime).toFloat().coerceIn(0f,120f))
            state.setControl("phase",job?.phase(s.level().gameTime)?:0f);state.setControl("last_score",job?.lastScore?.toFloat()?:0f)
            state.setControl("beat",if(job!=null && kotlin.math.abs(s.level().gameTime-job.target)<=job.perfectWindow)1f else 0f)
        }
        override fun takeOutput(s:StationContext,player:Player,output:String):ItemStack {
            if(s.isClientSide || output!="result" || player.isSpectator || !TemperAndTang.config.enabled.get())return ItemStack.EMPTY
            if(s.state().getItem(FINISHED).isEmpty)commit(s,player)
            val result=s.state().getItem(FINISHED).copy()
            if(!result.isEmpty) {s.state().setItem(FINISHED,ItemStack.EMPTY);work=null;s.state().setControl("progress",0f)}
            return result
        }
    }
}
