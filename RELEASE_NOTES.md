# Temper & Tang 0.4.2

A standalone smithing progression revision for Minecraft 1.21.1 Fabric and NeoForge.

The default station display is deliberately short: your stage, next action and required ingredients. Stages use warm gold, quantities use muted cyan, and benefits or penalties use soft green or red. Press **H** for extra station help and hold **Shift** over forged equipment for detailed stats and maker information.

Material choice now adds small strengths and trade-offs beneath craftsmanship. A completed forge can roll one equipment-appropriate trait. Strike accuracy improves the odds; finishing keeps the trait, while paid reforging replaces it. Existing unstamped equipment is unchanged.

Metal blanks now develop into the appropriate weapon, tool, shield or armor form. Workstation rests and clips are more readable, while the simpler hammer and existing strike, bellows and dipping animations remain in place. Metal hoes and other low-damage tiered tools now use the same equipment progression rules.

Forging now uses readable rhythm phrases: a pulsing fixed green target and occasional double or triple strike markers moving along one shared track. Harder metals require faster, more precise strikes. Future beats stay fixed when a strike is early or late. Hammer contact triggers a warm surface flash, clang and sparks, with a stronger spark burst on perfect strikes. Material difficulty and rhythm phrases can be disabled separately.

Bellows compression now sends a visible air stream from the nozzle toward the coals, with a brief flame and ember response.

Requires Diegesis beta.17, Fzzy Config and the loader's Kotlin runtime. Simply Swords, Simply More and Epic Knights remain optional. Existing recipes removed by a pack are not recreated. No food, world-generation, travel, loot or combat configuration is changed by installation.

Distinct bellows puffs now use a stable texture, a softer fade-in and retained fractional emission time. Their compression rhythm remains separate bursts. Initial stock gathers onto the anvil face before shaping rather than staying spread across the pattern grid.
