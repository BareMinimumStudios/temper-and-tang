# Metals and equipment

The default survival order is **copper + tin â†’ bronze â†’ iron â†’ steel â†’ diamond â†’ netherite**. Tin also supplies a soft equipment alternative below bronze. Silver is an optional iron-mining-tier side material used by compatible equipment mods, not a mandatory step in the main ladder. See [the workshop guide](THERMAL_WORKSHOP.md) for controls, heating, timing, quenching and finishing.

## Getting started

Use a stone pickaxe to mine copper and naturally generated tin in new Overworld chunks. Smelt their raw forms in a normal furnace. You can combine three copper ingots and one tin ingot into four Bronze Blend, then smelt the blend into four bronze ingots. This cold-metal route lets you make your first tongs, hammer and shaping anvil without needing iron or handling hot stock unprotected.

The workshop route puts three copper ingots in an Alloy Furnace's stock slot and one tin ingot in its additive slot, with separate fuel, to produce four bronze ingots. The Charcoal Hearth needs seven cobblestone and one charcoal; it refines smeltable ore and raw metal, and reheats ingots and equipment. Alloys belong in the alloy furnaces. Craft bronze tongs, the bronze smithing hammer and the bronze-based shaping anvil; prepare a pattern before heating stock.

A bronze pickaxe can mine iron. Stone cannot mine iron under the default progression; bronze cannot mine diamond. Shape ordinary iron equipment from heated iron. To produce steel, alloy iron with coal or charcoal; the additive and the fuel are separate costs.

## Alloys

| Process | Stock | Additive | Output | Default processing time |
| --- | --- | --- | --- | --- |
| Bronze | 3 copper ingots | 1 tin ingot | 4 bronze ingots | 10 seconds |
| Steel | 1 iron ingot | 1 coal or charcoal | 1 steel ingot | 12 seconds |
| Netherite (Nether Alloy Furnace only) | 4 netherite scraps | 4 gold ingots | 1 netherite ingot | 60 seconds |

Fuel is charged once when a batch starts. Common ingredient tags accept compatible materials without merging existing stacks or changing another mod's canonical output. Small batches still spend whole fuel items. An Alloy Furnace costs eight clay balls and one coal or charcoal. A Nether Alloy Furnace costs seven clay balls, one coal or charcoal and one netherite scrap. Both accept ordinary furnace fuels at their normal burn durations; fuel and the carbon ingredient for steel are separate. The Nether Alloy Furnace also runs every ordinary alloy recipe. The hearth occupies 2 Ã— 2 Ã— 1 blocks; both furnaces reserve 2 Ã— 2 Ã— 2 blocks, including their actual collision and interaction space.

## Equipment and blueprints

The standalone catalog includes tin, bronze and steel swords, tools and armor. Extra weapon types come only from installed optional equipment mods. Installed normal Simply Swords, Simply More and Epic Knights equipment keeps its original identity and authored ingredient pattern. Patterns removed by a modpack stay removed.

Epic Knights patterns require a matching reusable blueprint. Craft a Blank Blueprint, select a pattern, insert the blank and press **B** after reaching mastery level 1. Making basic equipment first raises mastery. Press **F** to find patterns directly. An inserted filled blueprint selects its stored pattern. It never replaces the required ingredients.

The steel heavy crossbow uses its original blueprint and recipe. Shaping works its metal prod; the finished item remains the native crossbow. Craftsmanship affects its durability, arrow power and loading speed, preserving Quick Charge and ammunition effects. Wooden longbows retain native crafting.

## Diamond and netherite

Find a **Diamond Upgrade Smithing Template** in structure chests, including desert pyramids and ancient cities. It has no crafting or duplication recipe. At a smithing table, combine the template, matching steel equipment and a diamond. Normal mod-added iron-family gear can retain its native item name when forged with steel; its material tooltip identifies the steel core needed for the diamond upgrade. Existing workmanship, finish, name, enchantments and wear carry over.

For netherite, alloy four scraps and four gold ingots in the **Nether Alloy Furnace**, with separate fuel. It directly produces one netherite ingot; there is no billet-refining minigame in this route. Existing legacy billets can be smelted into their paid-for ingot.

Upgrade matching diamond equipment at a smithing table with the normal **Netherite Upgrade Smithing Template** and the refined ingot. This route does not bypass the diamond base or structure templates. EMI and JEI show the alloys, steel-core and blueprint routes.

## Quality and finishing

Craftsmanship has five grades: **Rough, Serviceable, Fine, Superior and Masterwork**. Accuracy has the largest influence, with smaller random and mastery contributions. Poor equipment stays usable; masterwork is not guaranteed by perfect timing. Applicable bonuses affect durability, damage or protection, handling and mining speed. Material and trait trade-offs are described in [materials and traits](MATERIALS_AND_TRAITS.md).

Finishing is a smaller final treatment, not another craftsmanship roll. Tempered favors durability; Keen trades durability for damage; Weighted trades speed for damage; Lightened trades durability for speed or a small armor movement bonus. Enchantments can enrich the result afterward. Reforging spends flux and hammer wear and replaces the previous quality rather than stacking bonuses or repairing gear for free.


## Tin and silver

Tin supplies a sword, pickaxe, axe, shovel, hoe and four armor pieces. The original owner-supplied silver-white equipment textures are used without recoloring. Tin tools have 96 base uses and 3.5 mining speed; the pickaxe cannot harvest the iron-stage ores that require bronze or better. Tin gear follows the same heating, shaping, quenching and finishing system.

Silver supplies normal/deepslate ore, raw silver, ingots, nuggets and raw/refined storage blocks. Its cool blue-silver palette differs from original tin and the darker steel ramp. Silver ore generates in new Overworld chunks from Y -32 through 48, with four vein attempts per chunk and a configured vein size of five. It requires an iron-tier pickaxe. Existing chunks are not retroactively filled. Datapacks can override the configured and placed features; loader biome integration uses the same placed-feature ID.

Silver ore or raw silver can use an ordinary furnace/blast furnace. The charcoal hearth also refines them from their smelting recipes and returns hot ingots. Existing silver ingots can be reheated. Common `c:ingots/silver` inputs make Epic Knights' normal silver equipment recipes available at the shaping anvil. Its matching reusable blueprint is required, followed by shaping, quenching and optional finishing. The native equipment item and special behavior remain owned by Epic Knights. Silver's material record has no additional base-stat multiplier; craftsmanship and finishing still apply.

Blueprints are made from paper and black dye. Choose the pattern at the shaping anvil, insert a blank blueprint and press B at the required mastery level (default 1). A scribed blueprint can select its pattern and is not consumed by a successful forge. It is required for Epic Knights equipment, not ordinary standalone catalog patterns.

The legacy unrefined netherite alloy item remains only for older saves and its one-ingot recovery recipe. It is not part of the current netherite route or creative catalog. An unquenched workpiece is different: it is the actual intermediate returned by shaping and carries the intended equipment, heat and craftsmanship until quenching.
