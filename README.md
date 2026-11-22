# Temper & Tang

Material-driven medieval smithing for **Minecraft 1.21.1**, written in Kotlin for **Fabric and NeoForge**. A Bare Minimum Studios project.

The first playable implementation is a **Diegesis finishing anvil**: place a weapon, finishing material and smithing hammer directly on the workstation, work the piece, then take its finished result. Its rules and item consumption run on the server. Fzzy Config supplies the configuration interface, validation and synchronization.

## Current features

- Tin ore generation in new Overworld chunks, raw tin and smelting.
- Bronze blend: three copper ingots plus one tin ingot makes four blend; smelting each blend produces one bronze ingot.
- A bronze-repaired sword, common material/tool tags, and a reusable smithing hammer.
- An animated working hammer, metalworking sounds, readable progress captions and a compact inventory tray. Finishing materials describe their effects in tooltips.
- Four deliberate finishes: tempered, keen, weighted and lightened. One finish per weapon by default; optional replacement retains the original durability baseline and does not stack modifiers.
- Enchantments, names, existing attribute entries and other mod data survive finishing.
- Simply Swords and Simply More weapons work in the finishing station without requiring a separate compatibility mod. Both are optional dependencies.
- With Overgeared installed, the bronze sword and untouched basic Simply Swords iron/gold grid recipes are adapted to native forging. Existing forging/assembly recipes, upgrades and unique recipes are left alone.

## Playing

Install the correct loader JAR, Fzzy Config, Diegesis **beta.17** for your loader, and Fabric Language Kotlin or Kotlin for Forge. See `libraries.toml` for exact development dependency pins. Install Simply Swords, Simply More and Overgeared separately if desired.

Craft the Finishing Anvil, hammer and finishing material. Open the anvil, place a single damageable weapon, material and hammer in its world-space slots, then click the working hammer or press Space six times, waiting between blows. Keep the cursor empty. A caption shows the missing input, current strike count or finished output. Taking a valid result consumes one material, six hammer durability and two experience levels by default. Leaving the station returns its inputs through Diegesis; no ingredients are consumed merely to preview a result.

| Finish | Default benefit | Tradeoff |
| --- | --- | --- |
| Tempered | +15% maximum durability | Material, work and XP |
| Keen | +5% attack damage | -10% maximum durability |
| Weighted | +10% attack damage | -5% attack speed |
| Lightened | +5% attack speed | -10% maximum durability |

The config is `config/temperandtang/config.json5`. Server settings govern results. Reload datapacks after changing recipe integration settings. These finishes do not enable Simply Swords implicits or add armor-piercing effects.

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

This is an initial prototype, not the complete heating/shaping/quenching simulation. The first workstation performs finishing; material-driven forging and further Diegesis scenes are next. Current item/block models reference vanilla textures as clearly temporary visuals; no third-party artwork is extracted. Tin appears only in newly generated chunks.

See [design](docs/DESIGN.md), [compatibility](docs/COMPATIBILITY.md), [development](DEVELOPMENT.md) and [release checklist](docs/RELEASING.md), [public API](docs/API.md) and [verification record](docs/VERIFICATION.md).

## License

The Bare Minimum License v1.0 applies to this project. See `LICENSE` and `THIRD_PARTY_NOTICES.txt`. External mods retain their own licenses and are not bundled.
