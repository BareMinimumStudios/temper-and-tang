package com.bareminimumstudios.temperandtang.mixin

import com.bareminimumstudios.temperandtang.client.PatternPicker
import com.pockyl.diegesis.impl.client.focus.FocusScreen
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

/** Diegesis overlays block station keyboard callbacks. Capture input only for our own picker. */
@Mixin(value=[FocusScreen::class],remap=false)
abstract class PatternInputMixin {
    @Inject(method=["mouseReleased(DDI)Z","method_25406(DDI)Z"],at=[At("HEAD")],cancellable=true,remap=false)
    private fun temperPatternRelease(x:Double,y:Double,button:Int,cir:CallbackInfoReturnable<Boolean>) {
        if(PatternPicker.consumeRelease(button))cir.returnValue=true
    }

    // FocusScreen overrides Minecraft input methods: Mojmap on NeoForge, intermediary on Fabric.
    @Inject(method=["keyPressed(III)Z","method_25404(III)Z"],at=[At("HEAD")],cancellable=true,remap=false)
    private fun temperPatternKey(key:Int,scan:Int,modifiers:Int,cir:CallbackInfoReturnable<Boolean>) {
        PatternPicker.active?.let {if(it.key(key))cir.returnValue=true}
    }
    @Inject(method=["charTyped(CI)Z","method_25400(CI)Z"],at=[At("HEAD")],cancellable=true,remap=false)
    private fun temperPatternChar(character:Char,modifiers:Int,cir:CallbackInfoReturnable<Boolean>) {
        PatternPicker.active?.let {if(it.type(character))cir.returnValue=true}
    }
}
