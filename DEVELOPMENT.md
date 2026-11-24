# Development

Use Java 21 and the checked-in Gradle wrapper. Shared Kotlin rules live in `src/main/kotlin`. Integration shared by both loaders lives in `src/loaderShared/kotlin`; loader entrypoints live in their corresponding source folders.

Run `scripts/build.ps1 -Clean` or `scripts/build.sh` for tests, loader builds, resource verification and local release staging. Dependency versions are pinned in `libraries.toml`. `scripts/update-gradle.ps1` checks the official stable Gradle release and updates the wrapper with its checksum. Review and test dependency updates before committing them.

The code compiles for the loader's Kotlin runtime. Required libraries remain separate dependencies. Configuration uses validated fields and server-owned gameplay settings.

A passing server does not establish that the stations look or feel right. Check both world and focused views, ingredient slots, taking output, cancellations, interrupted connections and two-player use in a disposable world. Keep automated client runs hidden and capture only their framebuffer and game audio.

Prepare release files locally with `scripts/prepare_release.py`. Publishing and pushing are separate actions. See [verification](docs/VERIFICATION.md) and [release preparation](docs/RELEASING.md).
