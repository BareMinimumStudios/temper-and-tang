# Changelog

## 0.5.1

- Add searchable pattern selection with F, automatic layout matching, and restoration of a valid saved pattern.
- Allow pattern selection while holding hot stock; prevent mouse-release clicks from activating nearby stations.
- Center the four workstation cameras and align station collision geometry in every horizontal orientation.
- Adapt the supplied Workshop Six hearth, anvils, hammer, tongs and animated bellows; fit the hearth and bellows inside one block.
- Add the Mine Furniture pallet support and matching timber surfaces, with retained creator credit.
- Smooth quenching acceleration and jaw closure, calibrate tool contacts, and synchronize air bursts to actual compression.
- Keep rhythm notes continuous across phrase changes and bound clock corrections so the marker cannot reverse under jitter.
- Reuse bellows leather meshes to reduce allocation during animation.
- Restrict alloy additives to an extendable ingredient tag; retain server-enforced finishing material restrictions.
- Build against Diegesis beta.19 on Fabric and NeoForge.

## 0.5.0

- Added standalone bronze/steel tools and armor, plus bronze/iron/steel daggers, spears and warhammers (27 owned equipment items total).
- Added counted, data-driven hearth refining and alloying with a separate additive dish, concise yield/fuel guidance and exact input consumption.
- Added steel ingots, nuggets and storage blocks; retained the furnace bronze bootstrap and added bronze tongs crafting.
- Added common tags, vanilla armor trims, appropriate creative tabs and optional Better Combat profiles.
- Bridged only Epic Knights' unchanged fast steel shortcut into carbon alloying. Removed/custom recipes remain authoritative.
- Preserved native gilding material records and external upgrade components, raw durability adjustments and foreign affixes without quality stacking.
- Added codec, batch, survival-route, cancellation, preservation and actual workstation client checks on both loaders.

## 0.4.2

- Preserved separate bellows compression puffs while smoothing their timing, texture, fade-in and the coals' brightness response.
- Gathered heated ingredients onto the anvil face before the first strike, then retained the existing shaping sequence.
- Recorded accurate bronze, accurate expert diamond triples, missed-note recovery and deliberately poor forging, using actual survival actions and unchanged random quality rolls.

## 0.4.1

- Replaced separate phrase lights with incoming gold strike markers on the same timing rail. Every note crosses one fixed green target; upcoming notes keep their spacing as the active strike changes.
- Made the perfect band match its actual time window on a shared track scale. Added full-bright target and note visuals for readability.
- Added visible, directed air streaks at the bellows nozzle, timed to compression, plus a brief flame and ember response.
- Kept quality based on accuracy: doubles and triples do not grant a separate rarity bonus. Missed notes reduce accuracy as before.

## 0.4.0

- Added compact stage, action and ingredient guidance, with muted color cues, H for optional help and Shift for detailed equipment tooltips.
- Added recorded material trade-offs, with additive common tags and neutral behavior for unrecognized materials.
- Added one accuracy-influenced, server-rolled equipment trait; material and equipment family influence its pool. Finishing preserves traits and paid reforging replaces them.
- Added distinct intermediate blanks for tools, polearms, shields and all armor slots, scaled to each recipe's required blow count.
- Added practical tool rests and blueprint clips without retiming the hammer, bellows or quenching motions.
- Closed the metal-tool filter gap, including low-damage hoes. Verified complete cross-mod pattern routes rather than only their recipe matches.
- Preserved raw durability baselines, wear, names, enchantments, foreign data, material/trait strengths and netherite upgrade behavior.


## 0.3.2

- Added fitted bench planks, joinery pegs, foot bindings and anvil holdfasts.
- Recessed the anvil tooling holes while preserving both striking surfaces.
- Wrapped the finishing stump with continuous iron bands and added fine end-grain checks.
- Added soot lining, an ash-pan pull, nozzle collar and braced bellows support to the hearth.
- Added separate trough staves, band rivets and a timber drain plug.
- Detailed the bellows with lid tacks, handle fittings and leather welts that follow the existing folds.
- Preserved the simpler hammer, strike/dip motions, timing, sounds, particles, recipes and configuration.


## 0.3.1

- Matched the inventory and animated hammer, with exact workpiece contact, metallic impact sounds and visible sparks on both anvils.
- Shaping now progresses from ingredients through a billet and blade blank; completed equipment appears after the final blow.
- Added folding leather bellows with a hinged lid, handle, airflow and responsive embers. Corrected overhanging model UVs.
- Tongs grip and dip the workpiece below the water, with steam and a hiss at immersion.
- Refined workstation frames, metal bands and fasteners; separated timing gauge layers and smoothed motion between server updates.
- Kept changing progress captions fully visible instead of restarting their fade on every strike.
- Preserved recipes, progression, costs and existing configuration settings.




## 0.3.0 — Craftsmanship



- Standalone shaping for supported shaped and shapeless equipment recipes, including metal armor, tools, shields, Simply Swords and Epic Knights.

- Variable hammer timing with scored accuracy, missed-beat penalties, latency tolerance and server-owned random quality rolls.

- Six craftsmanship grades, maker and accuracy records, player smithing mastery and reusable advanced blueprints.

- Heated gear reforging with consumable flux, bounded mastery gains and safeguards against free repair or repeated bonuses.

- Precision finishing complements craftsmanship; names, enchantments, foreign data and damage survive the workflow and netherite upgrades.

- Directional workstations, contextual slots, timing gauges, owned tongs animation, water, embers, tiny strike sparks and varied anvil sounds.

- Removed the legacy external smithing recipe bridge. No Overgeared installation is required.



## 0.2.1



- Added owned, craftable and iron-repairable Smithing Tongs with additive common tool tags.

- Added standalone server-side contact burns, bounded tongs wear, cursor protection and inventory cooling.

- Made owned heat timestamps independent of external heated components and disabled the legacy Overgeared recipe bridge by default.

- Added handling configuration, tooltips, isolated in-game checks and cooldown tests.

- Updated the workshop guide around a complete standalone workflow.



## 0.2.0



- Added Diegesis charcoal hearth, shaping anvil and quenching trough with original block/control geometry.

- Added fuel-paid batch heating, working-heat tooltips, ghost patterns, rhythmic hammer work, native quality limits and water-paid quenching.

- Integrated supported stone-tier Overgeared basic recipes without replacing blueprint or advanced forging.

- Added standalone shaping recipes and encoded unquenched workpieces to preserve the quenching progression.

- Preserved native heat/cooling, names, enchantments, damage, quality, batch counts and foreign data.

- Committed results on the final strike, guarded repeated actions, and kept cancellation costs explicit.

- Added thermal operator diagnostics, timing tests and a survival workshop guide.



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

### Rhythm forging

- Material-dependent tempo and precision, fixed-beat double/triple phrases, pulsing targets and note lights.
- Contact-synchronized warm flashes, stronger perfect sparks and faster hammer response.
- Independent difficulty and rhythm configuration switches; ingredient and preservation rules retained.
