# Workshop Six blacksmith assets

The project owner supplied the Blacksmith Furniture pack and confirmed redistribution permission on November 24, 2026. Artwork remains credited to Workshop Six and is excluded from the project code license. See THIRD_PARTY_NOTICES.txt.

The source pack uses ordinary Java item model JSON and PNG textures. Nexo is not required. Its server furniture YAML, item-display placement transforms and purchase/support files are not bundled.

This integration uses the supplied hearth with its coal bed aligned to the existing heated-stock and particle positions, and the anvil artwork for both functional anvils, scaled to keep their existing working surfaces and impact animations. It splits the supplied bellows into its fixed stand/nozzle, rotating upper board/handle and flexible textured leather. Air bursts and fire feedback retain the existing compression rhythm. The supplied hammer and tongs share their artwork with the inventory and animated versions. Their grip, striking face and jaw contact coordinates are calibrated to the workpieces. Mine Furniture provides the textured pallet stand and matching timber surfaces.

The remaining decorative furniture is not registered as new content. Large forge, table and rack models have multi-block proportions and need deliberate footprints and interaction layouts before replacing functional stations. The supplied water barrel now replaces the trough artwork, with its own rim height, water surface and dipping trajectory. The supplied furnace is uniformly fitted for the separate late-game alloy furnace.

## Pattern selection

Press **F** while preparing a shaping pattern to search by equipment name or recipe identifier, including its mod namespace. Click a result to select it. Escape closes the picker without leaving the station. The server accepts only current recipes while idle; the picker cannot change a committed workpiece or active forging job.

Placing a complete ingredient arrangement selects a matching pattern when the current pattern does not fit. An already matching choice is preserved, including cases where multiple mods provide similar patterns. Cooling still runs normally; there is no unlimited heat pause or client-only recipe authority. Choose the pattern before heating when preparing a new equipment type.

All four station cameras are centered on the work surface rather than offset sideways. Diegesis beta.19 is the build reference for this revision.

## Restricted ingredient slots

The hearth additive dish accepts only `temperandtang:alloy_additives`: common tin/gold ingots, coal and charcoal by default. Datapacks adding a new alloy's secondary ingredient should extend that tag as well as adding the alloy recipe. Accepted additives still need a matching primary ingredient and counted recipe before the hearth can run.

Finishing treatments remain limited to the four `temperandtang:finishing_materials/*` tags. Fuel accepts coal/charcoal, the hammer slots accept supported hammers, flux accepts smithing flux, blueprints accept blueprint items, and the quench water slot accepts water/empty buckets. General shaping grid cells remain recipe ingredient cells so sticks, leather, metal and compatibility ingredients can be arranged normally.

## Presentation polish

The entire hearth and bellows fit inside a single block footprint; its focused scene uses the same scale. Collision outlines follow the adapted geometry in all four horizontal orientations. Anvil contact heights are unchanged. The thin pattern board clears the anvil surface and its ghost pattern also follows the higher vanilla anvil surface.

The rhythm display carries upcoming strikes forward when a phrase advances, using the server target tick as the beat identity. Clock corrections are bounded so network updates cannot reverse the moving marker. The server still grades strikes and rolls quality. Quenching uses smooth acceleration, gradual jaw closure, and a tong-jaw contact point calibrated from the supplied model. Air bursts follow actual bellows compression.

The front-facing anvil camera is retained while the hammer swings in from the player's main-hand side. A yawed parent pose keeps the normal striking motion and collider aligned. Left- and right-handed poses land on the same workpiece contact point; the original forward pose remains available for older scene overrides without the side-pose node.

The hammer is rolled a quarter turn around its handle so its small 4×4 end face strikes downward. Its broad 6×4 cheek remains vertical. The head contact offset and interaction bounds follow this orientation.

All 107 shipped model resources pass parent/texture, UV, bounds, rotation and display-scale validation. The converted tongs retain uniform thickness; the original hammer texture coordinates are matched at transformed vertices. Model-gallery inspection remains part of release verification.
