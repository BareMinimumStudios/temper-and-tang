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
        "forge"->listOf(doubleArrayOf(0.0,8.0,0.0,16.0,10.0,16.0),doubleArrayOf(1.0,10.0,3.0,15.0,12.0,15.0),doubleArrayOf(1.0,0.0,1.0,4.0,8.0,4.0),doubleArrayOf(12.0,0.0,1.0,15.0,8.0,4.0),doubleArrayOf(1.0,0.0,12.0,4.0,8.0,15.0),doubleArrayOf(12.0,0.0,12.0,15.0,8.0,15.0),doubleArrayOf(0.0,10.0,14.0,16.0,16.0,16.0))
        "finishing"->listOf(doubleArrayOf(2.0,0.0,2.0,14.0,6.0,14.0),doubleArrayOf(4.0,6.0,4.0,12.0,8.0,12.0),doubleArrayOf(6.0,8.0,5.0,10.0,12.0,11.0),doubleArrayOf(0.0,12.0,4.0,16.0,16.0,12.0),doubleArrayOf(0.0,6.0,0.0,16.0,8.0,4.0),doubleArrayOf(0.0,8.0,0.0,16.0,8.5,2.0))
        "trough"->listOf(doubleArrayOf(0.0,2.0,0.0,16.0,5.0,16.0),doubleArrayOf(0.0,5.0,0.0,2.0,14.0,16.0),doubleArrayOf(14.0,5.0,0.0,16.0,14.0,16.0),doubleArrayOf(2.0,5.0,0.0,14.0,14.0,2.0),doubleArrayOf(2.0,5.0,14.0,14.0,14.0,16.0),doubleArrayOf(2.0,0.0,2.0,5.0,2.0,14.0),doubleArrayOf(11.0,0.0,2.0,14.0,2.0,14.0))
        else->listOf(doubleArrayOf(0.0,0.0,0.0,16.0,4.0,16.0),doubleArrayOf(0.0,4.0,0.0,3.0,14.0,16.0),doubleArrayOf(13.0,4.0,0.0,16.0,14.0,16.0),doubleArrayOf(3.0,4.0,13.0,13.0,14.0,16.0),doubleArrayOf(3.0,4.0,3.0,13.0,6.0,13.0))
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
