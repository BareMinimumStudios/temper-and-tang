# Workshop artwork

The owner-supplied Blossom Studios Platinum Set v1.02 is the source of tin ore, deepslate ore, raw metal, blocks, ingot and nugget art and the bronze/steel tool and armor families. Source pixel silhouettes, rock, timber, leather and armor layout are preserved; metal ramps distinguish neutral-silver tin, warm bronze and darker cool steel. Tin equipment is not registered.

Run `scripts/adapt_platinum_textures.py` with the original ZIP and Pillow available to reproduce those adaptations. The source archive remains outside the repository. Flux, finishing materials, bronze blend, blueprints, upgrade template and unfinished-workpiece icons have dedicated transparent 16Ã—16 sprites. The supplied Workshop Six tool and furniture models remain in use.


See THIRD_PARTY_NOTICES.txt for credits and the separation of artwork rights from the code license.

The compact red-topped `blacksmith_v2_furnace` is the regular Alloy Forge. The Nether Alloy Forge uses the same geometry. The Nether variant darkens neutral masonry pixels toward muted charcoal and Nether-brick tones while preserving warm timber and animated flame colors. The supplied animated frame sequences are retained with atlas interpolation.

Shaping colors use `models/diegesis/materials/<material>/<form>` aliases. Each inherits its original geometry and only overrides the metal, face and particle texture; wood and leather remain inherited. `scripts/generate_workpiece_palettes.py` reproduces the metal swatches without extra dependencies. Resource packs can override `textures/block/workpiece/<material>.png`. These are visual assets, not equipment registrations or recipes: equipment eligibility remains recipe-controlled, and diamond/netherite acquisition stays at the smithing table.


Finishing-material and flux icons remain their original native 16x16 PNGs, with binary alpha and no interior transparency holes. Their item models now use explicit front/back texture planes and texel-aligned boundary faces rather than the automatic generated-item extrusion. Boundary UVs sample opaque texel centers. `scripts/generate_material_edges.py` rebuilds these five meshes deterministically with Pillow; it does not resize or repaint the images.

The tin equipment set uses the original Platinum set pixel colors. Bronze retains the warm recolor. Steel uses a slightly darker blue-gray ramp. Silver resources use their own cool blue-silver ramp; no separate silver equipment models are duplicated when the equipment is provided by Epic Knights. The Nether Alloy Forge shares the regular forge geometry and UVs. Its muted shell texture is shared across the placed block, inventory item, and focused view. The animated fire uses a separate texture. World station models use cutout rendering to preserve transparent grate and bellows texture regions.
