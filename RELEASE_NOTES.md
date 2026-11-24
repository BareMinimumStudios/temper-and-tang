# Temper & Tang 0.7.1

The workshop now includes complete tin equipment, distinct silver resources, clearer pattern information and a full player guide. Both Minecraft 1.21.1 loader builds are included.

## Metals and equipment

- Tin has a sword, four tools and full armor in the original silver-white palette. It remains a softer alternative to bronze, with no shortcut through the mining tiers.
- Steel equipment has a slightly darker finish.
- Silver has normal and deepslate ore, raw metal, ingots, nuggets and storage blocks. Ore generates in new Overworld chunks and needs an iron-tier pickaxe.
- Common silver tags connect the material to supported equipment patterns. Native equipment retains its item identity and uses the regular blueprint, shaping, quenching and finishing workflow.
- The hearth accepts smeltable ores and raw metal as well as refined stock. Bronze and steel remain alloy-furnace recipes; netherite needs the advanced furnace.

## Workshop presentation

- Both alloy furnaces share the same compact red-topped geometry and full-size workshop footprint. The Nether version uses muted masonry and metal colors, with the animated fire left untinted.
- Fixed opaque black areas on world-rendered grates and bellows.
- Fuel sits in the firebox and disappears while burning. Unused fuel and container remainders are preserved.
- Shared shaping forms take their color from the selected material. Recipe grids read naturally from the front-facing camera.
- Pattern search shows ingredient summaries and blueprint requirements instead of raw recipe identifiers. Unchanged captions and ghost items are reused to reduce repeated updates.
- Craftsmanship has distinct tier colors. Finish labels are capitalized and non-italic; finishing-material models have closed pixel edges.

## Included documentation

The player guide covers the starter route, blueprints, quenching, finishing and smithing upgrades. Installation, configuration, datapack integration, screenshots, branding and draft Modrinth/CurseForge page copy are included.

Both loader builds pass 30 automated tests, resource validation, the 310-model audit and packaged-artifact checks. Separate server and client copies exercise refining, alloys, equipment progression, material colors, blueprints and quality preservation. See docs/VERIFICATION.md for scope and limitations.

Use Java 21 and install the required libraries separately; see docs/INSTALLATION.md. Existing configurations are retained. New ores appear only in newly generated chunks. Nothing is uploaded or installed automatically.
