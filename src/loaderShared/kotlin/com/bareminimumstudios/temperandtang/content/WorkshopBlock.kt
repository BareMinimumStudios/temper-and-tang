package com.bareminimumstudios.temperandtang.content

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.state.*
import net.minecraft.world.level.block.state.properties.*
import net.minecraft.world.phys.shapes.*

/** A single owned station spanning real occupied cells; every part anchors to the same session. */
class WorkshopBlock(properties:BlockBehaviour.Properties,kind:String):Block(properties.pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK)) {
    private val scale=when(kind){"hearth","alloy_forge","nether_alloy_forge"->2.0;else->1.0}
    val width=if(scale>1)2 else 1
    val height=if(kind == "alloy_forge" || kind == "nether_alloy_forge")2 else 1
    private val cells=buildList {for(y in 0 until height)for(z in 0 until width)for(x in 0 until width)add(BlockPos(x,y,z))}
    private val parts=WorkshopShapes.parts(kind).map { box -> box.map { it * scale }.toDoubleArray() }
    private fun turns(facing:Direction)=when(facing){Direction.EAST->1;Direction.SOUTH->2;Direction.WEST->3;else->0}
    private fun offset(index:Int,facing:Direction):BlockPos {var p=cells.getOrElse(index){BlockPos.ZERO};repeat(turns(facing)){p=BlockPos(-p.z,p.y,p.x)};return p}
    private val shapes=Direction.Plane.HORIZONTAL.associateWith {facing->cells.indices.map {index->
        val off=offset(index,facing)
        parts.map {box->var b=box.copyOf();repeat(turns(facing)){b=doubleArrayOf(16-b[5],b[1],b[0],16-b[2],b[4],b[3])};
            Shapes.joinUnoptimized(Block.box(b[0]-off.x*16,b[1]-off.y*16,b[2]-off.z*16,b[3]-off.x*16,b[4]-off.y*16,b[5]-off.z*16),Shapes.block(),net.minecraft.world.phys.shapes.BooleanOp.AND)
        }.fold(Shapes.empty(),Shapes::or)
    }}
    init {registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(PART,0))}
    override fun createBlockStateDefinition(builder:StateDefinition.Builder<Block,BlockState>){builder.add(FACING,PART)}
    fun root(pos:BlockPos,state:BlockState)=pos.subtract(offset(state.getValue(PART),state.getValue(FACING)))
    fun occupied(pos:BlockPos,state:BlockState)=cells.indices.map {pos.offset(offset(it,state.getValue(FACING)))}
    override fun getStateForPlacement(context:BlockPlaceContext):BlockState? {
        val state=defaultBlockState().setValue(FACING,context.horizontalDirection.opposite)
        val level = context.level
        for ((index, pos) in occupied(context.clickedPos, state).withIndex()) {
            if (!level.isInWorldBounds(pos) || !level.worldBorder.isWithinBounds(pos) ||
                !level.hasChunkAt(pos) || !level.getBlockState(pos).canBeReplaced(context)) return null
            if (index != 0 && !level.isUnobstructed(state.setValue(PART, index), pos, CollisionContext.empty())) return null
        }
        return state
    }
    override fun setPlacedBy(level:Level,pos:BlockPos,state:BlockState,placer:LivingEntity?,stack:ItemStack){
        super.setPlacedBy(level,pos,state,placer,stack)
        if(!level.isClientSide)for(i in 1 until cells.size)level.setBlock(pos.offset(offset(i,state.getValue(FACING))),state.setValue(PART,i),3)
    }
    override fun onRemove(state:BlockState,level:Level,pos:BlockPos,next:BlockState,moving:Boolean){
        if(!level.isClientSide && !next.`is`(this)){
            val root=root(pos,state)
            if(state.getValue(PART)==0){
                for(i in 1 until cells.size){val p=root.offset(offset(i,state.getValue(FACING)));val s=level.getBlockState(p)
                    if(s.`is`(this) && s.getValue(FACING)==state.getValue(FACING) && s.getValue(PART)==i)level.removeBlock(p,false)
                }
            }else{val main=level.getBlockState(root);if(main.`is`(this) && main.getValue(PART)==0 && main.getValue(FACING)==state.getValue(FACING))level.destroyBlock(root,true)}
        }
        super.onRemove(state,level,pos,next,moving)
    }
    override fun playerWillDestroy(level:Level,pos:BlockPos,state:BlockState,player:Player):BlockState {
        if(!level.isClientSide && player.isCreative && state.getValue(PART)!=0){val p=root(pos,state);val main=level.getBlockState(p);if(main.`is`(this) && main.getValue(PART)==0)level.destroyBlock(p,false)}
        return super.playerWillDestroy(level,pos,state,player)
    }
    override fun rotate(state:BlockState,rotation:Rotation)=state.setValue(FACING,rotation.rotate(state.getValue(FACING)))
    override fun mirror(state:BlockState,mirror:Mirror)=state.rotate(mirror.getRotation(state.getValue(FACING)))
    override fun getShape(state:BlockState,level:BlockGetter,pos:BlockPos,context:CollisionContext)=shapes.getValue(state.getValue(FACING)).getOrElse(state.getValue(PART)){Shapes.empty()}
    companion object {
        val FACING=BlockStateProperties.HORIZONTAL_FACING
        val PART=IntegerProperty.create("part",0,7)
        fun ensureSpace(level:Level,pos:BlockPos,state:BlockState,player:Player?=null):Boolean {
            val block=state.block as? WorkshopBlock?:return true
            val root=block.root(pos,state);val facing=state.getValue(FACING)
            val missing=block.cells.indices.drop(1).filter {i->val p=root.offset(block.offset(i,facing));val current=level.getBlockState(p);!(current.`is`(block) && current.getValue(PART)==i && current.getValue(FACING)==facing)}
            if(missing.any {i->val p=root.offset(block.offset(i,facing));!level.isInWorldBounds(p) || !level.getBlockState(p).canBeReplaced()}){
                if(!level.isClientSide)player?.displayClientMessage(net.minecraft.network.chat.Component.translatable("temperandtang.station.clear_space"),true)
                return false
            }
            if(!level.isClientSide)for(i in missing)level.setBlock(root.offset(block.offset(i,facing)),state.setValue(PART,i),3)
            return true
        }
        fun anchor(level:BlockGetter,pos:BlockPos,state:BlockState)= (state.block as? WorkshopBlock)?.root(pos,state)?:pos
        fun span(level:BlockGetter,pos:BlockPos):List<BlockPos>{val state=level.getBlockState(pos);return (state.block as? WorkshopBlock)?.occupied(pos,state)?.filter {it!=pos}?:emptyList()}
    }
}
