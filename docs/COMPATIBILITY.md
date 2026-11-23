# Compatibility

## Optional mods

Simply Swords and Simply More are detected through item registry namespaces; no classes from them are linked or copied. A supported weapon must be damageable and non-stackable. Per-mod toggles are available. Other SwordItem weapons are supported by default.

Standalone heating, shaping, quenching and hot-metal handling are provided by Temper & Tang. Overgeared is optional; its recipe bridge is disabled by default. The bronze sword and existing basic Simply Swords iron/gold shaped recipes are converted only when it is present and the config enables integration. Original pattern/result components and load conditions are preserved. Iron ingots become heated iron ingots; gold retains its native material. Existing Overgeared recipes and other addons' assembly recipes are not rewritten. Diamond/netherite upgrades and unique recipes remain their owners' responsibility.

The finishing result starts from a full copy of the input. It retains native quality data, enchantments, names and attributes. Only Temper & Tang's own modifier IDs are replaced. The raw maximum-durability baseline is stored, so Overgeared applies its dynamic quality multiplier once. Attribute tooltip visibility is preserved. The original baseline is stored to prevent compounded finish bonuses. A new finish that would leave an already damaged weapon beyond its maximum is refused instead of repairing it silently.

Overgeared's steel and copper smithing hammers can perform finishing, along with damageable members of the common hammer tag. Near-broken hammers are refused before charging anything. The finishing anvil exposes its Diegesis workflow rather than a vanilla repair/enchanting menu.

Pack-authored native forging recipes are preserved. New Test's existing gold recipes require quenching; their native rules were retained and all 15 iron, 15 gold and the bronze recipe matched and assembled after pack overrides. Baseline conversion uses heated iron and quenching for iron; gold and bronze conversion do not add a quenching requirement.

## Inspected references

- Overgeared source, `multiloader-1.21.1`: https://github.com/phuccom000/Overgeared
- Installed Overgeared 1.7.1.1 recipe data and serializer.
- OvergearedXSimplySwords NeoForge 1.1.0 reference JAR: inspected metadata, recipes and tool-tier bytecode; never installed or redistributed.
- Diegesis beta.17 API and upstream station documentation: https://github.com/Pocky-l/diegesis

These are independently implemented integrations. Simply Swords and its artwork remain under their separate license. The mod does not import Andromeda's global loot, travel or food restrictions into unrelated modpacks.

## Thermal stations (0.2.0)

See [thermal workshop](THERMAL_WORKSHOP.md). The optional native adapter was checked against Overgeared 1.7.1.1. Basic shaping imports only stone-tier, blueprint-free recipes without minigame-only/transfer-NBT/advanced quality requirements. Its quality range is Poorâ€“Well and follows native ingredient caps and tiered-hammer settings. Native polishing and author components are retained. All shaped outputs start hot; native air-cooling and water routes remain available.

Without Overgeared, the `temperandtang:shaping` recipe type delegates vanilla shaped matching/serialization while keeping basic weapons out of normal grid crafting. Its result becomes an encoded non-weapon workpiece until quenched. Existing native forging recipes, pack food tags/recipes, loot restrictions and Create progression are not rewritten by this feature.

## Owned hot-metal handling (0.2.1)

Smithing Tongs use `temperandtang:tongs`, which includes `c:tools/tongs`. They can be repaired with common iron ingots. The server processes exposure once per player, including the inventory cursor; held tools protect either hand. Burn/wear cooldowns are per player, so carrying several hot stacks does not multiply charges. Expired inventory/cursor heat is normalized even when contact damage is disabled.

Owned `hot_until` timestamps are authoritative without an external heated flag. Standalone patterns are used by default even when another smithing mod is present. The explicit legacy bridge continues to import native recipes/components only when enabled; native exposure is then delegated to avoid applying two handling systems to bridged stock.
