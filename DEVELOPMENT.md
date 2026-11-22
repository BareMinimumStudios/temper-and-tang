# Development

Use Java 21 and the wrapper. Shared pure Kotlin rules live in `src/main/kotlin`; Minecraft/API integration shared by both loaders lives in `src/loaderShared/kotlin`. Loader entrypoints live in `src/fabric/1.21.1/kotlin` and `src/neoforge/1.21.1/kotlin`.

The Cloche build and version catalog follow the studio's PlayerEx/DifficultyEx layout. Game compilation targets Kotlin 2.2.20 for the installed loader runtimes; the Gradle Kotlin plugin version follows the sibling projects. Fzzy Config fields use validated values and the same configuration grouping conventions. Do not bundle Simply Swords, Overgeared or Diegesis.

Run `scripts/build.ps1 -Clean` or `scripts/build.sh` for tests, both loader builds, metadata/license verification and release staging. `scripts/update-gradle.ps1` checks the official current stable Gradle release and updates the wrapper with its checksum. Review and test dependency updates; keep released builds pinned and reproducible.

Dedicated-server and client runs are separate checks. A passing headless server does not establish that the in-world scene is visually correct. Keep graphical testing optional and never launch it on another person's screen without their instruction.
