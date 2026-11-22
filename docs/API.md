# Compatibility API and common tags

Material conventions use `c:` tags on both Fabric and NeoForge 1.21.1:

- `c:ingots/tin`, `c:ingots/bronze`, plus parent `c:ingots`.
- `c:nuggets/tin`, `c:nuggets/bronze`, plus parent `c:nuggets`.
- `c:raw_materials/tin`, plus parent `c:raw_materials`.
- `c:storage_blocks/tin`, `c:storage_blocks/bronze`, `c:storage_blocks/raw_tin` on items and blocks.
- `c:ores/tin`, `c:ores`, `c:ores_in_ground/stone`, `c:ores_in_ground/deepslate`, `c:ore_rates/singular` on items and blocks.

Alloy/compression/decompression recipes accept shared tags. Owned ore smelting stays scoped to our ore/raw items, so we do not silently replace another mod's smelting recipes. Tags allow cross-mod ingredients; they do not forcibly replace or merge existing inventory stacks. Almost Unified can choose canonical outputs in a modpack.

Weapons use `minecraft:swords` and `c:tools/melee_weapon`. The hammer is in `c:tools` and `c:tools/hammers`. Native Overgeared hammers are optional members of `temperandtang:smithing_hammers`. Mining tags are supplied for all metal blocks and ores. Common tags are additive (`replace: false`); there are no obsolete `forge:` aliases or hard dependency on a tech mod.

Crafting also accepts `c:ingots/copper`, `c:nuggets/iron`, `c:rods/wooden`, `c:leathers`, `c:strings`, `c:sands` and `minecraft:coals`. Vanilla fallback members keep these inputs usable on either loader. `temperandtang:tempering_binders` and `temperandtang:honing_abrasives` allow additional clay-like binders and abrasives without replacing recipes.

## Datapacks: no code required

Add another mod's damageable weapon to `data/temperandtang/tags/item/finishable_weapons.json`. Block an item with `excluded_weapons.json`; exclusions win. Per-mod Simply Swords/Simply More switches still apply.

Add another mod's ingredients to `data/temperandtang/tags/item/finishing_materials/tempered.json` (also `keen`, `weighted`, `lightened`). Add compatible damageable hammers to `smithing_hammers.json`. If an ingredient belongs to multiple finish tags, enum order is deterministic; prefer one tag per ingredient. Reusable containers/remainders are not supported as finish ingredients yet: use a consumable item.

## Code integrations

`com.bareminimumstudios.temperandtang.api.TemperAndTangApi` exposes `FINISHABLE_WEAPONS`, `EXCLUDED_WEAPONS`, `finishingMaterialTag(name)` and `registerWeaponPredicate(java.util.function.Predicate<ItemStack>)` with Java-friendly static methods. Register predicates during common setup on both sides. Predicates run on the main thread, must be deterministic and side-effect-free, and must not reference client-only classes. Tag integration is preferred for fixed item lists.

The current API adds support for existing finish types. Arbitrary new finish definitions, material fluids and dynamic alloy profiles are future API work. Item finish schema version 1 is stored under `minecraft:custom_data` -> `temperandtang`; do not rewrite that data to apply bonuses. Let the server station calculate and consume the result. Finishes currently affect melee attributes and durability; custom projectile damage propagation needs its own integration.

Recipe integration uses MixinExtras WrapMethod to compose with the normal recipe reload path. It never adds a global loot blacklist and does not override already-converted forging/assembly recipes.
