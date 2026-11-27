<div align="center">

<img src="docs/branding/banner.png" alt="Temper &amp; Tang" width="100%">

![Minecraft 1.21.1](https://img.shields.io/badge/MINECRAFT-1.21.1-D7B16A?style=for-the-badge&labelColor=252322)
![Fabric and NeoForge](https://img.shields.io/badge/LOADERS-Fabric_%2B_NeoForge-91B7BC?style=for-the-badge&labelColor=252322)
[![License: BML 1.0](https://img.shields.io/badge/LICENSE-BML_1.0-D7B16A?style=for-the-badge&labelColor=252322)](LICENSE)

Minecraft 1.21.1 · Fabric + NeoForge · Java 21 · Client & Server

[Getting started](docs/THERMAL_WORKSHOP.md) · [Metals and progression](docs/METALLURGY.md) · [Configuration](docs/CONFIGURATION.md) · [Report an issue](https://github.com/BareMinimumStudios/temper-and-tang/issues)

</div>

## A place at the anvil

Temper & Tang brings the work of a blacksmith into the world. Stoke the hearth, lay hot metal on the anvil, strike to the rhythm, then lower the shaped workpiece into water. Finish the cooled equipment to suit the way you use it.

A good weapon begins with good work. Strike accuracy, material and smithing experience influence its craftsmanship. Luck still plays a part, but a masterwork takes practice. Rough equipment remains usable, and enchantments can add something extra afterward.

For **Minecraft 1.21.1**, on **Fabric and NeoForge**. Install it on the server and every client.

**Current revision: 0.7.1, in playtesting.** See the [playtest guide](docs/PLAYTESTING.md) for what to try and the [release notes](RELEASE_NOTES.md) for this revision.

## Heat, shape, quench, finish

### Stoke the hearth

Refine raw metal or bring ingots back to working heat. The bellows feed the coals as the stock warms. Hold tongs in either hand when moving hot metal.

### Make each strike count

Find a pattern with **F**, check its ingredients, and lay them out on the anvil. Keep your cursor empty, then press **Space** as the marker reaches the green band. Tougher materials have tighter timing and can ask for several strikes in a phrase.

### Keep the pattern

Some advanced equipment needs a matching blueprint. Choose the pattern, insert a blank blueprint and press **B** once you have the required mastery. The finished blueprint is reusable.

### Take the workpiece to water

The result of shaping is an **Unquenched Workpiece**, not usable equipment. It carries the equipment you are making and the quality you earned. Take it with tongs, add water to the barrel, and quench it. If it cools before you get there, reheat it at the hearth.

### Give it a finish

Bring the cooled equipment, hammer and a finishing material to the finishing anvil. Choose durability, a sharper edge, heavier blows or quicker handling. Most finishes cost two experience levels. Better timing strengthens the treatment; a new finish replaces the old one.

## Choose your metal

Tin, bronze and steel each have a sword, four tools and a full armor set. Tin keeps a pale silver-white finish, bronze has warm coppery tones, and steel is darker and stronger.

Silver has its own ore, raw metal, ingots, nuggets and storage blocks. Its cool color stands apart from tin. Common material tags let compatible equipment recipes use it directly.

The main route is **copper + tin → bronze → iron → steel → diamond → netherite**. Alloy copper and tin for bronze, then iron and coal or charcoal for steel. Diamond gear comes from upgrading steel at a smithing table with a structure-found template. The Nether Alloy Forge combines gold ingots and netherite scraps into netherite and can also run ordinary alloys.

Tin veins start from Y 20 to 80 and peak near Y 50. Silver veins start from Y −16 to 30 and are most common around Y −1 to 15. Both appear in new Overworld chunks and have separate World Generation toggles. See the [metallurgy guide](docs/METALLURGY.md) for recipes, mining requirements and the full route.

## Five grades of craftsmanship

**Rough · Serviceable · Fine · Superior · Masterwork**

Quality affects durability and the stats that matter to the equipment: damage and handling for weapons, protection for armor, mining speed for tools. The tooltip keeps the grade, finish, material and trait together. Hold **Shift** for the details and maker's mark.

## Forge your armor

Helmets, chestplates, leggings and boots use the same heating, shaping, quenching and finishing loop. Armor patterns take more strikes than a basic sword. Craftsmanship changes durability and protection, with toughness bonuses where the base armor has toughness. Material and traits can add movement trade-offs.

Armor accepts **Tempered** for more durability or **Lightened** for quicker movement at a durability cost. Keen and Weighted are weapon finishes. Existing unstamped armor stays unchanged until you process it.

Supported Epic Knights armor uses its native item and a matching reusable blueprint. Ordinary supported Simply Swords and Simply More weapons use their native patterns; unique and legendary weapons are excluded. These are optional integrations: the workshop has its own tin, bronze and steel equipment and does not require another smithing mod.

Supported steel-to-diamond and diamond-to-netherite upgrades carry workmanship and finishing forward. Enchantments remain an extra enhancement. See [compatibility](docs/COMPATIBILITY.md) for the supported scope.

## Install

Use Java 21 and the build for your loader. Both sides need Temper & Tang and its required libraries:

| Both loaders | Fabric also needs | NeoForge also needs |
| --- | --- | --- |
| Diegesis 1.0.0-beta.19 and a matching 1.21.1 build of Fzzy Config | Fabric API and Fabric Language Kotlin | Kotlin for Forge |

Required libraries are installed separately. EMI and JEI show workshop recipes, blueprints and smithing upgrades, including animated refining and alloying previews. Equipment integrations are optional. Check the [installation guide](docs/INSTALLATION.md) for the tested versions.

## Make it fit your pack

Tune timing, mastery, quality, finishing and hot-metal handling in `config/temperandtang/config.json5`. Recipe costs and accepted materials can be changed with datapacks. Common `c:` tags cover metals, ores, tools and armor; workshop tags cover blueprints, finishing materials and stations.

[Configuration](docs/CONFIGURATION.md) · [Datapack and API guide](docs/API.md) · [Compatibility](docs/COMPATIBILITY.md)

## Project

[Modrinth](https://modrinth.com/project/temper-and-tang) · [CurseForge](https://www.curseforge.com/minecraft/mc-mods/temper-tang) · [Discord community](https://discord.gg/pcRw79hwey) · [Ko-fi](https://ko-fi.com/pokesmells)

[Source](https://github.com/BareMinimumStudios/temper-and-tang) · [Issues](https://github.com/BareMinimumStudios/temper-and-tang/issues) · [Documentation](docs/README.md) · [Development](DEVELOPMENT.md) · [Changelog](CHANGELOG.md)

[Modrinth page copy](docs/MODRINTH.md) · [CurseForge page copy](docs/CURSEFORGE.md)

The project uses the [Bare Minimum License](LICENSE). Supplied artwork is credited in [Third-party notices](THIRD_PARTY_NOTICES.txt).
