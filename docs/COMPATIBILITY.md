# Compatibility

Temper & Tang owns heating, shaping, quenching, craftsmanship, mastery and finishing. It requires Diegesis, Fzzy Config and the loader's Kotlin runtime. It does not depend on Overgeared or import its native minigames or heating components.

Supported damageable, non-stackable metal equipment includes weapons, tools, shields and armor. Simply Swords, Simply More and Epic Knights are detected by registry namespace without linking their classes. Each integration has a server configuration switch. Wooden/stone gear, leather and animal armor are excluded by default. Datapack inclusion and exclusion tags and deterministic predicates allow other authors to extend support.

Shaped and shapeless equipment recipes with metal, diamond or supported gear inputs are routed to the standalone shaping type at reload. Original item IDs, authored patterns, ingredients, result components and load conditions remain authoritative. Epic Knights equipment requires a reusable matching blueprint. A modpack that removes a recipe must explicitly restore it if it wants that pattern available; Temper & Tang does not undo recipe blacklists.

Results preserve names, enchantments, damage, repair costs and foreign custom data. Temper & Tang rebuilds only its own attribute modifiers from the original durability baseline. Repeated processing replaces earlier bonuses rather than stacking them. Netherite upgrades rebase onto the upgraded item's original stats while retaining workmanship and finishing. Excessively worn gear is refused instead of silently repaired.

Third-party code that calculates damage outside the normal item attributes may need a separate integration. Projectile damage is not automatically amplified. Shared ingredient tags permit compatible inputs; they do not merge existing stacks or enforce a pack's canonical output. Almost Unified remains responsible for that policy.

Owned tongs use additive `c:tools/tongs` and `temperandtang:tongs` tags. Compatible hammer tools use the common hammer tag and `smithing_hammers`. Existing optional tag entries do not create a runtime dependency on their owner.

See [the workshop guide](THERMAL_WORKSHOP.md), [API](API.md) and [verification record](VERIFICATION.md).

References inspected during development include [Overgeared](https://github.com/phuccom000/Overgeared), installed Simply Swords and Epic Knights recipes, and the [Diegesis API](https://github.com/Pocky-l/diegesis). Integrations are independently implemented; third-party artwork is not redistributed.


Version 0.4.0 checks every available routed pattern through actual output forging, quenching, a compatible finish and serialization. The NeoForge fixture also exercised Simply More from a separate optional test copy. Its disabled state in the live profile is preserved. Vanilla metal hoes are included in routing rather than bypassing the workshop because of low attack damage.

Material/trait strengths are saved with forged items. Finishing and ordinary netherite upgrades preserve those records; the latter refreshes the material and the raw stat baseline. Default guidance is short, H shows station help and Shift reveals equipment details. No compatibility path enables another mod's loot or implicit abilities, or restores a recipe removed by the pack.
