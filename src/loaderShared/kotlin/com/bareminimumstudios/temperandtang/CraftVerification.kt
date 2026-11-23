package com.bareminimumstudios.temperandtang

import com.bareminimumstudios.temperandtang.content.TemperContent
import com.bareminimumstudios.temperandtang.smithing.*
import com.bareminimumstudios.temperandtang.station.ThermalStations as T
import com.bareminimumstudios.temperandtang.station.FinishingStation as F
import com.mojang.authlib.GameProfile
import com.pockyl.diegesis.api.Diegesis
import com.pockyl.diegesis.api.state.*
import com.pockyl.diegesis.api.station.StationContext
import com.pockyl.diegesis.impl.action.ActionHandler
import com.pockyl.diegesis.impl.action.StationAction
import net.minecraft.commands.CommandSourceStack
import net.minecraft.core.*
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.*
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.ai.attributes.*
import net.minecraft.world.item.*
import net.minecraft.world.item.component.*
import net.minecraft.world.item.enchantment.Enchantments
import java.util.UUID

/** Explicit isolated diagnostics: no connected player's inventory, mastery, blocks or clock are changed. */
object CraftVerification {
    private class Fixture(val source:CommandSourceStack,name:String) {
        val type=Diegesis.stationType(TemperAndTang.id(name)).orElseThrow()
        val state=StationState(type.schema())
        val player=object:Player(source.level,BlockPos.ZERO,0f,GameProfile(UUID.randomUUID(),"SmithFixture")) {
            override fun isSpectator()=false
            override fun isCreative()=false
        }.also {it.experienceLevel=10}
        var sparks=0
        val context=object:StationContext {
            override fun type()=type
            override fun state()=state
            override fun level()=source.level
            override fun pos()=BlockPos.containing(source.position)
            override fun blockState()=TemperContent.block(name).defaultBlockState()
            override fun facing()=Direction.NORTH
            override fun users()=listOf(player)
            override fun effect(effect:ResourceLocation,node:String?,data:CompoundTag) {if(effect.toString()=="diegesis:spark")sparks++}
        }
        val logic=type.logic().create(context)
        init {logic.onOpen(context,player)}
        fun put(slot:SlotRef,stack:ItemStack) {state.setItem(slot,stack.copy());logic.onSlotChanged(context,slot);logic.updateOutputs(context)}
        fun press(key:String,custom:Boolean=true,data:CompoundTag=CompoundTag()) {ActionHandler.apply(context,logic,player,if(custom)StationAction.Custom(key,data)else StationAction.Press(key))}
        fun tick()=logic.tick(context)
        fun take()=logic.takeOutput(context,player,"result")
        fun close()=ActionHandler.returnItems(context,logic,player,true)
        fun count(item:Item)=player.inventory.items.filter {it.item==item}.sumOf {it.count}
        fun aim()=state.getControl("target")==0f
    }
    private data class Job(val source:CommandSourceStack,val fixtures:List<Fixture>,val step:()->Boolean)
    private val jobs=mutableMapOf<Pair<MinecraftServer,String>,Job>()
    private fun clean(job:Job) {job.fixtures.forEach {SmithingProgress.get(job.source.server).forgetFixture(it.player.uuid)}}
    fun tick(server:MinecraftServer) {
        for((key,job) in jobs.toMap()) {
            if(key.first!==server)continue
            try {job.fixtures.forEach {it.tick()};if(job.step()) {jobs.remove(key);clean(job);job.source.sendSuccess({Component.literal("Craftsmanship "+key.second+" scenarios passed.")},false)}}
            catch(failure:Exception) {jobs.remove(key);clean(job);job.source.sendFailure(Component.literal("Craftsmanship verification failed: "+failure.message));TemperAndTang.logger.error("TEMPER_CRAFT_"+key.second.uppercase()+"_FAILED",failure)}
        }
    }
    fun stop(server:MinecraftServer) {jobs.filterKeys {it.first===server}.values.forEach(::clean);jobs.keys.removeIf {it.first===server}}
    fun verify(source:CommandSourceStack) {
        val stacks=mutableListOf(ItemStack(Items.IRON_SWORD),ItemStack(Items.IRON_CHESTPLATE),ItemStack(Items.DIAMOND_HELMET))
        for(id in listOf("simplyswords:iron_longsword","magistuarmory:armet","magistuarmory:steel_bastardsword")) {
            val item=BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));if(item!=Items.AIR)stacks.add(ItemStack(item))
        }
        for(stack in stacks) {
            check(GearSupport.supported(stack)) {"Gear is not supported: "+BuiltInRegistries.ITEM.getKey(stack.item)}
            stack.damageValue=3;stack.set(DataComponents.CUSTOM_NAME,Component.literal("Craft preservation"))
            stack.enchant(source.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.UNBREAKING),2)
            stack.set(DataComponents.CUSTOM_DATA,CustomData.of(CompoundTag().also {it.putString("foreign_fixture","keep")}))
            for(grade in CraftGrade.entries) {
                val quality=Craftsmanship.apply(stack,CraftRoll(grade,1.0,1.0,0.5,10),"SmithFixture","fixture")
                check(!quality.isEmpty && Craftsmanship.grade(quality)==grade && quality.damageValue==3)
                check(quality.get(DataComponents.CUSTOM_NAME)==stack.get(DataComponents.CUSTOM_NAME) && quality.get(DataComponents.ENCHANTMENTS)==stack.get(DataComponents.ENCHANTMENTS))
                check(quality.get(DataComponents.CUSTOM_DATA)?.copyTag()?.getString("foreign_fixture")=="keep")
                val max=quality.maxDamage;val attrs=quality.get(DataComponents.ATTRIBUTE_MODIFIERS)
                repeat(10) {val rebuilt=GearStats.rebuild(quality);check(rebuilt.maxDamage==max && rebuilt.get(DataComponents.ATTRIBUTE_MODIFIERS)==attrs) {"Quality multiplied itself"}}
                val finish=WeaponFinishes.preview(quality,Finish.TEMPERED)
                check(!finish.isEmpty && Craftsmanship.grade(finish)==grade)
                check(finish.maxDamage==FinishRules.maximumDurability(max,FinishRules.effect(Finish.TEMPERED,TemperAndTang.config.balance())))
                val saved=ItemStack.parseOptional(source.registryAccess(),finish.save(source.registryAccess()) as CompoundTag)
                check(Craftsmanship.grade(saved)==grade && saved.maxDamage==finish.maxDamage && saved.damageValue==3)
                val rerolled=Craftsmanship.apply(finish,CraftRoll(CraftGrade.ROUGH,0.0,0.0,0.0,0),"SmithFixture","fixture")
                check(!rerolled.isEmpty && rerolled.maxDamage<=finish.maxDamage) {"Reforging stacked durability"}
            }
        }
        check(!GearSupport.supported(ItemStack(Items.WOODEN_SWORD)) && !GearSupport.supported(ItemStack(Items.LEATHER_CHESTPLATE)))
        val diamond=Craftsmanship.apply(ItemStack(Items.DIAMOND_SWORD),CraftRoll(CraftGrade.MASTERWORK,1.0,0.9,0.5,10),"SmithFixture","fixture")
        val finished=WeaponFinishes.preview(diamond,Finish.TEMPERED)
        val recipe=source.server.recipeManager.byKey(ResourceLocation.parse("minecraft:netherite_sword_smithing")).orElseThrow().value() as net.minecraft.world.item.crafting.SmithingTransformRecipe
        val upgrade=recipe.assemble(net.minecraft.world.item.crafting.SmithingRecipeInput(ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),finished,ItemStack(Items.NETHERITE_INGOT)),source.registryAccess())
        check(upgrade.item==Items.NETHERITE_SWORD && Craftsmanship.grade(upgrade)==CraftGrade.MASTERWORK)
        check(Craftsmanship.root(upgrade).getInt("origin_durability")==Items.NETHERITE_SWORD.defaultInstance.maxDamage) {"Upgrade retained the old material baseline"}
        check(upgrade.maxDamage>Items.NETHERITE_SWORD.defaultInstance.maxDamage) {"Upgrade lost craftsmanship"}
        val progress=SmithingProgress();val uuid=UUID.randomUUID();progress.award(uuid,30)
        val loaded=SmithingProgress.load(progress.save(CompoundTag(),source.registryAccess()),source.registryAccess())
        check(loaded.xp(uuid)==30L && loaded.level(uuid)==1)
        val movementIds=listOf(Items.IRON_HELMET,Items.IRON_CHESTPLATE,Items.IRON_LEGGINGS,Items.IRON_BOOTS).map {item->
            val light=WeaponFinishes.preview(ItemStack(item),Finish.LIGHTENED)
            light.get(DataComponents.ATTRIBUTE_MODIFIERS)!!.modifiers().single {it.attribute()==net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED}.modifier().id()
        }
        check(movementIds.toSet().size==4) {"Armor movement modifiers collide across equipment slots"}
        var patterns=0;var armor=0;var knights=0;var swords=0
        for(plan in WorkshopPlans.forming(source.level)) {
            val grid=plan.ghosts.map {if(WorkshopPlans.metal(it))ThermalState.heated(it,source.level)else it.copy()}
            check(plan.matches(grid,source.level) && plan.hot(grid,source.level)) {"Unreachable pattern: "+plan.id}
            val result=plan.assemble(grid,source.level)
            check(!result.isEmpty && GearSupport.supported(result) && Craftsmanship.canReforge(result)) {"Invalid patterned result: "+plan.id}
            patterns++;if(GearSupport.armor(result))armor++
            if(plan.id.namespace=="magistuarmory")knights++
            if(plan.id.namespace=="simplyswords")swords++
        }
        val routed=TemperAndTang.config.thermalEnabled.get() && TemperAndTang.config.recipeIntegration.get()
        if(routed)check(patterns>0 && armor>0)
        else {
            check(source.server.recipeManager.byKey(ResourceLocation.parse("minecraft:iron_sword")).orElseThrow().value().type==net.minecraft.world.item.crafting.RecipeType.CRAFTING)
            check(source.server.recipeManager.byKey(TemperAndTang.id("bronze_sword")).orElseThrow().value().type==net.minecraft.world.item.crafting.RecipeType.CRAFTING)
        }
        if(routed && TemperAndTang.modLoaded("magistuarmory") && TemperAndTang.config.epicKnights.get())check(knights>0) {"Epic Knights patterns are missing"}
        if(routed && TemperAndTang.modLoaded("simplyswords") && TemperAndTang.config.simplySwords.get())check(swords>0) {"Simply Swords patterns are missing"}
        TemperAndTang.logger.info("TEMPER_CRAFT_STATS_PASSED: {} gear types, six grades, stat/finish/persistence invariants; {} patterns ({} armor, {} Epic Knights, {} Simply Swords)",stacks.size,patterns,armor,knights,swords)
    }
    fun startFinishing(source:CommandSourceStack) {
        val key=source.server to "finishing";check(key !in jobs)
        val f=Fixture(source,"finishing_anvil");val p=Fixture(source,"finishing_anvil")
        val hammer=TemperContent.item("smithing_hammer");val material=TemperContent.item("tempering_compound")
        f.put(F.WEAPON,ItemStack(Items.IRON_CHESTPLATE));f.put(F.MATERIAL,ItemStack(material,2));f.put(F.HAMMER,ItemStack(hammer));f.player.experienceLevel=0
        var phase=0;val start=source.level.gameTime
        jobs[key]=Job(source,listOf(f,p)) {
            check(source.level.gameTime-start<1800) {"Finishing scenario timed out"}
            if(phase==0 && f.aim()) {
                val before=f.state.getControl("progress")
                f.press("strike");f.press("strike",false)
                check(f.state.getControl("progress")==before+1) {"Finishing spam bypass"}
                if(!f.state.getOutput("result").isEmpty) {
                    check(f.take().isEmpty && f.state.getItem(F.MATERIAL).count==2 && f.state.getItem(F.HAMMER).damageValue==0)
                    f.player.experienceLevel=TemperAndTang.config.experienceLevels.get()+3
                    val result=f.take();check(!result.isEmpty && f.player.experienceLevel==3 && f.take().isEmpty)
                    check(f.state.getItem(F.HAMMER).damageValue==TemperAndTang.config.requiredStrikes.get())
                    check(f.sparks==TemperAndTang.config.requiredStrikes.get())
                    p.put(F.WEAPON,result);p.put(F.MATERIAL,ItemStack(material,2));p.put(F.HAMMER,ItemStack(hammer));p.press("strike")
                    check(p.state.getControl("progress")==0f) {"Repeated finish accepted"}
                    p.put(F.WEAPON,ItemStack(Items.IRON_SWORD));phase=1
                }
            } else if(phase==1 && p.aim()) {
                p.press("strike");p.close()
                check(p.count(Items.IRON_SWORD)==1 && p.count(material)==2 && p.player.experienceLevel==10)
                check(p.player.inventory.items.first {it.item==hammer}.damageValue==0)
                phase=2;TemperAndTang.logger.info("TEMPER_CRAFT_FINISHING_PASSED: scored strikes, tiny sparks, XP recovery, tool/material costs, duplicate refusal and partial cancellation")
            }
            phase==2
        }
        source.sendSuccess({Component.literal("Running isolated scored finishing scenarios...")},false)
    }
    fun startThermal(source:CommandSourceStack) {
        val key=source.server to "thermal";check(key !in jobs)
        val fixtures=mutableListOf<Fixture>()
        fun make(name:String)=Fixture(source,name).also {fixtures.add(it)}
        val hearth=make("charcoal_hearth");val shaper=make("shaping_anvil");val trough=make("quenching_trough");val reforge=make("shaping_anvil");val blueprint=make("shaping_anvil")
        val level=source.level;val plans=WorkshopPlans.forming(level)
        val plan=plans.first {it.recipe.getResultItem(level.registryAccess()).item==TemperContent.item("bronze_sword")}
        val hammer=TemperContent.item("smithing_hammer");val bronze=TemperContent.item("bronze_ingot")
        hearth.put(T.STOCK,ItemStack(bronze,8));hearth.press("heat");check(hearth.take().isEmpty)
        hearth.put(T.FUEL,ItemStack(Items.CHARCOAL,4));hearth.press("heat");val left=hearth.state.getItem(T.FUEL).count
        repeat(5) {hearth.press("heat")};check(hearth.state.getItem(T.FUEL).count==left)
        var phase=0;var shaped=ItemStack.EMPTY;var grade:CraftGrade?=null;var accuracy=0
        val start=level.gameTime
        fun prepare(f:Fixture) {plan.ghosts.forEachIndexed {i,it->f.put(T.grid(i),if(WorkshopPlans.metal(it))ThermalState.heated(it,level)else it)};f.put(T.HAMMER,ItemStack(hammer))}
        jobs[key]=Job(source,fixtures) {
            check(level.gameTime-start<4000) {"Thermal scenario timed out phase $phase"}
            when(phase) {
                0->if(!hearth.state.getOutput("result").isEmpty) {
                    val hot=hearth.take();check(hot.count==8 && ThermalState.hot(hot,level) && hearth.take().isEmpty)
                    plan.ghosts.forEachIndexed {i,it->shaper.put(T.grid(i),it)};shaper.put(T.HAMMER,ItemStack(hammer));shaper.press("strike");check(shaper.state.getControl("progress")==0f)
                    prepare(shaper);phase=1
                }
                1->if(shaper.aim()) {shaper.press("strike");phase=11}
                11->if(shaper.state.getControl("misses")>=1f) {check(shaper.state.getControl("progress")==1f);phase=2}
                2->if(shaper.aim()) {
                    val before=shaper.state.getControl("progress");shaper.press("strike",false);shaper.press("strike")
                    check(shaper.state.getControl("progress")==before+1) {"Shaping spam bypass"}
                    if(!shaper.state.getOutput("result").isEmpty) {
                        val xp=SmithingProgress.get(source.server).xp(shaper.player.uuid)
                        shaped=shaper.take();check(ThermalState.blank(shaped) && shaper.take().isEmpty)
                        check(SmithingProgress.get(source.server).xp(shaper.player.uuid)==xp && xp>0)
                        val contents=ItemStack.parseOptional(level.registryAccess(),ThermalState.data(shaped).getCompound("weapon"))
                        grade=Craftsmanship.grade(contents);accuracy=Craftsmanship.data(contents).getInt("accuracy")
                        check(grade!=null && accuracy<1000 && grade!=CraftGrade.EXCEPTIONAL)
                        contents.damageValue=3;contents.set(DataComponents.CUSTOM_NAME,Component.literal("Craft persistence"))
                        contents.enchant(level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.UNBREAKING),2)
                        shaped=ThermalState.unquenched(contents,level)
                        trough.put(T.STOCK,ItemStack.parseOptional(level.registryAccess(),shaped.save(level.registryAccess()) as CompoundTag));trough.press("quench");check(trough.take().isEmpty)
                        trough.put(T.WATER,ItemStack(Items.WATER_BUCKET));trough.press("quench")
                        check(trough.state.getItem(T.WATER).`is`(Items.BUCKET));phase=3
                    }
                }
                3->if(!trough.state.getOutput("result").isEmpty) {
                    val result=trough.take();check(result.item==TemperContent.item("bronze_sword") && Craftsmanship.grade(result)==grade && result.damageValue==3 && result.isEnchanted && result.hoverName.string=="Craft persistence")
                    check(!ThermalState.pending(result) && trough.take().isEmpty && shaper.sparks==plan.required())
                    val armor=Craftsmanship.apply(ItemStack(Items.IRON_CHESTPLATE),CraftRoll(CraftGrade.FINE,0.8,0.5,0.5,1),"Original","fixture")
                    armor.set(DataComponents.CUSTOM_NAME,Component.literal("Reforged armor"))
                    reforge.put(T.GEAR,ThermalState.heated(armor,level));reforge.put(T.HAMMER,ItemStack(hammer));reforge.press("strike");check(reforge.state.getControl("progress")==0f)
                    reforge.put(T.FLUX,ItemStack(TemperContent.item("smithing_flux"),2));phase=4
                }
                4->if(reforge.aim()) {
                    reforge.press("strike")
                    if(!reforge.state.getOutput("result").isEmpty) {
                        val output=ThermalState.quenched(reforge.take(),level)
                        check(output.item==Items.IRON_CHESTPLATE && output.hoverName.string=="Reforged armor" && Craftsmanship.grade(output)!=null)
                        check(reforge.state.getItem(T.FLUX).count==1 && reforge.state.getItem(T.HAMMER).damageValue==TemperAndTang.config.shapingStrikes.get()+2)
                        check(SmithingProgress.get(source.server).xp(reforge.player.uuid)==5L)
                        val specialized=plans.firstOrNull {GearSupport.blueprint(it.recipe.getResultItem(level.registryAccess()))}?:plan
                        var tries=0
                        while(blueprint.state.data().getString("pattern_id")!=specialized.id.toString() && tries++<plans.size+1)blueprint.press("next")
                        check(blueprint.state.data().getString("pattern_id")==specialized.id.toString())
                        blueprint.put(T.BLUEPRINT,ItemStack(TemperContent.item("blank_blueprint")));blueprint.press("scribe")
                        if(TemperAndTang.config.blueprintMastery.get()>0)check(Blueprints.blank(blueprint.state.getItem(T.BLUEPRINT)))
                        SmithingProgress.get(source.server).award(blueprint.player.uuid,1000)
                        blueprint.press("scribe");check(Blueprints.pattern(blueprint.state.getItem(T.BLUEPRINT))==specialized.id.toString())
                        blueprint.close();check(blueprint.count(TemperContent.item("smithing_blueprint"))==1)
                        phase=5
                        TemperAndTang.logger.info("TEMPER_CRAFT_THERMAL_PASSED: batch heat/fuel, random targets, missed-beat penalties, anti-spam, committed grade, quench preservation, paid armor reforging, mastery and reusable blueprints")
                    }
                }
            }
            phase==5
        }
        source.sendSuccess({Component.literal("Running isolated craftsmanship thermal scenarios...")},false)
    }
}
