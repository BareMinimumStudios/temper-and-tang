from pathlib import Path
import json
root = Path(__file__).resolve().parents[1] / "src/main/resources"
for path in root.rglob("*.json"):
    data = json.loads(path.read_text())
    if "/tags/" in path.as_posix():
        assert data.get("replace") is False, path
for registry in ("item", "block"):
    for tag in ("ores/tin", "storage_blocks/tin", "storage_blocks/bronze", "storage_blocks/raw_tin"):
        assert (root / f"data/c/tags/{registry}/{tag}.json").exists(), (registry, tag)
recipe = json.loads((root / "data/temperandtang/recipe/bronze_ingot.json").read_text())
assert [x["tag"] for x in recipe["ingredients"]] == ["c:ingots/copper"] * 3 + ["c:ingots/tin"]
assert recipe["result"] == {"id": "temperandtang:bronze_blend", "count": 4}
scene = json.loads((root / "assets/temperandtang/diegesis/scenes/finishing_anvil.json").read_text())
def node_ids(nodes):
    return {n["id"] for n in nodes} | set().union(*(node_ids(n.get("children", [])) for n in nodes))
assert node_ids(scene["nodes"]) >= {"weapon", "material", "hammer", "strike", "result", "timing_marker"}
print("Verified additive common tags, tag-based alloy inputs, conserved material counts and station scene bindings.")

for name in ("charcoal_hearth", "shaping_anvil", "quenching_trough"):
    scene = json.loads((root / f"assets/temperandtang/diegesis/scenes/{name}.json").read_text())
    assert (root / f"data/temperandtang/recipe/{name}.json").exists()
    assert (root / f"assets/temperandtang/models/block/{name}.json").exists()
    if name == "shaping_anvil":
        assert {n["slot"]["slot"] for n in scene["nodes"] if "slot" in n and n["id"].startswith("grid_")} == {f"grid[{i}]" for i in range(9)}
print("Verified thermal workstation recipes, models and all nine distinct grid bindings.")

assert json.loads((root / "data/c/tags/item/tools/tongs.json").read_text())["values"] == ["temperandtang:smithing_tongs"]
assert json.loads((root / "data/temperandtang/tags/item/tongs.json").read_text())["values"] == ["#c:tools/tongs"]
assert (root / "data/temperandtang/recipe/smithing_tongs.json").exists()
print("Verified owned tongs recipe and additive common tool tags.")

# Overhanging scene parts require explicit UVs to stay inside the texture atlas.
assets = root / "assets/temperandtang"
for path in (assets / "models").rglob("*.json"):
    model = json.loads(path.read_text(encoding="utf-8"))
    for element in model.get("elements", []):
        if any(v < 0 or v > 16 for v in element["from"] + element["to"]):
            assert all("uv" in face and all(0 <= v <= 16 for v in face["uv"]) for face in element["faces"].values()), path
assert json.loads((assets / "models/item/smithing_hammer.json").read_text())["parent"] == "temperandtang:diegesis/impact_hammer"
head = json.loads((assets / "models/diegesis/impact_hammer.json").read_text())["elements"][-1]
width, height, depth = [b - a for a, b in zip(head["from"], head["to"])]
assert width == depth == 4 and height == 6, "Hammer must strike with its narrow end, not its broad cheek"
assert head["from"] == [6, 2, 10.5] and head["to"] == [10, 8, 14.5], "Recalibrate the hammer contact offset when moving its head"
for sound in (assets / "sounds/forge").glob("*.ogg"):
    assert sound.read_bytes()[:4] == b"OggS", sound
print("Verified overhanging UV bounds, shared hammer geometry and encoded impact audio.")

# Captions compose these keys dynamically from the finishing state, so literal-key scans cannot find omissions.
import re
translations=json.loads((assets / "lang/en_us.json").read_text())
client=(root.parents[2] / "src/loaderShared/kotlin/com/bareminimumstudios/temperandtang/client/TemperClient.kt").read_text()
branch=client[client.index("val key = when"):client.index('for(id in listOf("timing_rail"')]
for status in set(re.findall(r'-> "([a-z_]+)"',branch)) - {"instructions"}:
    key="temperandtang.short.finish_"+status
    assert key in translations, f"Missing finishing caption: {key}"
print("Verified every dynamically composed finishing caption has a readable translation.")

shape=json.loads((assets/'diegesis/scenes/shaping_anvil.json').read_text())
for node in shape['nodes']:
    if node['id'].startswith('grid_'):
        index=int(node['id'][5:])
        assert node['pos'][0]==12-4*(index%3) and node['pos'][2]==12-4*(index//3),'Front-view recipe grid reversed'
print('Verified crafting-grid rows and columns follow the front-facing view.')

# Common tags must cover both equipment families on either loader, independently of loader defaults.
for metal in ('bronze', 'steel'):
    for tag, parts in {
        'tools': ['sword', 'pickaxe', 'axe', 'shovel', 'hoe'],
        'tools/melee_weapon': ['sword', 'axe'],
        'tools/mining_tool': ['pickaxe'],
        'armors': ['helmet', 'chestplate', 'leggings', 'boots'],
    }.items():
        values=json.loads((root/f'data/c/tags/item/{tag}.json').read_text())['values']
        assert all(f'temperandtang:{metal}_{part}' in values for part in parts), (metal,tag)
assert json.loads((root/'data/c/tags/block/storage_blocks/steel.json').read_text())['values']==['temperandtang:steel_block']
print('Verified both equipment families have common tool, armor and storage-block tags on both loaders.')
