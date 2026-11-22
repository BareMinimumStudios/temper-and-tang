# Verification — 2026-11-22

The 0.1.0 prototype was built with Java 21 and Gradle 9.9.2. Both loader builds, four common unit tests, resource checks, artifact checks and release staging passed.

Isolated Fabric 1.21.1 (loader 0.20.1) and NeoForge 1.21.1 (21.1.267) dedicated servers started with Diegesis beta.17, Fzzy Config, Overgeared 1.7.1.1 and Simply Swords 1.70.2. Both passed `/temperandtang verify` before and after datapack reload, saved all dimensions and exited normally. Both adapted 30 basic Simply Swords recipes to native Overgeared forging. Earlier standalone tests also passed without the optional integrations.

The server diagnostic checks station registration, item registration, all four finish results, enchantments, names, foreign custom data, damage preservation, non-stacking, common material tags and stable item serialization. With the optional mods present it additionally checks weapon acceptance, Expert forging quality preservation, raw durability adjustment and native forging recipe conversion. Persisted representations are compared after codec normalization because default component representations can differ in memory.

Overgeared applies quality to durability dynamically: finishes therefore adjust the raw maximum-durability component, avoiding a second quality multiplier. Existing attribute tooltip visibility is preserved.

Known external log messages: Fabric Simply Swords emits missing entity data-fixer notices; optional Better Combat and Lootr mixin targets are absent in this small fixture. Neither prevented startup or verification. Invalid optional Simply Swords recipes with explicitly false Fabric mod guards are filtered within that namespace on either loader.

Not verified: graphical client rendering, player interaction with Diegesis slot/button placement, multiplayer contention in a live client, long-duration gameplay balance and the complete material heating/shaping progression. Current artwork uses vanilla texture placeholders. These checks establish a server-tested prototype, not a finished release.

All test worlds and downloaded reference dependencies were isolated outside the repository and existing Modrinth profiles. No profile was modified and nothing was published.
## 0.1.1 workstation input checks

Both dedicated-server targets passed item preservation and the new paced station-action diagnostic, including alternating button and keyboard action paths, synchronized progress, one-material consumption, XP cost, hammer wear and duplicate-output prevention. Both saved and stopped normally.

An isolated copy of New Test was used for a hidden NeoForge development client with a copied world. Automated clicks went through the actual Diegesis FocusScreen, filled the three slots, struck six times, took the finished weapon, and checked the resulting item, consumed material, two-level XP cost and six hammer wear. A second weapon completed the same cycle through six Space-key presses. Both complete routes passed. The caption and enlarged control were inspected from an off-screen screenshot.

The exact originally reported no-op could not be reproduced by clicking the old button center in this client fixture. Version 0.1.1 adds a larger explicit hitbox, visible guidance and an alternate input instead of assuming a confirmed root cause. The test copy excluded Diagonal Fences after an unrelated development-only model-loading failure; the installed profile was not changed by that exclusion. Other content and configurations were copied from New Test. The client harness is external to the shipped mod.

Further visual refinement and cancellation, restriction, save/reload and iron/gold progression coverage are in progress.
