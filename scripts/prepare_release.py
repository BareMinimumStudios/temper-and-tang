"""Stage only verified loader JARs. Never uploads or publishes anything."""
from pathlib import Path
import hashlib, json, shutil
from verify_artifacts import ROOT, artifacts
files = artifacts()
out = ROOT / "build/release"
out.mkdir(parents=True, exist_ok=True)
manifest = []
for loader, source in files.items():
    target = out / source.name
    shutil.copy2(source, target)
    manifest.append({"loader": loader, "minecraft": "1.21.1", "file": target.name,
                     "sha256": hashlib.sha256(target.read_bytes()).hexdigest()})
(out / "manifest.json").write_text(json.dumps(manifest, indent=2) + "\n")
(out / "SHA256SUMS.txt").write_text("".join(f"{x['sha256']}  {x['file']}\n" for x in manifest))
for name in ("LICENSE", "THIRD_PARTY_NOTICES.txt", "RELEASE_NOTES.md"):
    shutil.copy2(ROOT / name, out / name)
print(f"Staged verified release files in {out}")
