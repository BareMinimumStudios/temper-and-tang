# Refining revision

## Requested progression

Copper + tin → bronze → iron → steel → diamond → netherite. Silver remains a proposed specialist finish, not another required tier.

Five craftsmanship grades: Rough, Serviceable, Fine, Superior and Masterwork. Durability, attack damage/speed, armor and mining speed depend on workmanship. Material and one finishing treatment provide separate trade-offs. Existing Exceptional records are read as Masterwork; their stored attributes are rebuilt when the equipment is next processed.

Normal Simply Swords and Epic Knights equipment should retain its own item identity and abilities. Unique and runic weapons are excluded from forging/reforging. Diamond equipment upgrades from steel at a smithing table, using a structure-found template. Netherite requires demanding alloying and precision refinement before the normal diamond upgrade.

## Reference analysis

The supplied 15:05 Overgeared guide covers flint (0:00), copper forging (1:03), heating/tongs (2:52), steel (4:02), diamond and netherite (6:38), armor (8:15), blueprint/quality (9:22), and casting (11:51). The full captions were inspected. Chapter-spanning visual frames were inspected alongside the complete captions. These confirm steel-template upgrades, precision netherite refinement, blueprint quality progression, and reusable casts with a lower quality ceiling.

Useful principles: a necessary early metal tier, carbon alloying, template-gated steel upgrades, a separate high-temperature netherite alloy, a skill check with recoverable scrap on failure, and optional production molds with a quality ceiling. Avoid copying knapping, individual head assembly, and many extra stations into the core loop. Reuse the licensed supplied hearth/furnace and water barrel; do not copy assets from the video.

Remaining acceptance: supplied barrel and correctly proportioned volumetric tongs; actual mining-to-finishing loop; structure template availability and upgrade preservation; netherite alloy success/failure; native optional equipment and excluded uniques; independent EMI and JEI discovery; client/server concurrency, disconnect, cancellation and duplication checks; both loaders; isolated saved-world reload. All live profiles remain untouched.


## Development state

Implemented and compiled on both loaders: five ordered grades and old Exceptional migration; quality attack-speed/mining-speed effects; ordinary-only Simply Swords/Simply More forging; native iron-family weapon identities with steel-forged cores; structure-only diamond templates; present-recipe steel-to-diamond conversion; bronze access before iron mining; supplied barrel and volumetric tongs; vertex-preserving hammer/tong UV remapping; supplied-model nether alloy furnace and precision billet refinement.

Netherite route: four scraps + four gold, twelve hundred furnace ticks plus fuel, then twelve demanding shaping strikes. Accuracy of at least 90% yields one ingot; lower accuracy returns the four scraps and spends the gold/fuel. The billet may be reheated after cooling. A regular hearth refuses this alloy. Normal Netherite upgrade templates remain required for gear upgrades.

Isolated NeoForge bronze heat→shape→barrel→finish completed after the new model conversion. Rendered hammer contacts and submerged tong jaws passed their probes. Full model-gallery, progression, compatibility, recipe-viewer and multiplayer verification remain required; this is not yet a release claim.

Bootstrap audit: owned tongs accept bronze or iron ingots/nuggets, so bronze forging does not depend on mining iron first. Epic Knights diamond flame-bladed sword remains its native diamond-to-diamond cosmetic upgrade; it does not introduce an iron-to-diamond shortcut.


## Verification checkpoint

Both loader builds and 27 pure-rule tests pass. All 107 models were baked/rendered in an isolated client gallery; their parents, texture references, UVs and display transforms pass the resource audit. This exposed and corrected two inverted coal-bed elements and an unsupported furnace scene field.

The recipe-graph audit passed 62 steel-to-diamond upgrades, including 53 native mod equipment routes, with quality/name/wear preservation and unique/runic exclusions. That audit predates the generalized precision-refining serializer and must be repeated on the final artifacts. Independent EMI, JEI and combined-viewer runtime tests, full mining progression, the new furnace's actual controls, failure/recovery, save/reload and multiplayer acceptance remain open.

Recipe extension contracts: `temperandtang:alloying` accepts `requires_advanced_furnace`; `temperandtang:precision_refining` extends shaped recipe data with `minimum_accuracy`, `failure_result` and `strikes`. Add secondary ingredients to `temperandtang:alloy_additives`, and metal/billet inputs to `temperandtang:heatable_stock`. The normal count limit is eight items per workstation stock slot.

Precision attempts commit on the first accepted blow. Inputs and the hammer are locked during that attempt. Closing or allowing the stock to cool after starting returns the failure output and charges only accepted hammer blows; closing before the first blow returns the untouched billet. This prevents free retries by cancelling a poor attempt.


## Runtime checkpoint — refinement and recipe viewers

Both-loader builds and 29 pure-rule tests pass. All 108 models were baked and rendered; all nine gallery pages were inspected, with 36 additional multi-angle tool/station views. Hammer handle bands were replaced with continuous wood grain in a separate model-fix commit.

The current recipe-graph and real station-logic checks passed 62 steel-to-diamond upgrades (53 native), exact upgrade preservation, bronze mining eligibility, unique/runic exclusions and furnace-only netherite alloying. A full-duration furnace diagnostic consumed four scraps and four gold with one fuel charge. Twelve accurate blows produced one ingot; deliberately poor blows produced four scrap. Both paths consumed their input once, charged the hammer correctly, refused a second collection, and preserved outputs through item serialization.

