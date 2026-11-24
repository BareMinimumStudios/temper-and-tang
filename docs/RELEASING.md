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
