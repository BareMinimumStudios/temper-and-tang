# Temper & Tang 0.6.1

Fixes the Alloy Furnace body disappearing in its focused Diegesis workstation view. Its scene now references an automatically loaded scene-model alias instead of an unregistered block-model location. Its heat control is also scaled to fit the furnace face rather than obscuring the work. World appearance, recipes, timing, save data and equipment stats are unchanged.

The complete steel-to-diamond and netherite alloy/refine/upgrade recording is verified in an isolated Survival test copy. No live profile is changed.

# Temper & Tang 0.6.0

Standalone medieval blacksmithing for Minecraft 1.21.1 Fabric and NeoForge.

## Survival progression

Mine tin and copper, refine them and alloy bronze. Bronze opens iron mining; carbon and iron produce steel. Steel equipment upgrades to diamond at a smithing table using a structure-found Diamond Upgrade Smithing Template. That template has no crafting or duplication recipe. Matching diamond gear upgrades to netherite using the vanilla template and a workshop-refined ingot.

The Alloy Furnace combines four netherite scraps and four gold ingots with separate fuel into one unrefined billet. Twelve strikes at 90% accuracy refine it into an ingot. Failure recovers four scrap; gold and fuel are spent. The first blow commits the attempt, so abandoning or cooling a started attempt also returns the failure output.

## Equipment and recipe viewers

Five craftsmanship grades affect durability, damage or protection, handling and mining speed where applicable. Accuracy is the main influence; mastery and bounded randomness contribute. Material trade-offs, one equipment trait and deliberate finishing remain separate layers. Existing names, enchantments, wear and foreign data survive processing; bonuses replace earlier bonuses rather than stacking or repairing gear for free.

Normal Simply Swords, Simply More and Epic Knights gear keeps its original item identity. Unique/runic weapons are excluded. Native iron-family weapons can be forged with a steel core for their diamond upgrade. Epic Knights heavy crossbows use their steel blueprint and gain craftsmanship-based arrow power, loading speed and durability, preserving Quick Charge and ammunition effects. Wooden bows keep their native crafting process.

EMI and JEI independently show refining, alloys, forging and quenching, finishing, steel-core upgrades and blueprint scribing. Steel cores and distinct blueprints have separate lookup identities; names and wear do not split recipes. When EMI is present, duplicate JEI/JEMI workshop registration is suppressed.

## Workstations and multiplayer

The supplied water barrel is hollow when empty; its animated water surface appears only when supplied. The fitted wooden pattern board has visible thickness and rests on the anvil. The shared hammer has continuous handle grain; tongs keep their three-dimensional proportions. Impact clangs and sparks follow hammer contact, with centered workstation cameras and a main-hand-side swing.

Anvil timing calibrates the connection automatically within the server's bounded allowance. Server-graded rhythm remains authoritative. Actual delayed two-client checks cover contention, separate simultaneous work, interrupted sessions, full-inventory recovery and repeated output collection. Session items return before clean shutdown saves; disconnected overflow remains reserved for its owner.

Requires Java 21, Diegesis beta.19, Fzzy Config and the loader's Kotlin runtime (plus Fabric API on Fabric). Optional equipment mods and recipe viewers are not bundled. Overgeared is not required. Licensed Workshop Six artwork and adaptations retain their separate notices in THIRD_PARTY_NOTICES.txt.

This is a tested first refining revision, not a claim that every third-party modpack or custom combat implementation is covered. See the revision acceptance record for the precise tested environments. Nothing is uploaded automatically.
