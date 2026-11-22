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
@Version(1)
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
    @Translatable.Desc("Route basic Simply Swords iron/gold recipes through Overgeared when installed. Reload datapacks after changing.")
    @ConfigGroup.Pop var overgearedRecipes = ValidatedBoolean(true)
    val finishes = ConfigGroup("finishes", true)
    var temperedDurability = ValidatedDouble(0.15, 1.0, 0.0)
    var keenDamage = ValidatedDouble(0.05, 0.5, 0.0)
    var keenDurabilityCost = ValidatedDouble(0.10, 0.5, 0.0)
    var weightedDamage = ValidatedDouble(0.10, 0.5, 0.0)
    var weightedSpeedCost = ValidatedDouble(0.05, 0.5, 0.0)
    var lightenedSpeed = ValidatedDouble(0.05, 0.5, 0.0)
    @ConfigGroup.Pop var lightenedDurabilityCost = ValidatedDouble(0.10, 0.5, 0.0)
    override fun fileType() = FileType.JSON5
    fun balance() = FinishBalance(temperedDurability.get(), keenDamage.get(), keenDurabilityCost.get(),
        weightedDamage.get(), weightedSpeedCost.get(), lightenedSpeed.get(), lightenedDurabilityCost.get())
}
