# Refining revision

## Requested progression

Copper + tin → bronze → iron → steel → diamond → netherite. Silver remains a proposed specialist finish, not another required tier.

Five craftsmanship grades: Rough, Serviceable, Fine, Superior and Masterwork. Durability, attack damage/speed, armor and mining speed depend on workmanship. Material and one finishing treatment provide separate trade-offs. Existing Exceptional pieces migrate to Masterwork.

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
