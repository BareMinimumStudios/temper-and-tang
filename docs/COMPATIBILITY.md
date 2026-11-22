# Compatibility

## Optional mods

Simply Swords and Simply More are detected through item registry namespaces; no classes from them are linked or copied. A supported weapon must be damageable and non-stackable. Per-mod toggles are available. Other SwordItem weapons are supported by default.

Overgeared is optional. Existing basic Simply Swords iron/gold shaped recipes are converted only when it is present and the config enables integration. Original pattern/result components and load conditions are preserved. Iron ingots become heated iron ingots; gold retains its native material. Existing Overgeared recipes and other addons' assembly recipes are not rewritten. Diamond/netherite upgrades and unique recipes remain their owners' responsibility.

The finishing result starts from a full copy of the input. It retains native quality data, enchantments, names and attributes. Only Temper & Tang's own modifier IDs are replaced. The raw maximum-durability baseline is stored, so Overgeared applies its dynamic quality multiplier once. Attribute tooltip visibility is preserved. The original baseline is stored to prevent compounded finish bonuses. A new finish that would leave an already damaged weapon beyond its maximum is refused instead of repairing it silently.

## Inspected references

- Overgeared source, `multiloader-1.21.1`: https://github.com/phuccom000/Overgeared
- Installed Overgeared 1.7.1.1 recipe data and serializer.
- OvergearedXSimplySwords NeoForge 1.1.0 reference JAR: inspected metadata, recipes and tool-tier bytecode; never installed or redistributed.
- Diegesis beta.17 API and upstream station documentation: https://github.com/Pocky-l/diegesis

These are independently implemented integrations. Simply Swords and its artwork remain under their separate license. The mod does not import Andromeda's global loot, travel or food restrictions into unrelated modpacks.
