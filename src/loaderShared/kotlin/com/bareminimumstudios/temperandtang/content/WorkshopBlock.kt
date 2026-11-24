package com.bareminimumstudios.temperandtang.content
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.state.*
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.phys.shapes.*

class WorkshopBlock(properties:BlockBehaviour.Properties,kind:String):Block(properties) {
    private val parts=WorkshopShapes.parts(kind)
    private val shapes=(0..3).map {turns->parts.map {box->
        var b=box.copyOf();repeat(turns) {b=doubleArrayOf(16-b[5],b[1],b[0],16-b[2],b[4],b[3])}
        Block.box(b[0],b[1],b[2],b[3],b[4],b[5])
    }.fold(Shapes.empty(),Shapes::or)}
    init {registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH))}
    override fun createBlockStateDefinition(builder:StateDefinition.Builder<Block,BlockState>) {builder.add(FACING)}
    override fun getStateForPlacement(context:BlockPlaceContext)=defaultBlockState().setValue(FACING,context.horizontalDirection.opposite)
    override fun rotate(state:BlockState,rotation:Rotation)=state.setValue(FACING,rotation.rotate(state.getValue(FACING)))
    override fun mirror(state:BlockState,mirror:Mirror)=state.rotate(mirror.getRotation(state.getValue(FACING)))
    override fun getShape(state:BlockState,level:BlockGetter,pos:BlockPos,context:CollisionContext)=shapes[when(state.getValue(FACING)) {Direction.EAST->1;Direction.SOUTH->2;Direction.WEST->3;else->0}]
    companion object {val FACING=BlockStateProperties.HORIZONTAL_FACING}
}
