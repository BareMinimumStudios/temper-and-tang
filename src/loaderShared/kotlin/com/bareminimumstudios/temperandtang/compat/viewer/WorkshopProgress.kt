package com.bareminimumstudios.temperandtang.compat.viewer

import net.minecraft.Util
import net.minecraft.client.gui.GuiGraphics

/** Illustrates the recipe's direction and process without implying a processing time. */
internal object WorkshopProgress {
    fun draw(graphics: GuiGraphics, category: WorkshopCategory) {
        val accent = when (category) {
            WorkshopCategory.REFINING -> 0xFFE49A45.toInt()
            WorkshopCategory.ALLOYING -> 0xFFF2B45B.toInt()
            WorkshopCategory.FORGING -> 0xFFD47D40.toInt()
            WorkshopCategory.FINISHING -> 0xFF719A9F.toInt()
            else -> 0xFFAA9675.toInt()
        }
        val progress = (Util.getMillis() % 1600L * 18 / 1600).toInt()
        graphics.fill(109, 41, 130, 46, 0xFF554737.toInt())
        graphics.fill(110, 42, 110 + progress, 45, accent)
        for (row in -4..4) {
            graphics.fill(130, 43 + row, 135 - kotlin.math.abs(row), 44 + row, accent)
        }
        if (category == WorkshopCategory.REFINING || category == WorkshopCategory.ALLOYING) {
            for (ember in 0..2) {
                val rise = ((Util.getMillis() / 100 + ember * 3) % 8).toInt()
                val x = 114 + ember * 5
                graphics.fill(x, 39 - rise, x + 1, 41 - rise, accent)
            }
        }
    }
}
