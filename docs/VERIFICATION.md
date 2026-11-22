# Verification — 2026-11-22

The 0.1.0 prototype was built with Java 21 and Gradle 9.9.2. Both loader builds, four common unit tests, resource checks, artifact checks and release staging passed.

Isolated Fabric 1.21.1 (loader 0.20.1) and NeoForge 1.21.1 (21.1.267) dedicated servers started with Diegesis beta.17, Fzzy Config, Overgeared 1.7.1.1 and Simply Swords 1.70.2. Both passed `/temperandtang verify` before and after datapack reload, saved all dimensions and exited normally. Both adapted 30 basic Simply Swords recipes to native Overgeared forging. Earlier standalone tests also passed without the optional integrations.

The server diagnostic checks station registration, item registration, all four finish results, enchantments, names, foreign custom data, damage preservation, non-stacking, common material tags and stable item serialization. With the optional mods present it additionally checks weapon acceptance, Expert forging quality preservation, raw durability adjustment and native forging recipe conversion. Persisted representations are compared after codec normalization because default component representations can differ in memory.

Overgeared applies quality to durability dynamically: finishes therefore adjust the raw maximum-durability component, avoiding a second quality multiplier. Existing attribute tooltip visibility is preserved.

Known external log messages: Fabric Simply Swords emits missing entity data-fixer notices; optional Better Combat and Lootr mixin targets are absent in this small fixture. Neither prevented startup or verification. Invalid optional Simply Swords recipes with explicitly false Fabric mod guards are filtered within that namespace on either loader.

Not verified: graphical client rendering, player interaction with Diegesis slot/button placement, multiplayer contention in a live client, long-duration gameplay balance and the complete material heating/shaping progression. Current artwork uses vanilla texture placeholders. These checks establish a server-tested prototype, not a finished release.

All test worlds and downloaded reference dependencies were isolated outside the repository and existing Modrinth profiles. No profile was modified and nothing was published.