Cancellation was verified through the station close/item-return sequence: preparation returns the billet unchanged; after the first blow, inputs and controls lock and closure recovers exactly four scrap with accepted hammer wear. Repeated closure cannot duplicate the billet or scrap.

JEI alone rendered all six workshop categories, including blueprint scribing, steel-core upgrades and precision refinement. Actual JEI item equivalence distinguishes ordinary iron from a steel-forged native weapon and distinguishes blueprint patterns, while custom names/wear do not split recipe lookup. EMI alone and with JEI rendered the categories. EMI alone also passed the same steel-core, blueprint, name and wear comparison checks.

Station-manager checks cover all five stations using two simulated connections: exclusive ownership, unauthorized and stale actions, logout/cursor return, subsequent owner access and full-inventory overflow. This is not real remote-client coverage. Actual network latency, interrupted connections with real clients, both-loader saved-world testing, full-pack-copy startup and the final mining-through-finishing walkthrough remain acceptance work.


The standalone dedicated NeoForge server passed the equipment/catalog checks (367 forging patterns: 246 Epic Knights, 30 Simply Swords, 20 Simply More, and remaining standalone/vanilla patterns), progression checks and the timed netherite success/failure loop, then saved and shut down normally.

The isolated GUI bronze walkthrough now starts with actual survival block destruction and generated tin/copper loot. It completed refining, bronze alloying, six shaping strikes, barrel quenching and six finishing strikes, with exact fuel/leftover accounting and no hot-metal damage while tongs were equipped. Both anvil contact probes and the airflow/track probes passed. Six crafted/worn equipment records and mastery were checked after an actual saved-world restart. The iron-to-steel GUI route and fresh full-pack copy remain in progress.


## Full-pack integration checkpoint

A read-only snapshot of the current New Test profile (262 existing dependency/content JARs plus the replacement development build) exposed Reliable Recipes applying its equipment edits after the original workshop hook. Routing now runs on the final JSON map immediately before recipe parsing, after HEAD recipe modifiers. This respects removed patterns and converts late additions. The current full-pack audit passes 55 steel-to-diamond routes and 55 diamond-to-netherite routes, with no supported equipment crafting/shaping shortcuts. Native food knives and cleavers use their own steel-forged iron-family identities when no native steel item exists. The backported diamond spear upgrades from the existing standalone steel spear.

Netherite equipment requires its matching diamond base, a vanilla netherite upgrade template and a netherite ingot. Normal mod-added crafting alternatives are converted to the same route. Dedicated checks passed 63 netherite upgrade recipes on the NeoForge compatibility fixture and 53 on Fabric, including quality/name/wear preservation. Every generated steel-core pattern is now checked through forging, finishing and reforging; reforging retains its recorded steel material.

Both bronze and steel GUI walkthroughs passed from actual survival ore drops through their alloy, shaping, quenching and finishing paths. Both retained six real crafted/worn equipment records and mastery after saved-world restart. The Fabric client rendered the six workshop categories and passed variant matching as well as the recipe/slot audits. The full-pack copy rendered the same categories in EMI after its progression audit.

Full-pack fixture caveats: an unrelated MidnightLib concurrent-initialization failure occurred during one launch; only the disposable copy uses `fml.toml` startup `maxThreads = 1` to avoid that race. After successful world save, the pack also logs Small Ships' existing `smallships-server.toml` unload exception. No live New Test files were modified, no client content mods were disabled, and these third-party startup/shutdown issues are not claimed fixed in the live profile.

Outstanding acceptance remains actual two-client network interruption/latency checks, a normal-equipment catalog gap audit (including ranged equipment), final configurable-rule checks, final artifacts and preview review. This checkpoint is not release completion.


## Real network checkpoint

Two independent hidden clients joined a dedicated server through real TCP connections on each loader. The owner's connection was delayed 100 ms in each direction; the guest joined directly. On both Fabric and NeoForge, exclusive station access, disconnect/reconnect custody, held-cursor return, empty station reacquisition, first-strike cancellation, and repeated collection passed. A first strike on a fresh connection scored 100%; the complete twelve-strike precision refinement also scored 100% with zero misses, producing exactly one ingot despite a repeated collection request. Invalid timing acknowledgements were ignored.

These checks exposed two issues beyond simulated-player coverage. Diegesis normally drops session items when a player has disconnected, so our stations now return them before the library's fallback and explicitly save the player's inventory. Disconnected full-inventory overflow is owner-reserved and does not expire. Actual full-inventory disconnect pickup behavior remains a separate acceptance check. A short server-owned round-trip exchange at anvil opening now calibrates the visible rhythm and the server's evaluation together, rather than waiting for Minecraft's initially-zero averaged ping. Three samples use the minimum observed round trip, compensation remains bounded by configuration, and neither strike time nor accuracy comes from the client.

The exact network test artifacts are recorded in the isolated workspace's `network-060/result.json` and `network-fabric-060/result.json`, including build checksums, actual inventory snapshots, scores and single-output counts. No live profile was modified. Remaining acceptance includes parallel work at separate stations, disconnected overflow custody, natural tin/structure-template availability, the normal-equipment gap audit and final artifact/configuration checks.
