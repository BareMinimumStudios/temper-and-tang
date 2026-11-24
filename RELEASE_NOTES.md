# Temper & Tang 0.5.1

Standalone medieval smithing for Minecraft 1.21.1 Fabric and NeoForge.

Press **F** at the shaping anvil to search patterns by equipment name or recipe identifier, then click a result. Selection works while holding hot stock; Escape returns to the station. A complete placed ingredient layout can choose a matching recipe automatically. Valid stored pattern selections survive reopening. Cooling continues normally, and active work stays protected.

The four workstation cameras now face the work directly. The supplied Workshop Six hearth, anvils, hammer, tongs and bellows replace the workshop presentation, with a Mine Furniture pallet stand and matching timber. The complete hearth fits inside one block; collision outlines follow the station models in all four directions. Creator credit and separate artwork rights are retained.

Rhythm notes continue along the same path when a phrase advances. Bounded clock correction prevents backward jumps between server updates. The hammer swings from the main-hand side, mirrored for left-handed players, using the supplied model’s proportions. Tool contact positions, dipping acceleration and tong closure are refined; air bursts follow actual bellows compression. Cached leather meshes reduce repeated allocation during pumping. The server still determines timing, quality, costs and item transfers.

The additive dish accepts common tin ingots, coal and charcoal through `temperandtang:alloy_additives`. Finishing materials remain restricted to the finishing-treatment tags. Recipes and the equipment progression are preserved.

Requires Diegesis beta.19, Fzzy Config and the loader's Kotlin runtime. Simply Swords, Simply More and Epic Knights remain optional integrations. Overgeared is not required. Artwork rights are described in THIRD_PARTY_NOTICES.txt.
