package com.bareminimumstudios.temperandtang.compat

import com.bareminimumstudios.temperandtang.TemperAndTang
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

/** Optional native components stay opaque; no optional mod class is linked at startup. */
object NativeSmithing {
    private var checked = false
    private var supported = false
    fun loaded(): Boolean {
        if (!TemperAndTang.modLoaded("overgeared")) return false
        if (!checked) {
            supported = runCatching {
                Class.forName("net.stirdrem.overgeared.AnvilTier").getMethod("stone")
                Class.forName("net.stirdrem.overgeared.config.ServerConfig").getField("HEATED_ITEM_COOLDOWN_TICKS")
                Class.forName("net.stirdrem.overgeared.util.ItemUtils").getMethod("getCooledItem", ItemStack::class.java, Level::class.java)
            }.isSuccess
            checked = true
            if (!supported) TemperAndTang.logger.warn("The installed Overgeared API is unsupported for thermal stations; native forging has been left in place.")
        }
        return supported
    }
    @Suppress("UNCHECKED_CAST")
    private fun <T> component(name: String): DataComponentType<T>? = if (loaded())
        BuiltInRegistries.DATA_COMPONENT_TYPE.get(ResourceLocation.fromNamespaceAndPath("overgeared", name)) as? DataComponentType<T> else null
    fun config(name: String): Any? {
        if (!loaded()) return null
        val value = Class.forName("net.stirdrem.overgeared.config.ServerConfig").getField(name).get(null)
        return value.javaClass.getMethod("get").invoke(value)
    }
    fun hot(stack: ItemStack): Boolean = !stack.isEmpty && loaded() &&
        (component<Boolean>("heated")?.let { stack.get(it) } == true ||
            listOf("heated_metals", "hot_items").any { stack.`is`(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("overgeared", it))) })
    fun heatTime(stack: ItemStack): Long? = component<Long>("heated_time")?.let { stack.get(it) }
    fun heat(stack: ItemStack, now: Long) {
        component<Boolean>("heated")?.let { stack.set(it, true) }
        component<Long>("heated_time")?.let { stack.set(it, now) }
    }
    fun clearHeat(stack: ItemStack) {
        component<Boolean>("heated")?.let { stack.remove(it) }
        component<Long>("heated_time")?.let { stack.remove(it) }
    }
    fun cooled(stack: ItemStack, level: Level): ItemStack {
        if (!loaded()) return stack.copy()
        val cls = Class.forName("net.stirdrem.overgeared.util.ItemUtils")
        val result = (cls.getMethod("getCooledItem", ItemStack::class.java, Level::class.java).invoke(null, stack.copy(), level) as ItemStack).copy()
        cls.getMethod("copyComponentsExceptHeated", ItemStack::class.java, ItemStack::class.java).invoke(null, stack, result)
        clearHeat(result)
        result.count = stack.count
        return result
    }
    fun quality(stack: ItemStack): String? = component<Any>("forging_quality")?.let { (stack.get(it) as? Enum<*>)?.name }
    fun setQuality(stack: ItemStack, name: String) {
        val value = Class.forName("net.stirdrem.overgeared.ForgingQuality").enumConstants.first { (it as Enum<*>).name == name }
        component<Any>("forging_quality")?.let { stack.set(it, value) }
    }
    fun markForged(stack: ItemStack, quality: String?, polished: Boolean, creator: String) {
        if (quality != null && config("ENABLE_MINIGAME") == true) setQuality(stack, quality)
        if (polished && config("ENABLE_MINIGAME") == true) component<Boolean>("polished")?.let { stack.set(it, false) }
        if (config("PLAYER_AUTHOR_TOOLTIPS") == true) component<String>("creator")?.let { stack.set(it, creator) }
    }
    @Suppress("UNCHECKED_CAST")
    fun hammerAllowed(stack: ItemStack): Boolean {
        if (!loaded() || config("ENABLE_TIERED_HAMMERS") != true) return true
        val tier = Class.forName("net.stirdrem.overgeared.AnvilTier").getMethod("stone").invoke(null)
        if (tier.javaClass.getMethod("isEnabled").invoke(tier) != true) return false
        val tag = tier.javaClass.getMethod("hammerTag").invoke(tier) as TagKey<net.minecraft.world.item.Item>
        return stack.`is`(tag)
    }
    fun stoneEnabled(): Boolean = !loaded() || Class.forName("net.stirdrem.overgeared.AnvilTier").getMethod("stone").invoke(null).let {
        it.javaClass.getMethod("isEnabled").invoke(it) == true
    }
}
