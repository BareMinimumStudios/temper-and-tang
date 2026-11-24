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
            override fun effect(effect:ResourceLocation,node:String?,data:CompoundTag) {if(effect==TemperAndTang.id("hammer_strike"))sparks++}
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
            val item=BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));if(item!=Items.AIR && GearSupport.supported(ItemStack(item)))stacks.add(ItemStack(item))
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
        for(material in ForgeMaterial.entries)for(raw in listOf(ItemStack(Items.IRON_SWORD),ItemStack(Items.IRON_CHESTPLATE))) {
            raw.damageValue=7
            val stamped=CraftMaterials.stamp(raw,material)
            val made=Craftsmanship.apply(stamped,CraftRoll(CraftGrade.FINE,1.0,.6,.5,5),"MaterialFixture","fixture")
            check(!made.isEmpty && made.damageValue==7 && CraftMaterials.recorded(made)==material)
            val finished=WeaponFinishes.preview(made,Finish.TEMPERED)
            check(!finished.isEmpty && CraftMaterials.recorded(finished)==material && finished.damageValue==7)
            val saved=ItemStack.parseOptional(source.registryAccess(),finished.save(source.registryAccess()) as CompoundTag)
            check(saved.maxDamage==finished.maxDamage && CraftMaterials.recorded(saved)==material)
            repeat(10) {val rebuilt=GearStats.rebuild(saved);check(rebuilt.maxDamage==saved.maxDamage && rebuilt.get(DataComponents.ATTRIBUTE_MODIFIERS)==saved.get(DataComponents.ATTRIBUTE_MODIFIERS)){"Material bonuses stacked"}}
        }
        TemperAndTang.logger.info("TEMPER_MATERIALS_PASSED: material preservation, finish, serialization and non-stacking across weapons and armor")
        for(trait in ForgeTrait.entries)for(item in (stacks.map {it.item}+listOf(Items.IRON_PICKAXE,Items.IRON_HOE,Items.SHIELD)).distinct()) {
            val raw=ItemStack(item);raw.damageValue=5
            val stamped=CraftTraits.stamp(CraftMaterials.stamp(raw,ForgeMaterial.IRON),trait)
            val made=Craftsmanship.apply(stamped,CraftRoll(CraftGrade.FINE,1.0,.6,.5,5),"TraitFixture","fixture")
            check(!made.isEmpty && made.damageValue==5 && CraftTraits.recorded(made)==trait)
            val finished=WeaponFinishes.preview(made,Finish.TEMPERED)
            check(!finished.isEmpty && CraftTraits.recorded(finished)==trait && finished.damageValue==5)
            val saved=ItemStack.parseOptional(source.registryAccess(),finished.save(source.registryAccess()) as CompoundTag)
            check(CraftTraits.recorded(saved)==trait && saved.maxDamage==finished.maxDamage)
            repeat(10) {val rebuilt=GearStats.rebuild(saved);check(rebuilt.maxDamage==saved.maxDamage && rebuilt.get(DataComponents.ATTRIBUTE_MODIFIERS)==saved.get(DataComponents.ATTRIBUTE_MODIFIERS)){"Trait bonuses stacked"}}
        }
        val unsafe=ItemStack(Items.IRON_SWORD)
        val worst=(unsafe.maxDamage*(1+CraftGrade.ROUGH.durability*TemperAndTang.config.qualityPower.get()+ForgeTrait.HONED.durability*TemperAndTang.config.traitPower.get())).toInt().coerceAtLeast(1)
        if(worst<unsafe.maxDamage){unsafe.damageValue=worst;check(!Craftsmanship.canReforge(unsafe)){"Reforge accepted wear that cannot survive the worst possible committed roll"}}
        TemperAndTang.logger.info("TEMPER_TRAITS_PASSED: all traits, four gear families, finish and serialization preservation, ten rebuilds and worst-roll wear gating")
        check(!GearSupport.supported(ItemStack(Items.WOODEN_SWORD)) && !GearSupport.supported(ItemStack(Items.LEATHER_CHESTPLATE)))
        val diamond=Craftsmanship.apply(CraftTraits.stamp(CraftMaterials.stamp(ItemStack(Items.DIAMOND_SWORD),ForgeMaterial.DIAMOND),ForgeTrait.STURDY),CraftRoll(CraftGrade.MASTERWORK,1.0,0.9,0.5,10),"SmithFixture","fixture")
        val finished=WeaponFinishes.preview(diamond,Finish.TEMPERED)
        val recipe=source.server.recipeManager.byKey(ResourceLocation.parse("minecraft:netherite_sword_smithing")).orElseThrow().value() as net.minecraft.world.item.crafting.SmithingTransformRecipe
        val upgrade=recipe.assemble(net.minecraft.world.item.crafting.SmithingRecipeInput(ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),finished,ItemStack(Items.NETHERITE_INGOT)),source.registryAccess())
        check(upgrade.item==Items.NETHERITE_SWORD && Craftsmanship.grade(upgrade)==CraftGrade.MASTERWORK)
        check(CraftMaterials.recorded(upgrade)==ForgeMaterial.NETHERITE && CraftTraits.recorded(upgrade)==ForgeTrait.STURDY){"Upgrade lost or retained the wrong material/trait"}
        check(Craftsmanship.root(upgrade).getInt("origin_durability")==Items.NETHERITE_SWORD.defaultInstance.maxDamage) {"Upgrade retained the old material baseline"}
        check(upgrade.maxDamage>=Items.NETHERITE_SWORD.defaultInstance.maxDamage) {"Upgrade lost craftsmanship"}
        if(TemperAndTang.modLoaded("magistuarmory") && TemperAndTang.config.epicKnights.get()) {
            val helmet=ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("magistuarmory:armet")))
            if(GearSupport.supported(helmet)) {
                helmet.damageValue=3
                val made=Craftsmanship.apply(CraftTraits.stamp(CraftMaterials.stamp(helmet,ForgeMaterial.STEEL),ForgeTrait.REINFORCED),CraftRoll(CraftGrade.MASTERWORK,1.0,.9,.5,10),"SmithFixture","native-upgrade")
                val base=WeaponFinishes.preview(made,Finish.TEMPERED)
                val holder=source.server.recipeManager.byKey(ResourceLocation.parse("magistuarmory:ceremonialarmet")).orElseThrow()
                val native=holder.value() as net.minecraft.world.item.crafting.SmithingTransformRecipe
                val input=net.minecraft.world.item.crafting.SmithingRecipeInput(ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("magistuarmory:gilding_template"))),base,ItemStack(Items.GOLD_INGOT))
                check(native.matches(input,source.level))
                val gilded=native.assemble(input,source.registryAccess())
                check(Craftsmanship.root(gilded).getInt("origin_durability")==gilded.item.defaultInstance.maxDamage){"Native gilding used the old armor baseline"}
                check(CraftMaterials.recorded(gilded)==ForgeMaterial.STEEL && CraftTraits.recorded(gilded)==ForgeTrait.REINFORCED && Craftsmanship.grade(gilded)==CraftGrade.MASTERWORK && gilded.damageValue==3){"Native gilding lost its recorded steel or craftsmanship"}
                check(Craftsmanship.root(gilded).getString("finish")==Finish.TEMPERED.key)
                check(CraftMaterials.choose(gilded,emptyList())==ForgeMaterial.STEEL){"Refining an unnamed material lost its provenance"}
                val plate=BuiltInRegistries.ITEM.get(ResourceLocation.parse("magistuarmory:small_steel_plate"))
                if(plate!=Items.AIR)check(CraftMaterials.identify(ItemStack(plate))==ForgeMaterial.STEEL){"Small steel plates have no shared material identity"}
                TemperAndTang.logger.info("TEMPER_NATIVE_GILDING_PASSED: steel material, trait, finish, grade, wear and input provenance retained")
            }
        }
        val external=ItemStack(Items.IRON_SWORD).also {
            it.set(DataComponents.MAX_DAMAGE,300);it.set(DataComponents.BASE_COLOR,net.minecraft.world.item.DyeColor.RED)
            it.set(DataComponents.CUSTOM_NAME,Component.literal("Inherited equipment"));it.damageValue=3
            it.set(DataComponents.CUSTOM_DATA,CustomData.of(CompoundTag().also {data->data.putString("foreign_fixture","preserved");data.putString("authored_priority","old")}))
        }
        val old=Craftsmanship.apply(CraftMaterials.stamp(external,ForgeMaterial.IRON),CraftRoll(CraftGrade.MASTERWORK,1.0,.9,.5,10),"SmithFixture","transfer-fixture")
        val foreignId=ResourceLocation.parse("fixture:portable_damage")
        old.set(DataComponents.ATTRIBUTE_MODIFIERS,old.get(DataComponents.ATTRIBUTE_MODIFIERS)!!.withModifierAdded(Attributes.ATTACK_DAMAGE,AttributeModifier(foreignId,1.25,AttributeModifier.Operation.ADD_VALUE),net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND))
        val authored=ItemStack(TemperContent.item("steel_sword")).also {it.set(DataComponents.CUSTOM_DATA,CustomData.of(CompoundTag().also {tag->tag.putString("authored_priority","new")}))}
        val transferred=GearTransfer.inherit(authored,old)
        check(transferred.maxDamage==450 && transferred.damageValue==3){"Crafting upgrade baked in old quality or lost external durability"}
        check(transferred.get(DataComponents.BASE_COLOR)==net.minecraft.world.item.DyeColor.RED && transferred.hoverName.string=="Inherited equipment")
        check(transferred.get(DataComponents.CUSTOM_DATA)!!.copyTag().getString("authored_priority")=="new" && transferred.get(DataComponents.CUSTOM_DATA)!!.copyTag().getString("foreign_fixture")=="preserved")
        check(transferred.get(DataComponents.ATTRIBUTE_MODIFIERS)!!.modifiers().count {it.modifier().id()==foreignId}==1)
        check(transferred.get(DataComponents.ATTRIBUTE_MODIFIERS)!!.modifiers().none {it.modifier().id().namespace=="temperandtang" && it.modifier().id().path.startsWith("craft_")})
        check(Craftsmanship.grade(transferred)==null){"Crafting upgrade retained a free old quality roll"}
        TemperAndTang.logger.info("TEMPER_CRAFT_TRANSFER_PASSED: native stats, external durability, affixes, portable metadata and authored component priority")
        val progress=SmithingProgress();val uuid=UUID.randomUUID();progress.award(uuid,30)
        val loaded=SmithingProgress.load(progress.save(CompoundTag(),source.registryAccess()),source.registryAccess())
        check(loaded.xp(uuid)==30L && loaded.level(uuid)==1)
        val movementIds=listOf(Items.IRON_HELMET,Items.IRON_CHESTPLATE,Items.IRON_LEGGINGS,Items.IRON_BOOTS).map {item->
            val light=WeaponFinishes.preview(ItemStack(item),Finish.LIGHTENED)
            light.get(DataComponents.ATTRIBUTE_MODIFIERS)!!.modifiers().single {it.attribute()==net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED}.modifier().id()
        }
        check(movementIds.toSet().size==4) {"Armor movement modifiers collide across equipment slots"}
        check(GearSupport.supported(ItemStack(Items.IRON_HOE)) && GearSupport.supported(ItemStack(Items.GOLDEN_HOE))){"Metal tools bypass the workshop filter"}
        val catalogPlayer=Fixture(source,"shaping_anvil").player
        var patterns=0;var armor=0;var knights=0;var swords=0;var more=0
        for(plan in WorkshopPlans.forming(source.level).filterNot {it.polishing}) {
            val grid=plan.ghosts.map {if(WorkshopPlans.metal(it))ThermalState.heated(it,source.level)else it.copy()}
            check(plan.matches(grid,source.level) && plan.hot(grid,source.level)) {"Unreachable pattern: "+plan.id}
            val result=plan.assemble(grid,source.level)
            check(!result.isEmpty && GearSupport.supported(result) && Craftsmanship.canReforge(result)) {"Invalid patterned result: "+plan.id}
            val forged=plan.output(grid,source.level,catalogPlayer,plan.required(),.95)
            check(ThermalState.blank(forged)){"Could not forge pattern "+plan.id}
            val quenched=ThermalState.quenched(forged,source.level)
            check(quenched.item==result.item && Craftsmanship.grade(quenched)!=null && !ThermalState.pending(quenched)){"Quenching lost authored equipment: "+plan.id}
            val finished=WeaponFinishes.preview(quenched,Finish.TEMPERED)
            check(!finished.isEmpty && finished.item==result.item && CraftTraits.recorded(finished)==CraftTraits.recorded(quenched)){"Finishing lost equipment or trait: "+plan.id}
            val saved=ItemStack.parseOptional(source.registryAccess(),finished.save(source.registryAccess()) as CompoundTag)
            check(saved.item==result.item && saved.maxDamage==finished.maxDamage && CraftMaterials.recorded(saved)==CraftMaterials.recorded(finished)){"Catalog save failed: "+plan.id}
            patterns++;if(GearSupport.armor(result))armor++
            if(plan.id.namespace=="magistuarmory")knights++
            if(plan.id.namespace=="simplyswords")swords++
            if(plan.id.namespace=="simplymore")more++
        }
        SmithingProgress.get(source.server).forgetFixture(catalogPlayer.uuid)
        val routed=TemperAndTang.config.thermalEnabled.get() && TemperAndTang.config.recipeIntegration.get()
        if(routed)check(patterns>0 && armor>0)
        else {
            check(source.server.recipeManager.byKey(ResourceLocation.parse("minecraft:iron_sword")).orElseThrow().value().type==net.minecraft.world.item.crafting.RecipeType.CRAFTING)
            check(source.server.recipeManager.byKey(TemperAndTang.id("bronze_sword")).orElseThrow().value().type==net.minecraft.world.item.crafting.RecipeType.CRAFTING)
        }
        if(routed && TemperAndTang.modLoaded("magistuarmory") && TemperAndTang.config.epicKnights.get())check(knights>0) {"Epic Knights patterns are missing"}
        if(routed && TemperAndTang.modLoaded("simplyswords") && TemperAndTang.config.simplySwords.get())check(swords>0) {"Simply Swords patterns are missing"}
        if(routed && TemperAndTang.modLoaded("simplymore") && TemperAndTang.config.simplyMore.get())check(more>0){"Simply More patterns are missing"}
        TemperAndTang.logger.info("TEMPER_CRAFT_STATS_PASSED: {} gear types, six grades, stat/finish/persistence invariants; {} patterns ({} armor, {} Epic Knights, {} Simply Swords, {} Simply More)",stacks.size,patterns,armor,knights,swords,more)
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
                        check(grade!=null && accuracy<1000 && grade!=CraftGrade.MASTERWORK)
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
    fun startMetallurgy(source:CommandSourceStack) {
        val key=source.server to "metallurgy";check(key !in jobs)
        val level=source.level
        val fixtures=mutableListOf<Fixture>()
        fun make()=Fixture(source,"charcoal_hearth").also {fixtures.add(it)}
        val bronze=TemperContent.item("bronze_ingot");val tin=TemperContent.item("tin_ingot");val steel=TemperContent.item("steel_ingot")
        val bridgeId=ResourceLocation.fromNamespaceAndPath("magistuarmory","furnace/steel_ingot_blasting")
        val example=com.google.gson.JsonParser.parseString("""{"type":"minecraft:blasting","ingredient":{"item":"minecraft:iron_ingot"},"result":{"id":"magistuarmory:steel_ingot"},"cookingtime":40,"experience":0.5}""")
        val original=example.deepCopy()
        val applied=com.bareminimumstudios.temperandtang.compat.WorkshopRecipes.apply(mapOf(bridgeId to example))
        check(example==original){"Integration mutated authored source data"}
        if(TemperAndTang.config.recipeIntegration.get() && TemperAndTang.config.thermalEnabled.get() && TemperAndTang.config.epicKnights.get())check(applied[bridgeId]!!.asJsonObject.get("type").asString=="temperandtang:alloying")
        val alternative=example.deepCopy().asJsonObject.also {it.add("ingredient",com.google.gson.JsonParser.parseString("""[{"item":"minecraft:iron_ingot"},{"tag":"c:ingots/iron"}]"""))}
        check(com.bareminimumstudios.temperandtang.compat.WorkshopRecipes.apply(mapOf(bridgeId to alternative))[bridgeId]==alternative){"Alternative ingredient recipe was overwritten"}
        val custom=example.deepCopy().asJsonObject.also {it.addProperty("cookingtime",800)}
        check(com.bareminimumstudios.temperandtang.compat.WorkshopRecipes.apply(mapOf(bridgeId to custom))[bridgeId]==custom){"Custom steel recipe was overwritten"}
        check(bridgeId !in com.bareminimumstudios.temperandtang.compat.WorkshopRecipes.apply(emptyMap())){"Removed steel recipe was recreated"}
        var nativeSteelUses=0
        for(holder in level.recipeManager.recipes) {
            if(holder.id().namespace!="magistuarmory")continue
            val recipe=holder.value() as? net.minecraft.world.item.crafting.CraftingRecipe?:continue
            val base=if(recipe is ShapingRecipe)recipe.base else recipe
            val shaped=base as? net.minecraft.world.item.crafting.ShapedRecipe
            val width=shaped?.width?:3;val height=shaped?.height?:3
            var replaced=false
            val grid=recipe.ingredients.map {ingredient ->
                when {
                    ingredient.isEmpty->ItemStack.EMPTY
                    ingredient.test(ItemStack(steel))->{replaced=true;ItemStack(steel)}
                    ingredient.test(ItemStack(TemperContent.item("steel_nugget")))->{replaced=true;ItemStack(TemperContent.item("steel_nugget"))}
                    else->ingredient.items.first().copy()
                }
            }.toMutableList()
            if(!replaced)continue
            while(grid.size<width*height)grid.add(ItemStack.EMPTY)
            val input=net.minecraft.world.item.crafting.CraftingInput.of(width,height,grid)
            check(recipe.matches(input,level) && !recipe.assemble(input,source.registryAccess()).isEmpty){"Native steel recipe rejects common workshop metal: ${holder.id()}"}
            nativeSteelUses++
        }
        if(TemperAndTang.modLoaded("magistuarmory"))check(nativeSteelUses>0)
        TemperAndTang.logger.info("TEMPER_METALLURGY_COMPAT_PASSED: {} native recipes accept workshop steel/nuggets; removed/custom recipe identities respected",nativeSteelUses)
        val plans=level.recipeManager.getAllRecipesFor(AlloyRecipes.TYPE)
        check(plans.any {it.id()==TemperAndTang.id("bronze_alloy")} && plans.any {it.id()==TemperAndTang.id("steel_alloy")})
        for(holder in plans) {
            val r=holder.value();val ops=source.registryAccess().createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE)
            val json=AlloyRecipes.SERIALIZER.codec().codec().encodeStart(ops,r).getOrThrow()
            val restored=AlloyRecipes.SERIALIZER.codec().codec().parse(ops,json).getOrThrow()
            check(ItemStack.isSameItemSameComponents(r.result,restored.result) && r.primaryCount==restored.primaryCount)
            val buffer=net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),source.registryAccess())
            try {
                AlloyRecipes.SERIALIZER.streamCodec().encode(buffer,r)
                val decoded=AlloyRecipes.SERIALIZER.streamCodec().decode(buffer)
                check(decoded.additiveCount==r.additiveCount && decoded.cookingTime==r.cookingTime && ItemStack.isSameItemSameComponents(decoded.result,r.result))
            } finally {buffer.release()}
        }
        val pick=ItemStack(TemperContent.item("bronze_pickaxe"));val steelPick=ItemStack(TemperContent.item("steel_pickaxe"))
        check(pick.isCorrectToolForDrops(net.minecraft.world.level.block.Blocks.IRON_ORE.defaultBlockState()) && !pick.isCorrectToolForDrops(net.minecraft.world.level.block.Blocks.DIAMOND_ORE.defaultBlockState()))
        check(steelPick.isCorrectToolForDrops(net.minecraft.world.level.block.Blocks.DIAMOND_ORE.defaultBlockState()) && !steelPick.isCorrectToolForDrops(net.minecraft.world.level.block.Blocks.OBSIDIAN.defaultBlockState()))
        for(name in com.bareminimumstudios.temperandtang.content.WorkshopCatalog.items.keys) {
            val gear=ItemStack(TemperContent.item(name));check(GearSupport.supported(gear)) {"Unsupported catalog item $name"}
            val metal=name.substringBefore('_');check(gear.item.isValidRepairItem(gear,ItemStack(if(metal=="iron")Items.IRON_INGOT else TemperContent.item(metal+"_ingot")))) {"No common-tag repair for $name"}
        }
        check(HearthProcesses.alloy(ItemStack(Items.COPPER_INGOT,4),ItemStack(tin,2),level)==null)
        check(HearthProcesses.alloy(ItemStack(Items.COPPER_INGOT,9),ItemStack(tin,3),level)==null)
        check(HearthProcesses.alloy(ItemStack(Items.IRON_INGOT,2),ItemStack(Items.COAL),level)==null)
        check(HearthProcesses.refine(ItemStack(Items.RAW_IRON,9),level)==null)
        val outcomes=mutableListOf<Triple<Fixture,Item,Int>>()
        fun run(input:ItemStack,additive:ItemStack,result:Item,count:Int):Fixture {
            val f=make();f.put(T.STOCK,input);if(!additive.isEmpty)f.put(T.ADDITIVE,additive)
            f.put(T.FUEL,ItemStack(Items.CHARCOAL,8));val cost=f.state.getControl("fuel_cost").toInt()
            f.press("heat");repeat(5){f.press("heat")};check(f.state.getItem(T.FUEL).count==8-cost)
            outcomes.add(Triple(f,result,count));return f
        }
        val alloy=run(ItemStack(Items.COPPER_INGOT,6),ItemStack(tin,3),bronze,8)
        val steelBatch=run(ItemStack(Items.IRON_INGOT,8),ItemStack(Items.COAL,8),steel,8)
        check(steelBatch.state.getControl("fuel_cost")==2f)
        run(ItemStack(Items.RAW_IRON,2),ItemStack.EMPTY,Items.IRON_INGOT,2)
        run(ItemStack(Items.RAW_COPPER,2),ItemStack.EMPTY,Items.COPPER_INGOT,2)
        run(ItemStack(TemperContent.item("raw_tin"),2),ItemStack.EMPTY,tin,2)
        run(ItemStack(TemperContent.item("bronze_blend"),4),ItemStack.EMPTY,bronze,4)
        run(ThermalState.heated(ItemStack(Items.IRON_INGOT),level),ItemStack(Items.CHARCOAL),steel,1)
        val closed=make();closed.put(T.STOCK,ItemStack(Items.COPPER_INGOT,3));closed.put(T.ADDITIVE,ItemStack(tin));closed.put(T.FUEL,ItemStack(Items.COAL));closed.press("heat");closed.close()
        check(closed.count(Items.COPPER_INGOT)==3 && closed.count(tin)==1 && closed.count(bronze)==0 && closed.count(Items.COAL)==0)
        val changed=make();changed.put(T.STOCK,ItemStack(Items.COPPER_INGOT,3));changed.put(T.ADDITIVE,ItemStack(tin));changed.put(T.FUEL,ItemStack(Items.COAL));changed.press("heat");changed.put(T.ADDITIVE,ItemStack(Items.CHARCOAL))
        val missing=make();missing.put(T.STOCK,ItemStack(Items.COPPER_INGOT,3));missing.put(T.ADDITIVE,ItemStack(tin));missing.press("heat");check(missing.state.getControl("progress")==0f && missing.take().isEmpty)
        val collected=mutableSetOf<Fixture>();val start=level.gameTime
        jobs[key]=Job(source,fixtures) {
            check(level.gameTime-start<3000){"Metallurgy verification timed out"}
            for((f,item,count) in outcomes)if(f !in collected && !f.state.getOutput("result").isEmpty) {
                val hot=f.take();check(hot.item==item && hot.count==count && ThermalState.hot(hot,level));check(f.take().isEmpty)
                val saved=ItemStack.parseOptional(source.registryAccess(),hot.save(source.registryAccess()) as CompoundTag)
                check(ItemStack.isSameItemSameComponents(saved,hot) && saved.count==count)
                if(f===alloy)check(f.state.getItem(T.ADDITIVE).count==1 && f.state.getItem(T.ADDITIVE).item==tin)
                if(f===steelBatch)check(f.state.getItem(T.ADDITIVE).isEmpty && f.state.getItem(T.FUEL).count==6)
                collected.add(f)
            }
            check(changed.state.getItem(T.STOCK).item==Items.COPPER_INGOT && changed.state.getItem(T.STOCK).count==3 && changed.take().isEmpty)
            check(missing.state.getItem(T.STOCK).count==3 && missing.state.getItem(T.ADDITIVE).count==1 && missing.take().isEmpty)
            if(collected.size==outcomes.size) {
                TemperAndTang.logger.info("TEMPER_METALLURGY_PASSED: codecs, native refinement, conserved bronze/steel batches, leftovers, hot input, fuel, cancellation, duplicate refusal, mutation safety, repair tags and mining tiers")
                true
            } else false
        }
        source.sendSuccess({Component.literal("Running isolated refining and alloying scenarios...")},false)
    }

}
