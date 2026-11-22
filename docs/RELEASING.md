# Release checklist

1. Update the version and release notes.
2. Run a clean build and tests using `scripts/build.ps1 -Clean -Version 0.1.0` or `scripts/build.sh -Pmod_version=0.1.0`.
3. Test Fabric and NeoForge servers, optional mods absent/present, recipe reloads and item persistence.
4. Test the Diegesis scene on both clients: slot hit boxes, accessibility, cancellation, disconnect, two-player contention and taking output into a full inventory.
5. Verify existing names, enchantments and Overgeared quality survive; verify no free repair, duplicated output or repeated XP charge.
6. Review `build/release/manifest.json` and `SHA256SUMS.txt`.
7. Commit and push when ready. The manual workflow can create a draft GitHub release; publishing remains an explicit action.
8. Once Modrinth pages exist, configure their IDs and token securely. Do not put tokens in source or add an untested automatic publication step.

Until the client checks pass, mark uploads as experimental rather than a stable finished smithing overhaul.
