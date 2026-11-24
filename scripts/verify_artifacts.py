"""Verify actual distributable metadata, API payload and license packaging."""
from pathlib import Path
import json, zipfile, tomllib, re
ROOT = Path(__file__).resolve().parents[1]
EXPECTED_VERSION = re.search(r"^mod_version=(.+)$", (ROOT / "gradle.properties").read_text(), re.M)[1].strip()

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
                assert m["version"] == EXPECTED_VERSION
                assert "diegesis" in m["depends"] and "fzzy_config" in m["depends"]
                assert m["entrypoints"]["main"] and m["entrypoints"]["client"]
                assert m["entrypoints"]["emi"] and m["entrypoints"]["jei_mod_plugin"]
                loader = "fabric"
            elif "META-INF/neoforge.mods.toml" in files:
                m = tomllib.loads(z.read("META-INF/neoforge.mods.toml").decode())
                assert m["mods"][0]["modId"] == "temperandtang"
                assert m["mods"][0]["version"] == EXPECTED_VERSION
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
            for name in ("AlloyRecipe", "AlloyRecipes", "HearthProcesses"):
                assert f"com/bareminimumstudios/temperandtang/smithing/{name}.class" in files
            assert "com/bareminimumstudios/temperandtang/content/WorkshopCatalog.class" in files
            for name in ("bronze_alloy", "steel_alloy", "smithing_tongs_from_bronze"):
                assert f"data/temperandtang/recipe/{name}.json" in files
            for metal in ("bronze", "steel"):
                for part in ("pickaxe", "helmet", "spear", "warhammer"):
                    assert f"assets/temperandtang/models/item/{metal}_{part}.json" in files
                    assert f"data/temperandtang/recipe/{metal}_{part}.json" in files
            assert "data/minecraft/tags/item/trimmable_armor.json" in files
            assert "data/temperandtang/weapon_attributes/steel_spear.json" in files

            assert "com/bareminimumstudios/temperandtang/smithing/HotMetalHandling.class" in files
            assert "assets/temperandtang/models/item/smithing_tongs.json" in files
            assert "com/bareminimumstudios/temperandtang/smithing/ShapingRecipe.class" in files
            assert "com/bareminimumstudios/temperandtang/client/ThermalClient.class" in files
            for name in ("Craftsmanship", "GearStats", "SmithingProgress", "Blueprints", "ForgeRandom"):
                assert f"com/bareminimumstudios/temperandtang/smithing/{name}.class" in files
            assert "com/bareminimumstudios/temperandtang/client/WorkpieceAppearance.class" in files
            assert "com/bareminimumstudios/temperandtang/mixin/WorkpieceRenderMixin.class" in files
            assert "com/bareminimumstudios/temperandtang/compat/NativeSmithing.class" not in files
            assert "temperandtang.mixins.json" in files
            mixins = json.loads(z.read("temperandtang.mixins.json"))
            for name in ("ArrowQualityMixin", "CrossbowQualityMixin", "PlayerListSaveAccess", "RecipeManagerMixin"):
                assert name in mixins["mixins"]
                assert f"com/bareminimumstudios/temperandtang/mixin/{name}.class" in files
            for name in ("PrecisionRefiningRecipe", "RangedCraftsmanship", "WorkshopRhythm"):
                assert f"com/bareminimumstudios/temperandtang/smithing/{name}.class" in files
            assert "com/bareminimumstudios/temperandtang/station/WorkshopRecovery.class" in files
            assert "assets/temperandtang/models/diegesis/crossbow_blank.json" in files
            assert "assets/temperandtang/models/block/alloy_furnace.json" in files
            assert "assets/temperandtang/models/diegesis/alloy_furnace_body.json" in files
            assert "assets/temperandtang/textures/block/workshop_six/blacksmith_water_barrel.png" in files
            assert not any(n.startswith(("com/pockyl/", "net/minecraft/", "net/sweenus/")) and n.endswith(".class") for n in files)
            assert "com/bareminimumstudios/temperandtang/client/TemperClient.class" in files
            for name in ("PatternPicker", "WorkshopSixBellows"):
                assert f"com/bareminimumstudios/temperandtang/client/{name}.class" in files
            assert "com/bareminimumstudios/temperandtang/mixin/PatternInputMixin.class" in files
            for name in ("blacksmith_anvil", "blacksmith_bellows", "blacksmith_hearth", "blacksmith_hearth_coals", "blacksmith_tool_board_long", "mine_pallet_stone", "mine_support"):
                assert f"assets/temperandtang/textures/block/workshop_six/{name}.png" in files
            for name in ("charcoal_hearth_body", "workshop_six_bellows_base", "workshop_six_bellows_top"):
                assert f"assets/temperandtang/models/diegesis/{name}.json" in files
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
