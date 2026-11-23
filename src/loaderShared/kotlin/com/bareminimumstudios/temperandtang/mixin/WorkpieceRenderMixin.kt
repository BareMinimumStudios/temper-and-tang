package com.bareminimumstudios.temperandtang.mixin

import com.bareminimumstudios.temperandtang.client.WorkpieceAppearance
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod
import com.llamalad7.mixinextras.injector.wrapoperation.Operation
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.ItemRenderer
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import org.spongepowered.asm.mixin.Mixin

/** Pending workpieces show the contained gear without changing server ownership or usability. */
@Mixin(ItemRenderer::class)
abstract class WorkpieceRenderMixin {
    @WrapMethod(method=["getModel"])
    private fun temperModel(stack: ItemStack, level: Level?, entity: LivingEntity?, seed: Int, original: Operation<BakedModel>): BakedModel =
        original.call(WorkpieceAppearance.shown(stack), level, entity, seed)

    @WrapMethod(method=["render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V"])
    private fun temperAppearance(stack: ItemStack, context: ItemDisplayContext, leftHand: Boolean, pose: PoseStack, buffers: MultiBufferSource, light: Int, overlay: Int, model: BakedModel, original: Operation<Void>) {
        original.call(WorkpieceAppearance.shown(stack), context, leftHand, pose, buffers, light, overlay, model)
    }
}
