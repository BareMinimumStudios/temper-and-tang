# Temper & Tang

Standalone, material-driven medieval smithing for **Minecraft 1.21.1**, written in Kotlin for **Fabric and NeoForge**. A Bare Minimum Studios project.

The first revision uses **Diegesis charcoal heating, scored hammer work, water quenching and equipment finishing**. Place materials directly on physical workstations, work the metal, then retrieve the result. Its rules and item consumption run on the server. Fzzy Config supplies the configuration interface, validation and synchronization.

Press **F** at the shaping anvil to find a pattern directly. You can select while holding hot stock, and a valid saved pattern is restored when its station state reopens. The workshop cameras face the work directly.

## Current features

- Standalone heat → shape → quench → finish progression, including metal armor, tools, shields and weapons.
- Compact colored station guidance; **H** for help and **Shift** for equipment details.
- Recorded material trade-offs and one bounded equipment trait, influenced by accuracy and equipment family.
- Distinct intermediate metal blanks for weapons, tools, shields and armor.
- Five server-rolled craftsmanship grades, weighted toward strike accuracy, with a smaller random contribution and smithing mastery bonus.
- Player mastery, maker records, reusable advanced blueprints and paid hot-gear reforging with smithing flux.
- Optional Simply Swords, Simply More and Epic Knights support using their original item identities and authored recipes.
- Bronze-before-iron mining, structure-template steel-to-diamond upgrades, and a dedicated precision netherite alloy route.
- Optional native EMI and JEI workshop recipe categories, with the same synchronized server recipe graph.
- Tin ore, furnace refining and counted hearth alloys: 3 copper + 1 tin → 4 bronze; iron + carbon → steel.
- Bronze and steel swords, tools and armor; extra weapon types use installed equipment mods through optional compatibility.
- Owned Smithing Tongs, bounded contact burns and wear, automatic inventory/cursor cooling and unusable unquenched workpieces.
- Directional workstations, contextual slots, physical hammer controls, a changing timing gauge, tiny sparks, varied anvil sounds, water and embers.
- Tempered, Keen, Weighted and Lightened finishes. Precision controls the smaller finish bonus. Armor accepts Tempered or Lightened.
- Preserved names, enchantments, damage and foreign custom data, including netherite upgrades. Reprocessing replaces bonuses rather than stacking them or repairing gear for free.

## Playing

Install the correct loader JAR, Fzzy Config, Diegesis **beta.19** for your loader, and Fabric Language Kotlin or Kotlin for Forge. See `libraries.toml` for exact development dependency pins. Simply Swords, Simply More and Epic Knights are optional equipment integrations. Overgeared is not required or used. The workshop handles weapons, tools, shields and metal armor with server-owned craftsmanship, mastery and reusable blueprints.

See [metals and equipment](docs/METALLURGY.md) for the standalone catalog, bootstrap route and alloy ratios.

See [materials and traits](docs/MATERIALS_AND_TRAITS.md) for the equipment trade-offs.

Follow the [thermal workshop guide](docs/THERMAL_WORKSHOP.md) to heat refined stock, shape equipment, quench it, learn blueprints and reforge existing gear.

Craft the Finishing Anvil, hammer and a finishing material. Place one supported equipment item, the material and hammer in their physical slots. Keep the cursor empty, then click the working hammer or press **Space** as the timing marker crosses the central band. Timing varies between blows. The first strike starts the challenge; preparation time is free. Most finishes require six blows and two experience levels. The final accepted blow commits the result and costs once. Closing returns session-owned inputs and completed work.

The table shows full-precision finishing bonuses; lower precision reduces their strength. Craftsmanship is a separate, larger equipment-quality layer.

| Finish | Default benefit | Tradeoff |
| --- | --- | --- |
| Tempered | +15% maximum durability | Material, work and XP |
| Keen | +5% attack damage | -10% maximum durability |
| Weighted | +10% attack damage | -5% attack speed |
| Lightened | Weapons: +5% attack speed; armor: +1% movement per piece | -10% maximum durability |

The config is `config/temperandtang/config.json5`. Server settings govern results. Reload datapacks after changing recipe integration settings. These finishes do not enable Simply Swords implicits or add armor-piercing effects.

The finishing anvil is crafted with a vanilla anvil, two copper ingots and three planks. Craft the finishing materials in the normal grid; their tooltips list the effects. Both coal and charcoal work for tempering compound. Hold Smithing Tongs in either hand when moving heated stock. Shape and quench a workpiece before applying a finish.

![Finishing anvil in a tested survival session](docs/images/finishing-anvil.png)

## Development

Use Java 21 and the checked-in **Gradle 9.9.2** wrapper:

```powershell
./scripts/build.ps1 -Clean
```

```sh
scripts/build.sh
```

The build creates separate Fabric and NeoForge JARs. Verified files, checksums, license notices and release notes are staged in `build/release/`. Nothing is uploaded automatically. GitHub workflows build both targets and can prepare an explicitly requested draft release. Modrinth publishing can be configured when project pages exist.

## Scope

The 0.6.0 refining revision has completed its recorded acceptance checks. Live station sessions return items when closed rather than keeping persistent block inventories. Working heat uses world ticks. Workstation and tool artwork combines independently authored elements with licensed Workshop Six assets supplied by the project owner; adaptations and credits are listed in `THIRD_PARTY_NOTICES.txt` and the workshop asset guide. Several material icons reference vanilla textures. Tin generates only in new chunks. Both loaders have been exercised with real delayed network connections, interrupted sessions and clean server restarts; see the [revision acceptance record](docs/REVISION_060_ACCEPTANCE.md) for exact coverage and limits. Third-party custom combat abilities remain authoritative and are not rewritten by craftsmanship.

See [design](docs/DESIGN.md), [compatibility](docs/COMPATIBILITY.md), [development](DEVELOPMENT.md) and [release checklist](docs/RELEASING.md), [public API](docs/API.md) and [verification record](docs/VERIFICATION.md).

## License

The Bare Minimum License v1.0 applies to this project. See `LICENSE` and `THIRD_PARTY_NOTICES.txt`. External mods retain their own licenses and are not bundled.
