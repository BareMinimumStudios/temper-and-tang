package com.bareminimumstudios.temperandtang.client

import com.bareminimumstudios.temperandtang.TemperAndTang
import com.bareminimumstudios.temperandtang.station.*
import com.bareminimumstudios.temperandtang.smithing.*
import com.pockyl.diegesis.api.client.StationView
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack

/** Captions describe the same slots and requirements used by the server, including pack-defined patterns. */
object WorkshopGuidance {
    private fun item(id:String):ItemStack=ResourceLocation.tryParse(id)?.let {ItemStack(BuiltInRegistries.ITEM.get(it))}?:ItemStack.EMPTY
    private fun list(stacks:List<ItemStack>):MutableComponent {
        val text=Component.empty()
        val groups=stacks.filterNot {it.isEmpty}.groupBy {BuiltInRegistries.ITEM.getKey(it.item)}
        for((index,group) in groups.values.withIndex()) {
            if(index>0)text.append(Component.literal(", "))
            text.append(Component.translatable("temperandtang.guide.quantity",group.sumOf {it.count},group.first().hoverName))
        }
        return text
    }
    fun decorate(view:StationView,action:String,instruction:Component):Component {
        val state=view.state();val status=state.data().getString("status")
        val stage=when(action){"heat"->1;"strike"->2;"quench"->3;else->4}
        val stock=when(action){"heat","quench"->state.getItem(ThermalStations.STOCK);"finish"->state.getItem(FinishingStation.WEAPON);else->item(state.data().getString("target"))}
        val name=if(stock.isEmpty)Component.translatable("temperandtang.guide.choose_stock") else stock.hoverName
        val header=Component.translatable("temperandtang.guide.header",Component.translatable("temperandtang.guide."+action),stage,name)
        val requirements:Component=when(action) {
            "heat"->{
                val ticks=WorkshopPlans.heating(stock,view.level())?.let {ThermalRules.heatingTicks(it.perItem,stock.count)}?:0
                Component.translatable("temperandtang.guide.heat_needs",state.getControl("fuel_cost").toInt(),(ticks+19)/20)
            }
            "quench"->Component.translatable("temperandtang.guide.quench_needs",(TemperAndTang.config.quenchingTicks.get()+19)/20)
            "finish"->{
                val material=state.getItem(FinishingStation.MATERIAL)
                Component.translatable("temperandtang.guide.finish_needs",if(material.isEmpty)Component.translatable("temperandtang.guide.finishing_material") else material.hoverName,TemperAndTang.config.requiredStrikes.get(),TemperAndTang.config.experienceLevels.get())
            }
            else->{
                if(state.data().getBoolean("reforging"))Component.translatable("temperandtang.guide.reforge_needs",state.getControl("required").toInt())
                else {
                    val ghosts=(0..8).map {item(state.data().getString("ghost_$it"))}
                    Component.translatable("temperandtang.guide.shape_needs",list(ghosts),state.getControl("required").toInt())
                }
            }
        }
        val result=header.append(Component.literal("\n")).append(instruction)
        if(status !in listOf("work","heating","quenching","ready") || action=="finish")result.append(Component.literal("\n")).append(requirements)
        if(action=="strike" && status in listOf("pattern","hot","hammer","blueprint"))result.append(Component.literal("\n")).append(Component.translatable("temperandtang.guide.substitutes"))
        if(status=="work" || action=="finish")result.append(Component.literal("\n")).append(Component.translatable("temperandtang.guide.timing"))
        return result
    }
}
