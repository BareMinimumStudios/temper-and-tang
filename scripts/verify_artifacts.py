"""Verify actual distributable metadata, API payload and license packaging."""
from pathlib import Path
import json, zipfile, tomllib
ROOT = Path(__file__).resolve().parents[1]
def artifacts():
    found = {}
    for p in sorted((ROOT / "build/libs").glob("*.jar")):
        if p.name.endswith(("-sources.jar", "-dev.jar")): continue
        with zipfile.ZipFile(p) as z:
            files = set(z.namelist())
            loader = None
            if "fabric.mod.json" in files:
                m = json.loads(z.read("fabric.mod.json"))
                assert m["id"] == "temperandtang"
                assert "diegesis" in m["depends"] and "fzzy_config" in m["depends"]
                assert m["entrypoints"]["main"] and m["entrypoints"]["client"]
                loader = "fabric"
            elif "META-INF/neoforge.mods.toml" in files:
                m = tomllib.loads(z.read("META-INF/neoforge.mods.toml").decode())
                assert m["mods"][0]["modId"] == "temperandtang"
                deps = {x["modId"] for x in m["dependencies"]["temperandtang"]}
                assert {"diegesis", "fzzy_config", "kotlinforforge"} <= deps
                loader = "neoforge"
            if loader is None: continue
            assert loader not in found, f"Multiple {loader} distributions: clean the build directory"
            assert "META-INF/licenses/temperandtang-LICENSE" in files
            assert "META-INF/licenses/THIRD_PARTY_NOTICES.txt" in files
            assert "assets/temperandtang/diegesis/scenes/finishing_anvil.json" in files
            assert "com/bareminimumstudios/temperandtang/station/FinishingStation.class" in files
            for station in ("charcoal_hearth", "shaping_anvil", "quenching_trough"):
                assert f"assets/temperandtang/diegesis/scenes/{station}.json" in files
            assert "com/bareminimumstudios/temperandtang/station/ThermalStations.class" in files
            assert "com/bareminimumstudios/temperandtang/smithing/HotMetalHandling.class" in files
            assert "assets/temperandtang/models/item/smithing_tongs.json" in files
            assert "com/bareminimumstudios/temperandtang/smithing/ShapingRecipe.class" in files
            assert "com/bareminimumstudios/temperandtang/client/ThermalClient.class" in files
            assert "temperandtang.mixins.json" in files
            assert "com/bareminimumstudios/temperandtang/client/TemperClient.class" in files
            assert "assets/temperandtang/models/diegesis/striking_hammer.json" in files
            assert "assets/temperandtang/models/block/finishing_anvil.json" in files
            assert not any(n.startswith("com/bareminimumstudios/temperandtang/qa/") for n in files)
            assert "HiddenWindowMixin" not in z.read("temperandtang.mixins.json").decode()
            for n in files:
                if n.endswith(".json"): json.loads(z.read(n))
            found[loader] = p
    assert set(found) == {"fabric", "neoforge"}, f"Expected both loader distributions, found {found}"
    return found
if __name__ == "__main__":
    for loader, path in artifacts().items(): print(f"Verified {loader}: {path.name}")
