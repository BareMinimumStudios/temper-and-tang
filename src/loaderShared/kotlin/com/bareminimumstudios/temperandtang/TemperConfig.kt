package com.bareminimumstudios.temperandtang
import me.fzzyhmstrs.fzzy_config.annotations.RootConfig
import me.fzzyhmstrs.fzzy_config.annotations.Version
import me.fzzyhmstrs.fzzy_config.api.FileType
import me.fzzyhmstrs.fzzy_config.config.Config
import me.fzzyhmstrs.fzzy_config.config.ConfigGroup
import me.fzzyhmstrs.fzzy_config.util.Translatable
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedDouble
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt
import com.bareminimumstudios.temperandtang.smithing.FinishBalance

@RootConfig
@Version(2)
@Translatable.Name("Temper & Tang")
@Translatable.Desc("Medieval smithing and deliberate weapon finishes. Server settings apply to every player.")
class TemperConfig : Config(TemperAndTang.id("config")) {
    val station = ConfigGroup("station", false)
    var enabled = ValidatedBoolean(true)
    var experienceLevels = ValidatedInt(2, 30, 0)
    var requiredStrikes = ValidatedInt(6, 40, 1)
    var strikeIntervalTicks = ValidatedInt(8, 40, 1)
    @Translatable.Desc("Allow replacing a finish. Original durability is retained; finishes never stack.")
    @ConfigGroup.Pop var allowRefinishing = ValidatedBoolean(false)
    val integration = ConfigGroup("integration", true)
    var simplySwords = ValidatedBoolean(true)
    var simplyMore = ValidatedBoolean(true)
    var epicKnights = ValidatedBoolean(true)
    var recipeIntegration = ValidatedBoolean(true)
    @Translatable.Desc("Diamond equipment requires steel gear, a diamond and a structure-found upgrade template.")
    @ConfigGroup.Pop var diamondUpgrades = ValidatedBoolean(true)
    val thermal = ConfigGroup("thermal", true)
    var thermalEnabled = ValidatedBoolean(true)
    var heatingTicks = ValidatedInt(100, 1200, 20)
    var workingHeatTicks = ValidatedInt(1200, 12000, 200)
    var shapingStrikes = ValidatedInt(6, 40, 1)
    @Translatable.Desc("Harder materials require faster and more precise strikes.")
    var materialDifficulty = ValidatedBoolean(true)
    @Translatable.Desc("Show occasional fixed-beat double and triple strike phrases.")
    var rhythmPhrases = ValidatedBoolean(true)
    var shapingBeatTicks = ValidatedInt(20, 60, 16)
    var shapingWindowTicks = ValidatedInt(3, 4, 1)
    var quenchingTicks = ValidatedInt(40, 200, 10)
    @Translatable.Desc("Burns from carrying hot metal without tongs. Cooling still runs when disabled.")
    var hotMetalHandling = ValidatedBoolean(true)
    var hotMetalDamage = ValidatedDouble(1.0, 4.0, 0.0)
    var burnIntervalTicks = ValidatedInt(40, 200, 20)
    @Translatable.Desc("One durability charge per player interval while handling hot stock; stack count never multiplies wear.")
    @ConfigGroup.Pop var tongsWearTicks = ValidatedInt(200, 1200, 20)
    val craftsmanship = ConfigGroup("craftsmanship", true)
    var qualityLuck = ValidatedDouble(0.20,0.35,0.0)
    var qualityMastery = ValidatedDouble(0.10,0.20,0.0)
    var qualityPower = ValidatedDouble(1.0,2.0,0.0)
    var timingJitter = ValidatedInt(6,12,0)
    @Translatable.Desc("Maximum one-way ping allowance for server-graded strikes; raw click cooldowns still apply. 0 disables it.")
    var latencyCompensationTicks = ValidatedInt(3,6,0)
    var sparks = ValidatedBoolean(true)
    var vanillaAnvilWorkshop = ValidatedBoolean(true)
    @ConfigGroup.Pop var blueprintMastery = ValidatedInt(1,20,0)
    @Translatable.Desc("Strength of stamped workshop material trade-offs; 0 disables them. Existing unstamped gear is unchanged.")
    var materialPower = ValidatedDouble(1.0,1.0,0.0)
    @Translatable.Desc("Strength of the single bounded trait rolled at forging completion. 0 disables traits on new work; finishing never rerolls them.")
    var traitPower = ValidatedDouble(1.0,1.0,0.0)
    val worldGeneration = ConfigGroup("worldGeneration", true)
    @Translatable.Desc("Generate Temper & Tang tin ore in new Overworld chunks.")
    var generateTinOre = ValidatedBoolean(true)
    @Translatable.Desc("Generate Temper & Tang silver ore in new Overworld chunks.")
    @ConfigGroup.Pop var generateSilverOre = ValidatedBoolean(true)
    val finishes = ConfigGroup("finishes", true)
    var temperedDurability = ValidatedDouble(0.15, 1.0, 0.0)
    var keenDamage = ValidatedDouble(0.05, 0.5, 0.0)
    var keenDurabilityCost = ValidatedDouble(0.10, 0.5, 0.0)
    var weightedDamage = ValidatedDouble(0.10, 0.5, 0.0)
    var weightedSpeedCost = ValidatedDouble(0.05, 0.5, 0.0)
    var lightenedSpeed = ValidatedDouble(0.05, 0.5, 0.0)
    var lightenedArmorSpeed = ValidatedDouble(0.01,0.03,0.0)
    @ConfigGroup.Pop var lightenedDurabilityCost = ValidatedDouble(0.10, 0.5, 0.0)
    override fun fileType() = FileType.JSON5
    fun balance() = FinishBalance(temperedDurability.get(), keenDamage.get(), keenDurabilityCost.get(),
        weightedDamage.get(), weightedSpeedCost.get(), lightenedSpeed.get(), lightenedDurabilityCost.get())
}
