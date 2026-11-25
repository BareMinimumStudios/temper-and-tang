package com.bareminimumstudios.temperandtang

import com.bareminimumstudios.temperandtang.content.*
import com.bareminimumstudios.temperandtang.smithing.*
import com.bareminimumstudios.temperandtang.station.ThermalStations as T
import com.pockyl.diegesis.api.Diegesis
import net.minecraft.commands.CommandSourceStack
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.world.item.*
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.InteractionHand
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.AABB
import net.minecraft.world.entity.item.ItemEntity

/** Explicit isolated workshop checks; run in a disposable verification world. */
object WorkshopVerification {
    private data class Case(val fixture:CraftVerification.Fixture,val expected:Item,val count:Int,val duration:Int,val fuel:ItemStack)
    private data class Job(val source:CommandSourceStack,val started:Long,val cases:List<Case>)
    private val jobs=mutableMapOf<MinecraftServer,Job>()
    private val forced=mutableMapOf<MinecraftServer,List<net.minecraft.world.level.ChunkPos>>()
    private fun release(server:MinecraftServer){forced.remove(server)?.forEach {server.overworld().setChunkForced(it.x,it.z,false)}}
    private val warming=mutableMapOf<MinecraftServer,Pair<CommandSourceStack,Long>>()
    fun start(source:CommandSourceStack) {
        check(source.server !in jobs && source.server !in warming)
        val origin=BlockPos.containing(source.position).offset(2048,0,2048).atY(280)
        val chunks=(0..2).flatMap {n->Direction.Plane.HORIZONTAL.map {f->net.minecraft.world.level.ChunkPos(origin.offset(n*12,0,f.ordinal*12))}}.distinct()
        forced[source.server]=chunks.filter {!source.level.forcedChunks.contains(it.toLong())}
        forced.getValue(source.server).forEach {source.level.setChunkForced(it.x,it.z,true)}
        warming[source.server]=source to (source.level.gameTime+40)
    }
    private fun begin(source:CommandSourceStack) {
        check(source.server !in jobs){"Workshop verification already running"}
        val cases=mutableListOf<Case>()
        try {
            try {geometry(source)}finally{release(source.server)}
            fun add(station:String,primary:ItemStack,additive:ItemStack,fuel:ItemStack,expected:Item,count:Int,duration:Int) {
                val f=CraftVerification.Fixture(source,station)
                cases+=Case(f,expected,count,duration,fuel.copy())
                check(f.type.schema().slotGroups().getValue("stock").rules().accepts().test(primary)){"Stock slot rejects valid $station input: $primary"}
                f.put(T.STOCK,primary);f.put(T.ADDITIVE,additive);f.put(T.FUEL,fuel);f.press("heat")
                check(f.state.data().getString("status")=="heating"){"$station rejected fuel/recipe: ${f.state.data()}"}
                val remaining=f.state.getItem(T.FUEL)
                val remainder=WorkshopFuel.remainder(fuel)
                check(if(remainder.isEmpty)remaining.count==fuel.count-WorkshopFuel.cost(fuel,duration) else ItemStack.isSameItemSameComponents(remaining,remainder)){"Fuel count/container lost"}
                f.press("heat");check(ItemStack.matches(remaining,f.state.getItem(T.FUEL))){"Repeated start spent fuel twice"}
            }
            val tin=TemperContent.item("tin_ingot");val bronze=TemperContent.item("bronze_ingot");val steel=TemperContent.item("steel_ingot")
            add("charcoal_hearth",ItemStack(Items.IRON_INGOT),ItemStack.EMPTY,ItemStack(Items.OAK_PLANKS,2),Items.IRON_INGOT,1,HearthProcesses.duration(checkNotNull(WorkshopPlans.heating(ItemStack(Items.IRON_INGOT),source.level)),1))
            for((raw,ingot) in listOf(Items.RAW_IRON to Items.IRON_INGOT,TemperContent.item("raw_tin") to tin,TemperContent.item("raw_silver") to TemperContent.item("silver_ingot"),TemperContent.item("silver_ore") to TemperContent.item("silver_ingot"))) {
                val input=ItemStack(raw)
                add("charcoal_hearth",input,ItemStack.EMPTY,ItemStack(Items.COAL),ingot,1,HearthProcesses.duration(checkNotNull(WorkshopPlans.heating(input,source.level)),1))
            }
            add("alloy_furnace",ItemStack(Items.COPPER_INGOT,3),ItemStack(tin),ItemStack(Items.OAK_PLANKS,2),bronze,4,200)
            add("alloy_furnace",ItemStack(Items.IRON_INGOT),ItemStack(Items.COAL),ItemStack(Items.STICK,4),steel,1,240)
            add("alloy_furnace",ItemStack(Items.IRON_INGOT),ItemStack(Items.CHARCOAL),ItemStack(Items.COAL_BLOCK),steel,1,240)
            add("nether_alloy_furnace",ItemStack(Items.IRON_INGOT),ItemStack(Items.CHARCOAL),ItemStack(Items.LAVA_BUCKET),steel,1,240)
            add("nether_alloy_furnace",ItemStack(Items.COPPER_INGOT,3),ItemStack(tin),ItemStack(Items.BLAZE_ROD),bronze,4,200)
            add("nether_alloy_furnace",ItemStack(Items.NETHERITE_SCRAP,4),ItemStack(Items.GOLD_INGOT,4),ItemStack(Items.CHARCOAL),Items.NETHERITE_INGOT,1,1200)
            val reject=CraftVerification.Fixture(source,"alloy_furnace")
            try {reject.put(T.STOCK,ItemStack(Items.NETHERITE_SCRAP,4));reject.put(T.ADDITIVE,ItemStack(Items.GOLD_INGOT,4));reject.put(T.FUEL,ItemStack(Items.COAL));reject.press("heat");check(reject.state.getControl("progress")==0f && reject.state.getItem(T.FUEL).count==1 && reject.take().isEmpty)}finally{reject.close()}
            val cancel=CraftVerification.Fixture(source,"alloy_furnace")
            cancel.put(T.STOCK,ItemStack(Items.IRON_INGOT));cancel.put(T.ADDITIVE,ItemStack(Items.CHARCOAL));cancel.put(T.FUEL,ItemStack(Items.OAK_PLANKS));cancel.press("heat");cancel.close()
            check(cancel.count(Items.IRON_INGOT)==1 && cancel.count(Items.CHARCOAL)==1 && cancel.count(steel)==0){"Cancel created metal or lost inputs"}
            val hearth=CraftVerification.Fixture(source,"charcoal_hearth")
            try {check(!hearth.type.schema().slotGroups().getValue("additive").rules().accepts().test(ItemStack(tin)))}finally{hearth.close()}
            jobs[source.server]=Job(source,source.level.gameTime,cases)
            TemperAndTang.logger.info("WORKSHOP_VERIFY_STARTED: counted alloys, normal fuels, lava container, cancellation and occupied station geometry")
        }catch(e:Exception){cases.forEach {it.fixture.close()};TemperAndTang.logger.error("WORKSHOP_VERIFY_FAILED",e);source.sendFailure(Component.literal(e.message?:"Workshop verification failed"))}
    }
    fun tick(server:MinecraftServer) {
        warming[server]?.let {(source,ready)->if(source.level.gameTime>=ready){warming.remove(server);begin(source)}}
        val job=jobs[server]?:return
        try {
            job.cases.forEach {it.fixture.tick()}
            if(job.source.level.gameTime-job.started<1202)return
            for(case in job.cases){val f=case.fixture;val result=f.take();check(result.item==case.expected && result.count==case.count){"Unexpected alloy result: $result"};check(f.take().isEmpty){"Output could be taken twice"};check(f.state.getItem(T.STOCK).isEmpty && f.state.getItem(T.ADDITIVE).isEmpty){"Alloy inputs not consumed exactly"}}
            jobs.remove(server);job.cases.forEach {it.fixture.close()}
            TemperAndTang.logger.info("WORKSHOP_VERIFY_PASSED: eleven timed batches including raw iron/tin/silver and silver ore refining; both carbon types; wood/stick/block/blaze/lava fuels; bucket returned; direct netherite; no duplicate output; cancellation; four-facing multiblocks")
            job.source.sendSuccess({Component.literal("Workshop verification passed.")},false)
        }catch(e:Exception){jobs.remove(server);job.cases.forEach {it.fixture.close()};TemperAndTang.logger.error("WORKSHOP_VERIFY_FAILED",e)}
    }
    fun stop(server:MinecraftServer){release(server);warming.remove(server);jobs.remove(server)?.cases?.forEach {it.fixture.close()}}
    private fun geometry(source:CommandSourceStack){
        val level=source.level;val origin=BlockPos.containing(source.position).offset(2048,0,2048).atY(280)
        for((number,name)in listOf("charcoal_hearth","alloy_furnace","nether_alloy_furnace").withIndex())for(facing in Direction.Plane.HORIZONTAL){
            val block=TemperContent.block(name) as WorkshopBlock;val state=block.defaultBlockState().setValue(WorkshopBlock.FACING,facing);val root=origin.offset(number*12,0,facing.ordinal*12)
            val occupied=block.occupied(root,state);check(occupied.all {level.getBlockState(it).isAir}){"Diagnostic region occupied"}
            try {
                level.getEntitiesOfClass(ItemEntity::class.java,AABB(root).inflate(5.0)).forEach {it.discard()}
                val context = object : BlockPlaceContext(level, null, InteractionHand.MAIN_HAND, ItemStack(block),
                    BlockHitResult(Vec3.atCenterOf(root), Direction.UP, root, false)) {
                    override fun getHorizontalDirection() = facing.opposite
                }
                val blocked = occupied.last()
                level.setBlock(blocked, Blocks.STONE.defaultBlockState(), 3)
                check(block.getStateForPlacement(context) == null) { "Placement accepted a blocked station footprint" }
                level.removeBlock(blocked, false)
                check(block.getStateForPlacement(context) == state) { "Clear station footprint refused placement" }
                level.setBlock(root,state,3)
                level.setBlock(blocked,Blocks.STONE.defaultBlockState(),3)
                check(!WorkshopBlock.ensureSpace(level,root,state) && level.getBlockState(blocked).`is`(Blocks.STONE)){"Legacy assembly overwrote a neighbor"}
                level.removeBlock(blocked,false)
                check(WorkshopBlock.ensureSpace(level,root,state)){"Legacy root could not reserve clear space"}
                block.setPlacedBy(level,root,state,null,ItemStack(block))
                check(occupied.size==block.width*block.width*block.height)
                for(pos in occupied){val part=level.getBlockState(pos);check(part.`is`(block) && block.root(pos,part)==root);val type=Diegesis.stationFor(part).orElseThrow();check(type.anchor().resolve(level,pos,part)==root);val shape=part.getShape(level,pos);if(!shape.isEmpty){val b=shape.bounds();check(b.minX>=0 && b.minY>=0 && b.minZ>=0 && b.maxX<=1 && b.maxY<=1 && b.maxZ<=1)}}
                val region=AABB(root).inflate(5.0);check(level.getEntitiesOfClass(ItemEntity::class.java,region).isEmpty())
                val loot=net.minecraft.world.level.block.Block.getDrops(state,level,root,null)
                check(loot.sumOf {if(it.item==Item.byBlock(block))it.count else 0}==1){"Root loot invalid for $name: $loot table=${block.lootTable}"}
                level.destroyBlock(occupied.last(),true)
                check(occupied.all {level.getBlockState(it).isAir}){"Orphaned station part"}
                val drops=level.getEntitiesOfClass(ItemEntity::class.java,region);check(drops.sumOf {if(it.item.item==Item.byBlock(block))it.item.count else 0}==1){"Station did not drop exactly once: $name $facing drops=$drops tileDrops=${level.gameRules.getBoolean(net.minecraft.world.level.GameRules.RULE_DOBLOCKDROPS)}"};drops.forEach {it.discard()}
            }finally{occupied.forEach {if(level.getBlockState(it).`is`(block))level.removeBlock(it,false)}}
        }
    }
}
