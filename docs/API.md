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



Recipe integration uses a MixinExtras WrapOperation at iteration of the final recipe JSON map, after other HEAD recipe modifiers and before parsing. It never adds a global loot blacklist and does not override already-converted forging/assembly recipes.



## Thermal recipes and stock



`temperandtang:heatable_stock` accepts common refined ingots by default and can be extended additively. Do not add raw ores to this tag unless intentionally allowing direct reheating of them. The hearth preserves components and applies the mod's own timed heat state. It does not invoke another smithing mod's heating or quality mechanics.



`temperandtang:shaping` and `temperandtang:shaping_shapeless` retain vanilla recipe fields while matching only the dedicated shaping recipe type. Supported original equipment recipes are routed at reload when `recipeIntegration` and thermal shaping are enabled. Original IDs, conditions, ingredients and result components remain authoritative. Removed recipes are not recreated.



`forgeable_equipment` and `excluded_equipment` are additive item tags; exclusions win. Existing weapon inclusion/exclusion tags remain accepted for compatibility. `TemperAndTangApi.FORGEABLE_EQUIPMENT`, `EXCLUDED_EQUIPMENT` and `registerEquipmentPredicate` expose the equipment API. Register deterministic predicates during common setup on both sides.



Craftsmanship schema version 1 lives under `minecraft:custom_data` -> `temperandtang.craftsmanship`. It stores grade, accuracy, effective score, random contribution, maker, mastery and pattern. It also owns a raw durability baseline. Do not write bonuses directly: use station processing so costs, eligibility and normalization remain server-authoritative. Saved world mastery belongs to `temperandtang_smithing`.



Thermal schema version 1 lives under `minecraft:custom_data` → `temperandtang.thermal`, storing a world-tick `hot_until`, stage, and (only for a standalone unquenched workpiece) encoded `weapon`. Treat this as owned persistence data rather than a public modifier API. All completion and consumption must run through the server station; client captions and animations never authorize a result.



## Tongs and contact heat



`TemperAndTangApi.TONGS` exposes the additive `temperandtang:tongs` item tag. The common `c:tools/tongs` tag is included by default. Damageable members use normal durability/unbreaking handling; explicitly tagged non-damageable protective tools remain valid. Held tools can protect in either hand. The thermal timestamp belongs to Temper & Tang and does not require any optional mod component.



Hot-metal handling runs on the server across player inventory slots and the cursor. Contact burns and tool wear are bounded per player rather than per hot stack. The public configuration can disable exposure while keeping cooling active. Libraries remain Diegesis and Fzzy Config; another smithing mod is never required.



## Material and trait records

Extend `temperandtang:materials/<material>` additively for iron, bronze, steel, gold, copper, tin, diamond or netherite. Defaults reference optional common ingot/gem tags. Equipment can also be assigned to these tags. A recognized output profile wins; otherwise refined recipe inputs select the dominant recognized profile. Unknown materials are neutral. Avoid assigning one item to several material profiles.

The reserved custom-data root additionally stores `material`, `material_power`, `trait`, `trait_power` and `trait_version`. Material and trait keys are stable lowercase identifiers; invalid identifiers read as neutral/none. Powers are captured when stamped. These are persistence records, not an invitation to bypass the server's costs or RNG. Attribute IDs beginning with `temperandtang:craft_` or `temperandtang:finish_` are owned by this mod and are rebuilt from the original baseline.

Metal tiered tools are supported even without positive attack-damage modifiers. Mining bonuses use the vanilla block-break-speed attribute in the main hand. Custom ability/projectile damage systems outside the normal equipment attributes still require specific integration.


## Counted hearth alloys (0.5)

Register datapack recipes under the `temperandtang:alloying` type. Both loaders share the same JSON and network serializer:

```json
{
  "type": "temperandtang:alloying",
  "primary": {"tag": "c:ingots/copper"},
  "primary_count": 3,
  "additive": {"tag": "c:ingots/tin"},
  "additive_count": 1,
  "result": {"id": "temperandtang:bronze_ingot", "count": 4},
  "cookingtime": 200
}
```

Counts are 1–8; cooking time is 1–12000 ticks per recipe batch (default 200). The hearth requires complete primary batches, enough additive and a total output of at most eight. Extra additive remains in its slot. Result items must belong to heatable stock. Default-namespace Temper & Tang alloys take precedence; remaining matches sort by recipe ID. Override/remove the default alloy when authoring a different canonical output. Fuel is computed separately from process duration.

`temperandtang:refinable_stock` permits raw ingredients to use their installed `minecraft:smelting` recipe. This tag alone cannot invent a recipe or make a non-metal result heatable. Ingredient and result counts, recipe conditions and removals remain authoritative. The result receives the mod's hot-metal state. No smelting XP is generated by the hearth.

The catalog adds `c:ingots/steel`, `c:nuggets/steel`, `c:storage_blocks/steel` and parent tags, repair tags, vanilla mining/enchantment/trim tags and optional Better Combat weapon profiles. Tags are additive. Existing third-party stacks and recipes are not globally replaced.

With Epic Knights integration enabled, only its unmodified `magistuarmory:furnace/steel_ingot_blasting` signature (one iron, one steel, 40 ticks, 0.5 XP) is bridged to the counted carbon alloy. Its ID, output and conditions remain intact. A different duration/ingredient/result or `"temperandtang:keep_native": true` opts out. Missing recipes stay missing. Turning recipe integration or thermal processing off restores normal native loading on reload.


## Additive sorting tags (0.7.1)

Both loaders ship the same `c:` material, ore, storage-block, tool, melee-weapon, mining-tool, armor and enchantable tags. Bronze and steel pickaxes belong to `c:tools/mining_tool`; swords and axes belong to `c:tools/melee_weapon`. Vanilla equipment tags remain authoritative for individual tool and armor slots. Steel storage blocks have both item and block tags.

Workshop-specific categories use `temperandtang:workstations` (item/block), `blueprints`, `finishing_materials`, `alloy_compounds`, `workpieces` and `smithing_templates`. These categories do not have an established common equivalent, so they use the mod namespace instead of claiming a new common standard. Existing recipe-input tags such as `honing_abrasives`, `tempering_binders` and `finishing_materials/<finish>` remain the extension points for accepted ingredients. Sorting tags alone never grant fuel, slot access or a new recipe. All tags are additive (`replace: false`); authors may reference them from recipes or extend them with datapacks.
