package com.bareminimumstudios.temperandtang.client

import com.bareminimumstudios.temperandtang.TemperAndTang
import com.bareminimumstudios.temperandtang.station.*
import com.bareminimumstudios.temperandtang.smithing.*
import com.pockyl.diegesis.api.client.StationView
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import java.util.WeakHashMap

/** Keep normal play to stage, next action and ingredients. Extra help is deliberately opt-in. */
object WorkshopGuidance {
    private val expanded=WeakHashMap<StationView,Boolean>()
    fun toggle(view:StationView) {expanded[view]=expanded[view]!=true}
    private fun key(name:String)=WorkshopText.style(Component.literal(name),WorkshopText.STAGE)
    private fun item(id:String)=ResourceLocation.tryParse(id)?.let {ItemStack(BuiltInRegistries.ITEM.get(it))}?:ItemStack.EMPTY
    private fun ingredients(stacks:List<ItemStack>):Component {
        val text=Component.empty()
        for((i,group) in stacks.filterNot {it.isEmpty}.groupBy {BuiltInRegistries.ITEM.getKey(it.item)}.values.withIndex()) {
            if(i>0)text.append(Component.literal(", "))
            text.append(Component.translatable("temperandtang.guide.quantity",WorkshopText.number(group.sumOf {it.count}),WorkshopText.style(group.first().hoverName,WorkshopText.TEXT)))
        }
        return text
    }
    fun caption(view:StationView,action:String,finishStatus:String?=null):Component {
        val state=view.state();val data=state.data()
        val status=finishStatus?:data.getString("status").ifEmpty {"stock"}
        val input=when(action) {
            "heat","quench"->state.getItem(ThermalStations.STOCK)
            "finish"->state.getItem(FinishingStation.WEAPON)
            else->item(data.getString("target"))
        }
        val stock=if(input.isEmpty)state.getOutput("result")else input
        val shown=if(ThermalState.blank(stock))WorkpieceAppearance.shown(stock)else stock
        val name=if(shown.isEmpty)Component.translatable("temperandtang.guide.choose_stock")else shown.hoverName
        val stage=when(action){"heat"->1;"strike"->2;"quench"->3;else->4}
        val help=expanded[view]==true
        val header=Component.translatable("temperandtang.short.header",WorkshopText.style(Component.translatable("temperandtang.guide."+action),WorkshopText.STAGE,true),WorkshopText.number(stage),WorkshopText.style(name,WorkshopText.TEXT,true),WorkshopText.style(Component.translatable(if(help)"temperandtang.short.hide" else "temperandtang.short.help"),WorkshopText.STAGE))
        val paid=action=="finish" && !state.getItem(FinishingStation.FINISHED).isEmpty
        val waiting=action=="finish" && !paid && !state.getOutput("result").isEmpty
        val required=if(action=="finish")TemperAndTang.config.requiredStrikes.get() else if(action=="strike")state.getControl("required").toInt() else 0
        val working=action=="strike" && status=="work" || action=="finish" && status=="instructions"
        val message=when {
            paid->Component.translatable("temperandtang.short.take")
            waiting->Component.translatable(if(status=="need_experience")"temperandtang.short.xp_missing" else "temperandtang.short.pay",WorkshopText.number(TemperAndTang.config.experienceLevels.get()))
            working->Component.translatable("temperandtang.short.strike",key("SPACE"),WorkshopText.style(Component.translatable("temperandtang.guide.green"),WorkshopText.BENEFIT),WorkshopText.number(state.getControl("progress").toInt()),WorkshopText.number(required),WorkshopText.accuracy(state.getControl("accuracy").toInt()))
            action=="heat" && status=="heating" || action=="quench" && status=="quenching"->Component.translatable("temperandtang.short.progress",WorkshopText.number(state.getControl("progress").toInt()))
            status=="ready"->Component.translatable("temperandtang.short.ready_"+action)
            action=="quench" && status=="stock"->Component.translatable("temperandtang.short.workpiece")
            action=="finish"->Component.translatable("temperandtang.short.finish_"+status)
            status=="idle"->Component.translatable("temperandtang.short.start_"+action,key("SPACE"))
            status=="fuel"->Component.translatable("temperandtang.short.fuel_needed")
            status=="water"->Component.translatable("temperandtang.short.water_needed")
            else->Component.translatable("temperandtang.short."+status)
        }
        val color=when {paid || status=="ready"->WorkshopText.BENEFIT;status in listOf("repair","reload","disabled","invalid_weapon")->WorkshopText.COST;else->WorkshopText.TEXT}
        val text=Component.empty().append(WorkshopText.style(header,WorkshopText.MUTED)).append(Component.literal("\n")).append(WorkshopText.style(message,color))
        val busy=working || status in listOf("heating","quenching","ready") || paid || waiting
        if(working && action=="finish")text.append(Component.literal("\n")).append(WorkshopText.style(Component.translatable("temperandtang.short.cost",WorkshopText.number(TemperAndTang.config.experienceLevels.get())),WorkshopText.MUTED))
        if(!busy) {
            val needs:Component=when(action) {
                "strike"->if(data.getBoolean("reforging"))Component.translatable("temperandtang.short.reforge_needs")else ingredients((0..8).map {item(data.getString("ghost_$it"))})
                "heat"->if(stock.isEmpty)Component.translatable("temperandtang.short.fuel_types")else Component.translatable("temperandtang.short.fuel",WorkshopText.number(state.getControl("fuel_cost").toInt()))
                "quench"->Component.translatable("temperandtang.short.water")
                else->Component.translatable("temperandtang.short.finish_cost",WorkshopText.number(TemperAndTang.config.experienceLevels.get()),state.getItem(FinishingStation.MATERIAL).takeUnless {it.isEmpty}?.hoverName?:Component.translatable("temperandtang.short.treatment"))
            }
            text.append(Component.literal("\n")).append(WorkshopText.style(Component.translatable("temperandtang.short.needs",needs),WorkshopText.MUTED))
        }
        if(help) {
            text.append(Component.literal("\n"))
            val extra=if(working)Component.translatable("temperandtang.short.rhythm_help")else when(action) {
                "strike"->Component.translatable("temperandtang.guide.pattern_keys",key("← / →"),key("TAB"),key("B"),key("R"))
                "heat"->Component.translatable("temperandtang.short.heat_help")
                "quench"->Component.translatable("temperandtang.short.quench_help")
                else->Component.translatable("temperandtang.short.finish_help")
            }
            text.append(WorkshopText.style(extra,WorkshopText.MUTED))
            if(action=="strike" && !stock.isEmpty)text.append(Component.literal("\n")).append(WorkshopDescriptions.material(stock,true,(0..8).map {i->state.getItem(ThermalStations.grid(i)).takeUnless {it.isEmpty}?:item(data.getString("ghost_$i"))}))
            if(action=="finish" && !stock.isEmpty)WeaponFinishes.ingredient(state.getItem(FinishingStation.MATERIAL))?.let {finish->text.append(Component.literal("\n")).append(WorkshopDescriptions.finish(stock,finish))}
        }
        return text
    }
}
