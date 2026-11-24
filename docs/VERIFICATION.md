# Verification

The current build targets Minecraft 1.21.1 on Fabric and NeoForge. Checks run in separate server/client copies; the live modpack profile is not modified.

## Automated checks

- Both loader builds and 30 rule/motion tests pass.
- Resource checks cover additive common tags, separate tin/silver categories, recipe counts and serializers, station bindings and the front-facing pattern grid.
- Every registered block has a blockstate and inventory model. The model audit checks 310 model files, parent/texture links, UV bounds, geometry and scene references.
- Distribution checks inspect the actual JARs, dependency declarations, recipe-viewer entrypoints, icon, public API and license packaging.

Run `scripts/build.ps1 -Clean` or `scripts/build.sh` for the reproducible local checks.

## Dedicated servers

Both loaders pass the equipment and progression diagnostics. The nine tin equipment recipes resolve to shaping, retain their material/quality on save, and preserve the bronze mining gate. Silver ore/raw items resolve to silver smelting/refining; iron-tier mining and common silver ingredients are checked. With the supported equipment integration present, 28 native silver patterns resolve and require their matching blueprint.

The timed workshop diagnostic runs eleven batches in parallel: normal heating, raw iron/tin/silver and silver ore refining, ordinary bronze/steel alloys, and advanced netherite alloying. It checks accepted slot inputs, exact material consumption, whole fuel use, carbon alternatives, lava bucket return, cancellation and a single output claim. Expanded stations are checked in all four facings for anchoring, safe occupancy, collision, one drop and no orphaned parts.

Separate standalone checks on both loaders pass with only the required libraries. The optional equipment test sets are restored after those runs.

## In-game checks

The hidden client captures its own framebuffer and silent game-audio file. It does not record or control the desktop.

- The starter route mines tin and copper with a stone pickaxe, refines both, alloys bronze, shapes a sword, carries the real unquenched workpiece, quenches it and applies a finish.
- The silver route mines ore with an iron pickaxe, refines it, scribes a matching reusable blueprint, forges the native shortsword, shows its workpiece, quenches it and finishes it. The blueprint remains after forging.
- The steel route checks the ordinary forge, actual timed shaping, quenching, finishing, structure-template diamond upgrade, advanced netherite alloy and the final upgrade. Quality and finishing survive both upgrades.
- Additional recordings exercise tin/bronze/steel/iron/gold shaping colors and all four finishing treatments. Finishing preserves craftsmanship and produces only one result.
- The inventory catalog renders tin, bronze and steel armor on the player, swords and all four tools, current resources and workshop supplies. It verifies every owned item has a common or workshop sorting tag.
- World/focused comparisons cover all six stations. Transparent furnace grates and bellows use cutout world rendering. Both forges share geometry and scale, with the advanced version tinted consistently and fire left separate.

## What these checks cover

Server diagnostics cover recipe handling, persistence and the tested station interactions. Client captures cover the visible workflow and current models. They do not certify arbitrary combat overhauls, custom resource packs, unusual network loads or every possible third-party item implementation. Foreign special abilities and projectiles remain controlled by their owning systems.

Operator diagnostics include `/temperandtang verify`, `verify_progression`, `verify_workshops`, `verify_station` and `verify_thermal`. They are intended for disposable verification worlds.
