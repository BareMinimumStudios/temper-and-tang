package com.bareminimumstudios.temperandtang.content

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.MenuProvider
import net.minecraft.world.level.Level
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.AnvilBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

class FinishingAnvilBlock(properties: BlockBehaviour.Properties) : AnvilBlock(properties) {
    // This workstation is not a second route into vanilla repair/enchanting.
    override fun getMenuProvider(state: BlockState, level: Level, pos: BlockPos): MenuProvider? = null
    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape =
        if (state.getValue(FACING).axis == Direction.Axis.X) Z_SHAPE else X_SHAPE
    companion object {
        private val BASE = Shapes.or(Block.box(2.0, 0.0, 2.0, 14.0, 6.0, 14.0), Block.box(4.0, 6.0, 4.0, 12.0, 8.0, 12.0))
        private val X_SHAPE = Shapes.or(BASE, Block.box(6.0, 8.0, 5.0, 10.0, 12.0, 11.0), Block.box(0.0, 12.0, 4.0, 16.0, 16.0, 12.0))
        private val Z_SHAPE = Shapes.or(BASE, Block.box(5.0, 8.0, 6.0, 11.0, 12.0, 10.0), Block.box(4.0, 12.0, 0.0, 12.0, 16.0, 16.0))
    }
}
