# Temper & Tang 0.7.1

Shaping billets and blanks now use the selected metal’s ingot palette with a subtle heat tint; shared geometry, timber and leather details are preserved.

This presentation pass keeps the 0.7.0 survival recipes and equipment progression.

- Regular Alloy Forge: compact red-brick-topped supplied furnace; the red masonry stays unchanged.
- Nether Alloy Furnace: larger supplied hooded forge with animated logs, interior and fire, with subtly muted charcoal and Nether-brick masonry.
- The focused camera shows the stations at a useful working size. World and focused furnace bodies use matching geometry and texture references; coplanar front faces are separated.
- Fuel goes directly into the firebox. Its display hides while burning; unused fuel and container remainders stay available afterward. No extra fuel shelf or blaze-powder control.
- Hearth air starts at the bellows nozzle and follows the complete model transform toward the coals. Furnaces use the supplied fire animation without loose sparks floating outside.
- Recipe grids read top-to-bottom and left-to-right from the front camera. Completed items lie flat instead of bobbing. The finishing compound rests on a separate attached corner tray, clear of the timing track.
- The quenching tool lifts before crossing the rim and uses a steeper dip. A small iron rest supports the waiting workpiece.
- Craftsmanship labels use title case, including Superior Craftsmanship.

Supplied animated fire textures interpolate between their original frames. Required dependencies and configuration settings are unchanged. See THIRD_PARTY_NOTICES.txt for supplied artwork credits.

Common tool, melee-weapon, mining-tool, armor and enchantable tags cover both equipment families on both loaders. Workshop sorting tags cover stations, blueprints, compounds, finishing materials, workpieces and templates without changing slot restrictions or recipe eligibility.

Tooltip polish: craftsmanship progresses through muted gray, ivory, green, light blue and gold. Finish labels are capitalized, non-italic and grouped under craftsmanship. Only the mod-owned legacy finish lore is hidden from the rendered tooltip; saved components and foreign lore are preserved. Finishing-material/flux item models use closed texel-aligned edges while retaining their native 16px sprites.
