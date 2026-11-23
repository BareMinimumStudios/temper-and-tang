package com.bareminimumstudios.temperandtang.smithing

import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.saveddata.SavedData
import java.util.UUID

class SmithingProgress : SavedData() {
    private val experience=mutableMapOf<UUID,Long>()
    internal fun forgetFixture(id:UUID) {experience.remove(id);setDirty()}
    fun xp(id:UUID)=experience[id]?:0L
    fun level(id:UUID)=CraftRules.level(xp(id))
    fun award(id:UUID,amount:Int) { experience[id]=(xp(id)+amount.coerceIn(0,1000)).coerceAtMost(1_000_000_000L);setDirty() }
    override fun save(tag:CompoundTag,provider:HolderLookup.Provider):CompoundTag {
        val entries=CompoundTag();experience.forEach {(id,xp)->entries.putLong(id.toString(),xp)}
        tag.put("experience",entries);tag.putInt("version",1);return tag
    }
    companion object {
        fun load(tag:CompoundTag,provider:HolderLookup.Provider)=SmithingProgress().also {data->
            val entries=tag.getCompound("experience")
            entries.allKeys.forEach {key->runCatching {UUID.fromString(key)}.getOrNull()?.let {data.experience[it]=entries.getLong(key).coerceIn(0,1_000_000_000L)}}
        }
        fun get(server:MinecraftServer):SmithingProgress=server.overworld().dataStorage.computeIfAbsent(Factory(::SmithingProgress,::load,null),"temperandtang_smithing")
    }
}
