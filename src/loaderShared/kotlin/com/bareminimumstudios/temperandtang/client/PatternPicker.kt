package com.bareminimumstudios.temperandtang.client

import com.bareminimumstudios.temperandtang.smithing.WorkshopPlans
import com.pockyl.diegesis.api.client.ScreenOverlay
import com.pockyl.diegesis.api.client.StationView
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW
import java.util.Locale

/** A local recipe index. Only the server may select a current, idle forming recipe. */
class PatternPicker(private val view:StationView):ScreenOverlay {
    companion object {
        var active:PatternPicker?=null; private set
        private var pendingRelease=-1
        fun consumeRelease(button:Int):Boolean {
            val consume=active?.open==true || pendingRelease==button
            if(pendingRelease==button)pendingRelease=-1
            return consume
        }
    }
    init {active=this}
    private var openingCharacter=true
    private val plans=WorkshopPlans.forming(view.level())
    private val names=plans.associate {it.id to it.recipe.getResultItem(view.level().registryAccess()).hoverName.string.lowercase(Locale.ROOT)}
    private val nameWords=names.mapValues {it.value.split(Regex("\\s+")).toSet()}
    private val searchText=plans.associate {it.id to (names[it.id]+" "+it.id).lowercase(Locale.ROOT).replace('_',' ')}
    private var filtered=plans
    private var query=""
    private var offset=0
    private var left=0
    private var top=0
    private var width=260
    private var rows=8
    var open=true
        private set
    private fun matches()=filtered
    private fun filter() {
        val terms=query.lowercase(Locale.ROOT).trim().split(Regex("\\s+")).filter {it.isNotEmpty()}
        val words=terms.toSet()
        val phrase=terms.joinToString(" ")
        filtered=if(terms.isEmpty())plans else plans.filter {plan->terms.all {searchText[plan.id]?.contains(it)==true}}
            .sortedWith(compareBy<WorkshopPlans.Forming> {if(nameWords[it.id]==words)0 else 1}
                .thenBy {if(names[it.id]?.startsWith(phrase)==true)0 else 1}.thenBy {it.id.toString()})
        offset=0
    }
    fun close(){if(!open)return;open=false;if(active===this)active=null;view.showOverlay(null)}
    override fun onClosed(){close()}
    fun type(c:Char):Boolean {if(!open)return false;if(openingCharacter){openingCharacter=false;if(c=='f' || c=='F')return true};if(!c.isISOControl() && query.length<80){query+=c;filter()};return true}
    fun key(key:Int):Boolean {
        if(!open)return false
        when(key){GLFW.GLFW_KEY_ESCAPE->close();GLFW.GLFW_KEY_BACKSPACE->{query=query.dropLast(1);filter()};GLFW.GLFW_KEY_DOWN->scroll(1);GLFW.GLFW_KEY_UP->scroll(-1)}
        return true
    }
    private fun scroll(amount:Int){offset=(offset+amount).coerceIn(0,(matches().size-rows).coerceAtLeast(0))}
    override fun mouseScrolled(x:Double,y:Double,delta:Double){if(open)scroll(if(delta>0)-3 else 3)}
    override fun mouseClicked(x:Double,y:Double,button:Int):Boolean {
        if(!open)return false
        pendingRelease=button
        if(button!=0)return true
        if(x<left || x>=left+width || y<top || y>=top+52+rows*24){close();return true}
        if(y>=top+44) {
            val row=((y-top-44)/24).toInt()
            if(row in 0 until rows)matches().getOrNull(offset+row)?.let {plan->
                view.sendAction("select_pattern",CompoundTag().also {it.putString("pattern",plan.id.toString())});close()
            }
        }
        return true
    }
    override fun render(g:GuiGraphics,mouseX:Int,mouseY:Int,partial:Float) {
        if(!open)return
        val mc=Minecraft.getInstance();width=minOf(320,mc.window.guiScaledWidth-24);rows=((mc.window.guiScaledHeight-100)/24).coerceIn(1,10)
        left=(mc.window.guiScaledWidth-width)/2;top=(mc.window.guiScaledHeight-52-rows*24)/2
        g.fill(left-1,top-1,left+width+1,top+53+rows*24,0xFF9C855C.toInt())
        g.fill(left,top,left+width,top+52+rows*24,0xF01B1B1B.toInt())
        g.drawString(mc.font,Component.translatable("temperandtang.patterns.title"),left+8,top+7,0xEBCB8B)
        g.drawString(mc.font,Component.translatable("temperandtang.patterns.search",query+"_"),left+8,top+23,0xE0E0E0)
        val filtered=matches();offset=offset.coerceAtMost((filtered.size-rows).coerceAtLeast(0))
        for((row,plan) in filtered.drop(offset).take(rows).withIndex()) {
            val y=top+44+row*24
            if(mouseX in left until left+width && mouseY in y until y+24)g.fill(left+2,y,left+width-2,y+24,0xFF3A4035.toInt())
            val result=plan.recipe.getResultItem(view.level().registryAccess())
            g.renderItem(result,left+6,y+3)
            g.drawString(mc.font,mc.font.plainSubstrByWidth(result.hoverName.string,width-35),left+28,y+2,0xECE6D8)
            g.drawString(mc.font,mc.font.plainSubstrByWidth(plan.id.toString(),width-35),left+28,y+13,0x9DA990)
        }
        if(filtered.isEmpty())g.drawString(mc.font,Component.translatable("temperandtang.patterns.empty"),left+8,top+48,0xBBBBBB)
    }
}
