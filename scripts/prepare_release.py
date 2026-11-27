"""Stage only verified loader JARs. Never uploads or publishes anything."""
from pathlib import Path
import hashlib, json, re, shutil
from verify_artifacts import EXPECTED_VERSION, ROOT, artifacts
files = artifacts()
changelog = (ROOT / "CHANGELOG.md").read_text(encoding="utf-8")
heading = re.search(rf"^## (?:{re.escape(EXPECTED_VERSION)}|\[{re.escape(EXPECTED_VERSION)}\])(?:[ \t]+[-—][^\n]*)?[ \t]*$", changelog, re.M)
if heading is None:
    raise SystemExit(f"Missing changelog section for {EXPECTED_VERSION}")
notes = re.split(r"^## ", changelog[heading.end():], maxsplit=1, flags=re.M)[0].strip()
if not notes:
    raise SystemExit(f"Empty changelog section for {EXPECTED_VERSION}")
out = ROOT / "build/release"
out.mkdir(parents=True, exist_ok=True)
# Keep the upload directory limited to this version; retain old builds locally.
current = {source.name for source in files.values()}
for old in out.glob("TemperAndTang-*.jar"):
    if old.name in current:
        continue
    archive = ROOT / "build/release-history" / old.name
    archive.parent.mkdir(parents=True, exist_ok=True)
    if archive.exists():
        assert archive.read_bytes() == old.read_bytes(), f"Archive collision: {old.name}"
        old.unlink()
    else:
        shutil.move(old, archive)
manifest = []
for loader, source in files.items():
    target = out / source.name
    shutil.copy2(source, target)
    manifest.append({"loader": loader, "minecraft": "1.21.1", "file": target.name,
                     "version": f"{EXPECTED_VERSION}+1.21.1-{loader}", "changelog": "CHANGELOG.md",
                     "sha256": hashlib.sha256(target.read_bytes()).hexdigest()})
(out / "CHANGELOG.md").write_text(notes + "\n", encoding="utf-8")
(out / "manifest.json").write_text(json.dumps(manifest, indent=2) + "\n")
(out / "SHA256SUMS.txt").write_text("".join(f"{x['sha256']}  {x['file']}\n" for x in manifest))
for name in ("LICENSE", "THIRD_PARTY_NOTICES.txt", "RELEASE_NOTES.md"):
    shutil.copy2(ROOT / name, out / name)
print(f"Staged verified release files in {out}")
