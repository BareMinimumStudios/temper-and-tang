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
assert {n["id"] for n in scene["nodes"]} == {"weapon", "material", "hammer", "strike", "result"}
print("Verified additive common tags, tag-based alloy inputs, conserved material counts and station scene bindings.")

for name in ("charcoal_hearth", "shaping_anvil", "quenching_trough"):
    scene = json.loads((root / f"assets/temperandtang/diegesis/scenes/{name}.json").read_text())
    assert (root / f"data/temperandtang/recipe/{name}.json").exists()
    assert (root / f"assets/temperandtang/models/block/{name}.json").exists()
    if name == "shaping_anvil":
        assert {n["slot"]["slot"] for n in scene["nodes"] if "slot" in n and n["id"].startswith("grid_")} == {f"grid[{i}]" for i in range(9)}
print("Verified thermal workstation recipes, models and all nine distinct grid bindings.")
