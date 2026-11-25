package com.bareminimumstudios.temperandtang.content

import com.bareminimumstudios.temperandtang.TemperAndTang
import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.util.RandomSource
import net.minecraft.world.level.levelgen.placement.PlacementContext
import net.minecraft.world.level.levelgen.placement.PlacementFilter
import net.minecraft.world.level.levelgen.placement.PlacementModifierType

internal class OreGenerationFilter private constructor(private val silver: Boolean) : PlacementFilter() {
    override fun shouldPlace(context: PlacementContext, random: RandomSource, pos: BlockPos): Boolean =
        if (silver) TemperAndTang.config.generateSilverOre.get() else TemperAndTang.config.generateTinOre.get()

    override fun type(): PlacementModifierType<*> = if (silver) SILVER else TIN

    companion object {
        val TIN = PlacementModifierType { MapCodec.unit(OreGenerationFilter(false)) }
        val SILVER = PlacementModifierType { MapCodec.unit(OreGenerationFilter(true)) }
    }
}
