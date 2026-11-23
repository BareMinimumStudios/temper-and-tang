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
    private val parts=when(kind) {
        "forge"->listOf(doubleArrayOf(0.0,7.9,0.0,16.0,9.1,16.0),doubleArrayOf(2.8,9.1,5.5,15.0,13.0,13.5),doubleArrayOf(1.0,0.0,1.0,2.8,7.9,2.8),doubleArrayOf(13.2,0.0,1.0,15.0,7.9,2.8),doubleArrayOf(1.0,0.0,13.2,2.8,7.9,15.0),doubleArrayOf(13.2,0.0,13.2,15.0,7.9,15.0),doubleArrayOf(.8,9.1,14.5,15.2,15.6,15.5))
        "finishing"->listOf(doubleArrayOf(4.0,0.0,0.0,12.0,8.5,16.0),doubleArrayOf(2.0,0.0,1.0,14.0,8.5,15.0),doubleArrayOf(1.0,0.0,3.0,15.0,8.5,13.0),doubleArrayOf(2.8,8.5,5.5,15.0,14.0,13.5),doubleArrayOf(.5,7.1,0.0,15.5,8.15,3.5))
        "trough"->listOf(doubleArrayOf(1.5,2.1,1.5,14.5,3.5,14.5),doubleArrayOf(.5,3.2,.5,2.2,12.0,15.5),doubleArrayOf(13.0,3.2,.5,15.5,12.0,15.5),doubleArrayOf(2.2,3.2,.5,13.0,12.0,2.2),doubleArrayOf(2.2,3.2,13.8,13.0,12.0,15.5),doubleArrayOf(3.0,0.0,1.0,5.0,2.1,15.0),doubleArrayOf(11.0,0.0,1.0,13.0,2.1,15.0))
        else->listOf(doubleArrayOf(.5,0.0,.5,15.5,2.0,15.5),doubleArrayOf(.5,2.0,1.0,2.5,11.3,15.5),doubleArrayOf(13.5,2.0,1.0,15.5,11.3,15.5),doubleArrayOf(2.5,2.0,13.5,13.5,12.8,15.5),doubleArrayOf(2.5,2.0,.5,13.5,3.5,2.5),doubleArrayOf(2.0,2.0,2.0,14.0,4.8,14.0))
    }
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
