package com.bareminimumstudios.temperandtang.mixin

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod
import com.llamalad7.mixinextras.injector.wrapoperation.Operation
import com.pockyl.diegesis.impl.client.focus.FocusController
import com.pockyl.diegesis.impl.client.focus.FocusScreen
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import org.spongepowered.asm.mixin.Mixin

/** Keep this mod's workshop instructions readable at small window sizes and large GUI scales. */
@Mixin(value=[FocusScreen::class], remap=false)
abstract class WorkshopCaptionMixin {
    @WrapMethod(method=["renderCaption"], remap=false)
    private fun temperCaption(graphics: GuiGraphics, original: Operation<Void>) {
        val station=FocusController.get().station()
        if(station==null || station.type().id().namespace!="temperandtang") {original.call(graphics);return}
        val text=station.caption()?:return
        val look=station.scene()?.definition()?.captions()?:return
        val mc=Minecraft.getInstance();val font=mc.font
        val alpha=(station.captionAlpha()*255).toInt().coerceIn(0,255)
        if(alpha<8)return
        val lines=font.split(text,(mc.window.guiScaledWidth-28).coerceAtLeast(80))
        var y=if(look.edge()==com.pockyl.diegesis.api.scene.CaptionOptions.Edge.TOP) 8 else mc.window.guiScaledHeight-8-lines.size*(font.lineHeight+2)
        for(line in lines) {
            val width=font.width(line);val x=(mc.window.guiScaledWidth-width)/2
            val background=(look.background() ushr 24)*alpha/255
            if(background>0)graphics.fill(x-5,y-2,x+width+5,y+font.lineHeight+2,(background shl 24) or (look.background() and 0xFFFFFF))
            graphics.drawString(font,line,x,y,(((look.color() ushr 24)*alpha/255) shl 24) or (look.color() and 0xFFFFFF),true)
            y+=font.lineHeight+2
        }
    }
}
