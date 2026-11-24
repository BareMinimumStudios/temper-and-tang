# Compatibility

Temper & Tang owns heating, shaping, quenching, craftsmanship, mastery and finishing. It requires Diegesis, Fzzy Config and the loader's Kotlin runtime.

Supported damageable, non-stackable metal equipment includes weapons, tools, shields and armor. Simply Swords, Simply More and Epic Knights are detected by registry namespace without linking their classes. Each integration has a server configuration switch. Wooden/stone gear, leather and animal armor are excluded by default. Datapack inclusion and exclusion tags and deterministic predicates allow other authors to extend support.

Shaped and shapeless equipment recipes with metal, diamond or supported gear inputs are routed to the standalone shaping type at reload. Original item IDs, authored patterns, ingredients, result components and load conditions remain authoritative. Epic Knights equipment requires a reusable matching blueprint. A modpack that removes a recipe must explicitly restore it if it wants that pattern available; Temper & Tang does not undo recipe blacklists.

Results preserve names, enchantments, damage, repair costs and foreign custom data. Temper & Tang rebuilds only its own attribute modifiers from the original durability baseline. Repeated processing replaces earlier bonuses rather than stacking them. Netherite upgrades rebase onto the upgraded item's original stats while retaining workmanship and finishing. Excessively worn gear is refused instead of silently repaired.

Third-party code that calculates damage outside the normal item attributes may need a separate integration. The native Epic Knights heavy crossbow uses steel forging and craftsmanship: arrow power and loading speed reflect quality, material, trait and finish, while Quick Charge and ammunition effects remain native. Wooden bows stay on their original crafting route. Other projectile systems are unchanged unless they opt in a `CrossbowItem` through `forgeable_equipment`; custom projectiles that are not vanilla arrows require their own integration. Shared ingredient tags permit compatible inputs; they do not merge existing stacks or enforce a pack's canonical output. Almost Unified remains responsible for that policy.

Owned tongs use additive `c:tools/tongs` and `temperandtang:tongs` tags. Compatible hammer tools use the common hammer tag and `smithing_hammers`. Existing optional tag entries do not create a runtime dependency on their owner.

See [the workshop guide](THERMAL_WORKSHOP.md), [API](API.md) and [verification record](VERIFICATION.md).



Version 0.4.0 checks every available routed pattern through actual output forging, quenching, a compatible finish and serialization. The NeoForge fixture also exercised Simply More from a separate optional test copy. Its disabled state in the live profile is preserved. Vanilla metal hoes are included in routing rather than bypassing the workshop because of low attack damage.

Material/trait strengths are saved with forged items. Finishing and ordinary netherite upgrades preserve those records; the latter refreshes the material and the raw stat baseline. Default guidance is short, H shows station help and Shift reveals equipment details. No compatibility path enables another mod's loot or implicit abilities, or restores a recipe removed by the pack.


## Catalog and metallurgy (0.5)

The standalone catalog supplies tin, bronze and steel tools, swords and armor. Extra weapon forms are supplied by optional equipment mods. It uses native tool/armor behavior, repair tags, vanilla trim/enchantment tags and optional Better Combat profiles. Another smithing or weapon mod is not needed.

Epic Knights common steel and nugget tags accept workshop materials. Both loader fixtures matched and assembled 77 native component recipes using those inputs. Its default fast iron-to-steel blast recipe is narrowly bridged into a carbon alloy; pack-authored alternatives and deleted recipes remain untouched. See the API for the explicit opt-out. Native steel armet gilding retains material, trait, workmanship, finish and wear even when the decorated output name does not identify steel.

Forging an upgrade keeps foreign component patches, portable vanilla metadata and foreign attribute additions unless the result explicitly authors that component. The new item's native base stats remain authoritative. Only the external raw durability difference carries forward; previous craftsmanship multipliers are not baked into the next baseline. Recipe-authored custom data wins on conflicts. Finishing and native smithing upgrades retain the original workmanship as before.


Silver resources are supplied directly and tagged under `c:ores/silver`, `c:raw_materials/silver`, `c:ingots/silver`, `c:nuggets/silver` and refined/raw storage categories, with parent item/block tags. Tin and silver categories are deliberately disjoint. Epic Knights silver recipes keep their original IDs/results and resolve the common silver inputs; they require their matching blueprint.
