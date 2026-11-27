"""Bundle verified builds, player documentation and presentation assets for review."""
from pathlib import Path
import hashlib
import json
import shutil
import subprocess
import sys
import zipfile
from verify_artifacts import ROOT, artifacts


def main():
    builds = artifacts()
    version = next(line.split("=", 1)[1].strip() for line in
                   (ROOT / "gradle.properties").read_text().splitlines()
                   if line.startswith("mod_version="))
    out = ROOT / "build" / "review" / version
    out.mkdir(parents=True, exist_ok=True)
    included = []

    def copy(source, relative):
        target = out / relative
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(source, target)
        included.append(target)

    for name in ("README.md", "LICENSE", "THIRD_PARTY_NOTICES.txt", "RELEASE_NOTES.md", "CHANGELOG.md", "DEVELOPMENT.md"):
        copy(ROOT / name, name)
    for source in sorted((ROOT / "docs").rglob("*")):
        if source.is_file():
            copy(source, source.relative_to(ROOT))
    for source in builds.values():
        copy(source, Path("builds") / source.name)
    showcase = ROOT / "build" / "showcase" / version
    for name in ("Temper-and-Tang-Complete-Showcase.mp4", "COMPLETE-TIMELINE.md"):
        source = showcase / name
        if source.exists():
            copy(source, Path("showcase") / name)

    start = out / "START-HERE.md"
    showcase_links = []
    for name, label in (("Temper-and-Tang-Complete-Showcase.mp4", "Full showcase"),
                        ("COMPLETE-TIMELINE.md", "Chapter guide")):
        relative = Path("showcase") / name
        if out / relative in included:
            showcase_links.append(f"[{label}]({relative.as_posix()})")
    showcase_header = "\n" + " · ".join(showcase_links) + "\n" if showcase_links else ""
    start.write_text("# Temper & Tang: review package\n" + showcase_header + """

## Try the build

Choose one JAR from `builds`: Fabric or NeoForge for Minecraft 1.21.1. Install Java 21 and the separate libraries listed in [Installation](docs/INSTALLATION.md). Use a backed-up test profile. The package does not install or change any profile or configuration.

[Workshop guide](docs/THERMAL_WORKSHOP.md) explains the complete starter route and controls. [Metallurgy](docs/METALLURGY.md) covers tin, bronze, iron, steel, silver and the template upgrades. [Verification](docs/VERIFICATION.md) records what has been tested and its limits.

## Prepare the project pages

- [Modrinth copy](docs/MODRINTH.md)
- [CurseForge copy](docs/CURSEFORGE.md), also provided as HTML
- [Icon and banner](docs/branding/README.md)
- Screenshots in `docs/images`
- [Release notes](RELEASE_NOTES.md)

Nothing has been published. Before publishing, create the project pages, review their required-dependency entries, and host the screenshots/banner. The page drafts currently use repository image URLs, which will work after those files are pushed to the matching repository branch. Keep the supplied-art notices with the distribution.

The source repository has the build and release workflows. Publishing and pushing are separate steps.
""", encoding="utf-8")
    included.append(start)
    commit = subprocess.check_output(["git", "rev-parse", "HEAD"], cwd=ROOT, text=True).strip()
    metadata = out / "PACKAGE.json"
    metadata.write_text(json.dumps({"version": version, "minecraft": "1.21.1", "sourceCommit": commit,
                                   "loaders": sorted(builds), "published": False}, indent=2) + "\n", encoding="utf-8")
    included.append(metadata)
    sums = out / "SHA256SUMS.txt"
    sums.write_text("".join(f"{hashlib.sha256(p.read_bytes()).hexdigest()}  {p.relative_to(out).as_posix()}\n"
                            for p in sorted(included)), encoding="utf-8")
    included.append(sums)
    archive = out.parent / f"Temper-and-Tang-{version}-Review-Package.zip"
    with zipfile.ZipFile(archive, "w", compression=zipfile.ZIP_DEFLATED, compresslevel=6) as z:
        for p in sorted(included):
            z.write(p, p.relative_to(out), compress_type=zipfile.ZIP_STORED if p.suffix == ".mp4" else zipfile.ZIP_DEFLATED)
    with zipfile.ZipFile(archive) as z:
        assert z.testzip() is None
        for p in included:
            assert hashlib.sha256(z.read(p.relative_to(out).as_posix())).digest() == hashlib.sha256(p.read_bytes()).digest()
    print(f"Review package: {archive}")
    print(f"Verified {len(included)} files; source commit {commit[:7]}")


if __name__ == "__main__":
    main()
