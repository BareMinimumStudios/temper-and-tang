# Design

Temper & Tang is a standalone medieval smithing mod. Player skill, material choice and deliberate finishing matter in its workshop. Libraries provide configuration and world-space interaction; another smithing mod is never required.

The owned workflow is refined stock → charcoal heating → patterned shaping → quenching → optional finishing. Smithing Tongs safely carry hot stock in either hand. Contact burns and tool wear are bounded per player, and cooling cannot turn an unfinished workpiece into a usable weapon.

Current layers are weapon pattern, material and finish. Names, damage, enchantments and foreign components must survive processing. Standalone quality grading, maker marks, grinding and further material profiles are future work. External quality data may be preserved by an explicit legacy bridge, but must never define the default progression or be multiplied twice.

Shipped milestones include server-authoritative Diegesis workstations, original workstation/control geometry, tin and bronze, survival recipes, deterministic ghost patterns, readable hammer timing, owned handling tools and bounded finishes. All completion and consumption runs on the server.

Further alloy properties, runes, material-percentage traits, steel patterns and new weapon variants remain future work. Keep actions short and accessible. Prefer deliberate selectable patterns over weapon roulette. Recipes should remain useful with medieval wind, water and coal workshops rather than requiring advanced Create machinery.
