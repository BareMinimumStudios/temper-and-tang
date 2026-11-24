# Revision 0.7.0 verification

Verified November 24, 2026 against the built Fabric and NeoForge 1.21.1 artifacts. Testing used separate clients and dedicated servers; New Test was not changed.

- 29 pure tests passed; both loader builds and artifact/resource checks passed.
- All 106 models baked and rendered in nine hidden-client gallery pages. Inventory textures and both custom worn armor layers were visually inspected.
- Both dedicated servers passed the gear, preservation and progression checks. Native compatibility fixtures covered 62 diamond upgrades on NeoForge and 52 on Fabric, according to the different installed optional-mod sets.
- Both loaders completed seven timed station batches: hearth heating; ordinary bronze and steel; Nether furnace bronze, steel and direct netherite. Carbon accepts coal and charcoal independently of fuel. Fuel coverage included planks, sticks, coal blocks, blaze rods and lava buckets; empty buckets were preserved.
- Checked exact input use, repeated-start fuel protection, single output claim, cancellation, and rejection of advanced netherite in an ordinary furnace.
- Checked three expanded stations in all four facings: owned occupied cells, collision clipped to each cell, shared root anchoring, exactly one drop and no orphaned parts. Legacy roots reserve clear space and reject solid neighbors without overwriting them.
- A normal hidden client opened stations by clicking their non-root parts, heated iron, alloyed bronze and directly alloyed netherite, retrieved each output once, and verified the lava bucket return. Bronze and steel armor rendered with the supplied-set adapted layers.
- The nine standalone dagger/spear/warhammer registrations, recipes, models, combat profiles and tag members are absent. Tin has resources but no equipment tier. Optional weapons retain their owning mod's identity.

The old netherite billet route is not required for new metal. Legacy billets retain a one-ingot smelting recovery recipe; they are not offered in the creative catalog. Prototype saves containing the removed fallback weapons need a backup before updating.

The verification helper and staged test-world supplies are not included in the release JARs. The finished texture and station previews are available locally under build/previews/0.7.0.
