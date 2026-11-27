# Prepare a release

1. Review the version and release notes.
2. Run `scripts/build.ps1 -Clean` or `scripts/build.sh` with Java 21.
3. Check both servers and clients, with optional integrations present and absent. Include recipe reload, saved equipment, cancellation and simultaneous station use when their code changes.
4. Review the staged JARs in `build/release`, their manifest and SHA-256 sums.
5. Check the README and page copy against the current screenshots and behavior.
6. Keep code and artwork credits with the distribution.

The page drafts are `docs/MODRINTH.md`, `docs/CURSEFORGE.md` and `docs/CURSEFORGE.html`. The banner/icon are in `docs/branding`, and actual gameplay captures are in `docs/images`. Upload the media to the chosen platform or use the committed raw media URLs after pushing the files. There are no invented live project-download links in the drafts.

Publishing and pushing are separate actions. Keep tokens outside the repository. The workflow can prepare a draft release when explicitly requested; local staging never publishes by itself.

## Review package

After building and staging the release, run `python scripts/prepare_review.py`. It bundles both verified loader builds, documentation, branding, screenshots and the completed showcase when available into `build/review/<version>`. The ZIP includes checksums and the source commit. It does not install, upload or publish anything.

## Modrinth and CurseForge

The **Build and publish** workflow is manual. Select **prepare** to build, test and stage both Minecraft 1.21.1 jars as a GitHub Actions artifact. To upload, select Modrinth, CurseForge, or both, then choose the channel and loader. Leaving the channel at **beta** marks the files as playtest builds. Use the destination and loader filters to retry only failed uploads.

- Modrinth project slug is `temper-and-tang`. Set the `MODRINTH_TOKEN` Actions secret before uploading there.
- CurseForge project ID is `1834462`. Set the `CURSEFORGE_TOKEN` Actions secret before uploading there.
- Set `mod_version` in `gradle.properties` and update the heading and content in `RELEASE_NOTES.md` before dispatching a new version.

From PowerShell with GitHub CLI signed in:

```powershell
.\scripts\publish.ps1
.\scripts\publish.ps1 -Target all -Channel beta -Upload
```
