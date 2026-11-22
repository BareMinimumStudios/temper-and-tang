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

Follow-up coverage is recorded under 0.1.2 below.


## 0.1.2 packaged prototype

Java 21, Gradle 9.9.2, both loader builds, the common unit tests, resource verification, distributable metadata checks and release staging passed. The verifier checks that client initialization and workshop models are packaged and that temporary client-harness classes are absent.

| Check | Result |
| --- | --- |
| Fabric and NeoForge with Overgeared/Simply Swords | 17 crafting, 8 cooking and 31 native forging recipes matched and assembled; item and station diagnostics passed |
| Both loaders without those optional mods | 18 crafting and 8 cooking recipes matched and assembled; item and station diagnostics passed |
| Native weapon spread | All 15 iron, 15 gold and the bronze sword recipe matched their native grids and assembled |
| Actual packaged NeoForge client | Six mouse strikes and six Space presses completed two survival finishes; both outputs were taken through actual mouse picking |
| Costs | One material, two levels and six hammer wear per completed finish; duplicate output refused |
| Preservation | Damaged, named, enchanted iron sword and native Expert-quality gold longsword retained their data |
| Real save/reload | Both named weapons, damage, enchantments, finishes and the gold weapon's Expert quality restored from a saved world |
| Cancellation | Closing after three strikes returned the named enchanted weapon, foreign data, material and hammer without charging XP or wear |
| Restrictions | Finished weapons refused repeat work; worn hammers, zero-XP output, click spam and writes to locked progress were checked |
| XP recovery | A ready result remained obtainable after gaining enough levels, without repeating the work |
| Tool integration | Native Overgeared hammer acceptance and wear passed the server scenarios |
| Audio | Client received 12 accepted strike sounds and 2 completion sounds; cancelled work added 3 strikes and rejected work added none |
| Reload/shutdown | Both integration servers reloaded recipes, re-verified, saved and stopped; client and standalone servers saved and stopped normally |

The final client run loaded the actual NeoForge distribution JAR through a separate temporary harness. It did not compile a second copy of the mod's gameplay code. Its SHA256 was checked against the installed New Test file. An off-screen screenshot was inspected for the final wood/copper anvil, hammer target, captions and layout.

The isolated client copy excluded Diagonal Fences because its model modification failed in the development launcher. This test-only exclusion was not applied to New Test. No live two-player contention test or complete human playthrough of every Overgeared timing minigame was performed. Native recipe matching/assembly and finishing were verified as described above. Further thermal simulation and additional artwork remain future work.

New Test received 0.1.2 after its mods, settings, worlds and gameplay data were backed up and 1,308 files checked. Required dependency JARs matched the tested copies. Existing food unification, workshop progression, settings and worlds were preserved.
