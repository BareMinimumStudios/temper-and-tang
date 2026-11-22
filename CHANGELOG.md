# Changelog

## 0.1.2

- Replaced the red control with a working hammer, swing animation, metallic strikes and a completion sound.
- Added a wood-and-copper finishing anvil model with matching collision and no vanilla repair menu.
- Added a compact inventory tray, material effect tooltips, worn-tool guidance and XP feedback.
- Accepted native Overgeared hammers and common hammer tags.
- Routed the bronze sword through native Overgeared forging when enabled.
- Expanded common crafting ingredient tags and preserved existing pack recipe overrides.
- Added recipe matching/assembly and station cancellation/restriction diagnostics.
- Verified packaged NeoForge client interaction, item preservation, native quality, save/reload, cancellation and restrictions; both loader servers passed with and without optional integration mods.

## 0.1.1

- Enlarged the finishing strike control and gave it an explicit hitbox.
- Added a Space-key strike route with the same server validation as button clicks.
- Added persistent workstation instructions, synchronized strike progress and output guidance.
- Added an operator-only, isolated station-action verification command.

## 0.1.0

- Established the studio Kotlin/Cloche cross-loader layout and BML license.
- Pinned Gradle 9.9.2 with a verified distribution checksum.
- Added Diegesis finishing-anvil logic and scene, validated Fzzy Config settings and survival material content.
- Added optional weapon integration, bounded non-stacking finish rules and server tick-based work validation.
- Added CI, local build scripts, artifact verification and opt-in draft-release staging.
