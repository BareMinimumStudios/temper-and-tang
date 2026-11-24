# Current revision

See [0.6.0 acceptance](REVISION_060_ACCEPTANCE.md) for the final requirement-by-requirement result and evidence. The records below describe earlier checkpoints.

# Verification — 0.4.2

## Quality showcase and puff smoothing

Version 0.4.2 passed both loader builds, 21 unit tests and resource/artifact checks. Four hidden NeoForge survival runs completed heating, shaping, quenching and finishing. Each used a fresh player at mastery 0. The scheduler and quality RNG were unchanged; each diamond run waited for a naturally occurring triple phrase. Finishing was accurate in all four cases to keep the comparison focused on shaping.

| Material | Attempt | Shaping accuracy | Missed beats | Actual grade | Actual luck roll |
| --- | --- | --- | --- | --- | --- |
| Bronze | accurate | 100% | 0 | Masterwork | 0.866646 |
| Diamond | accurate | 100% | 0 | Masterwork | 0.682756 |
| Diamond | missed | 72.7% | 3 | Superior | 0.816944 |
| Diamond | poor | 15.4% | 3 | Serviceable | 0.424895 |

These are actual examples, not a statistical guarantee. Poor timing creates lower-grade equipment; it does not destroy the item. Finishing retains the committed craftsmanship roll. The recordings include the new centered initial-stock presentation and the separate, smoothed bellows puffs. Capture reads only the isolated game's framebuffer and game audio.

## Shared-track and airflow revision

Version 0.4.1 passed 21 unit tests and resource/artifact validation on both loaders. New checks cover a shared time-to-position scale, exact perfect-band width, and equal quality rolls for equally accurate single and multi-hit work; missed notes lower quality through accuracy. No rarity bonus is added for phrase length.

The hidden NeoForge survival run waited for an actual randomly scheduled double, then completed both strikes 12 ticks apart without modifying the scheduler. Across 218 rendered frames with two notes visible, both markers shared the same path and height. The actual bellows air particles passed 3,180 directional samples with mean X velocity −10.83 scene pixels/second toward the hearth. The nozzle stream and its flame response were visually inspected in captured frames. All twelve hammer contact checks and the full survival route passed. Fabric completed its own hidden survival route with the revised visuals and particle registration.

## Rhythm revision

The final Fabric and NeoForge builds passed 19 unit tests, resource binding checks and packaged artifact checks. Deterministic rhythm tests cover material pacing/precision, fixed future notes after early input, cooldown rejection, missed-burst recovery and free preparation. Both dedicated servers passed equipment, scored finishing and the complete thermal scenarios with the new timing implementation.

Both hidden clients completed the actual survival heating → shaping → quenching → finishing route. Fabric then restarted as the same player and matched five inventory records and mastery exactly. NeoForge checked all twelve rendered hammer contacts against the actual model striking face, with maximum error 0.000001 scene pixels; sparks were present at each contact. Thirty-one submerged frames retained tong alignment, with maximum error below 0.000002 pixels. Both clients displayed ten equipment-specific blank forms. The 64-second demonstration captures only the isolated game's picture and sound. Multiplayer network latency remains untested; ownership/cleanup coverage uses server-side test players.

## Progression and guidance revision

Both loaders passed crafting, preservation and thermal server diagnostics with Simply Swords and Epic Knights. The NeoForge optional Simply More catalog exercised 378 routed patterns through shaping, quenching, finishing and serialization. Metal tools, including hoes, are supported. Zero-bonus settings passed the standalone NeoForge fixtures; disabling recipe integration restored native crafting recipes on Fabric.

Both hidden loader clients completed the survival workshop route and preserved five crafted/thermal records and mastery through an actual process restart under the same player identity. NeoForge additionally checked station ownership, replay rejection, logout returns, and full-inventory overflow using two server-side test players. This is not a substitute for a remote multiplayer latency test. Compact guidance passed 854×480 and 1280×720 checks, with optional H help and Shift equipment details.

## Previous presentation checks

## Workstation detail pass

Version 0.3.2 passed the clean Fabric/NeoForge build, unit, resource and packaged-artifact checks. The hidden packaged NeoForge client completed the full survival workflow with the new block models and animated bellows trim. Six shaping and six finishing contacts passed; rendered contact error remained below 0.000002 pixels. Thirty-one submerged frames passed tong alignment checks. Intermediate stock, timing movement and the bellows mesh checks also passed. Screenshots of all four active workstations were inspected for material mapping, joinery, band continuity, tool clearance and retained interaction placement.

Hashes confirmed that both simpler hammer models and the hammer/tong motion implementation were unchanged. The bellows timing, opening and hinge-angle calculation was compared directly with the saved baseline and remained identical. Only its leather geometry helper was shared with the additional edge welts. The recipe data, gameplay rules and live configuration were not edited by this detail pass.

The 64.2-second demonstration was captured with only the isolated game's picture and sound. The earlier two-loader server and save/reload coverage below applies to the unchanged gameplay implementation; the new graphical detail pass was tested on NeoForge, with Fabric graphical playtesting still outstanding.


## Physical presentation revision

Both loader artifacts passed the clean build, unit tests, resource checks and packaged-artifact checks. Both dedicated servers passed equipment/stat and preservation diagnostics, scored finishing scenarios and the complete thermal scenarios with Simply Swords and Epic Knights present. The isolated recipe catalog contained 344 patterns, including 54 armor patterns.

The packaged NeoForge release completed a hidden real-player survival recording: heated two bronze ingots, shaped a sword with six timed blows, quenched it and applied a six-blow finish. The external test driver checked the rendered hammer's actual striking-face geometry at each sound event: six contacts on each anvil, with maximum error below 0.000002 scene pixels. Scene sparks were present at every impact. The tong jaws remained aligned with the submerged workpiece across 31 checked frames, with maximum error below 0.000002 pixels.

The same run required three intermediate metal models and rejected premature finished-item previews during shaping. It captured the bellows leather mesh and multiple lid poses. More than 100 rendered timing frames were checked for backward movement outside beat resets. The final recording passed after correcting the initial unarmed-beat twitch. Workshop captions remain fully opaque while progress text changes, avoiding repeated fade-in flashes.

The 64-second demonstration includes only the isolated game's video and audio. No desktop or microphone audio was captured. Bellows, contact, intermediate-stock and submerged-tong screenshots were inspected. Model geometry uses original assets and vanilla texture references; impact sounds are original procedural synthesis.

These are local-client checks, not a remote-latency or universal frame-rate guarantee. The earlier save/reload, cancellation, recipe-routing and third-party integration coverage below remains relevant; the presentation update does not change those rules.

## Earlier gameplay coverage

## Build and mathematical checks

Java 21, Kotlin and both Minecraft 1.21.1 loader targets compile. Common tests cover finish arithmetic, thermal accounting, randomized quality distributions across 20,000 seeded samples, mastery progression, timing misses, duplicate/backward strikes, idle preparation and long custom strike cooldowns. Resource and distributable checks verify common tags, recipes, scenes, runtime classes, licenses and separation from external test drivers.

## Dedicated servers

Isolated Fabric 0.20.1 and NeoForge 21.1.267 servers passed item, crafting, cooking, craftsmanship, finishing and thermal diagnostics with Simply Swords 1.70.2 and Epic Knights installed. Both passed after datapack reload and saved normally. No Overgeared mod was installed.

The checks cover six equipment types and all six grades; names, enchantments, foreign custom data and damage; component serialization; repeated bonus rebuilding; actual netherite smithing upgrades; per-slot armor movement modifiers; shaped/shapeless pattern matching; batch heating and fuel costs; server timing and missed beats; duplicate-action rejection; one-time quality commitment; quenching; armor reforging and costs; mastery awards; reusable blueprints and mastery gating; finish XP recovery; cancellation and duplicate-output refusal.

A NeoForge fixture without Simply Swords or Epic Knights passed the same standalone workflow with 26 equipment patterns, including 12 armor patterns. A Fabric fixture with recipe integration disabled passed item/stat diagnostics and confirmed ordinary iron and bronze crafting remained available.

## Hidden client in a copied modpack

The packaged NeoForge mod ran in an isolated copy of New Test. Actual Diegesis mouse and Space input completed heating, timed shaping, quenching and armor finishing. Every visible workstation slot and control was ray-tested at 854×480 and 1280×720. All four block facings were represented. Screenshots were inspected for alignment, timing-gauge placement, caption wrapping, armor appearance, tool rests, water and embers. Rendering hides the local player's body while focused, and unfinished armor borrows its contained armor model.

A real world restart retained the actual player's smithing mastery, crafted/finished armor, reusable blueprint and encoded armor workpiece. Persisted item components were compared against a saved snapshot. Earlier real-player tests also checked unprotected burns, own-tongs protection and bounded wear, cursor handling, air cooling, names and saved hot stock.

The copied profile's existing food, travel, loot and workshop recipe restrictions remained in effect. Its removed recipes were not silently restored. Five existing Create workshop recipes matched and assembled with the standalone mod. Pattern availability therefore differs from an unrestricted server.

## Limits and external messages

This is a tested first revision, not a claim that every mod combination or long-term economy is proven balanced. Fabric graphical interaction, real remote-network latency, live two-player contention and exhaustive third-party custom damage systems still need playtesting. Projectile damage outside normal equipment attributes needs separate integration. Workshop contents are session-owned and returned on close; these blocks are not unattended inventory storage.

The hidden development copy excluded Diagonal Fences because its model modification fails in that launcher; the live profile was not changed for that test workaround. Existing pack warnings include optional missing models, recipe-viewer categories and third-party data-fixer messages. A Small Ships config-unload error can occur after all dimensions have saved; that external shutdown issue was not represented as a Temper & Tang gameplay failure or silently patched in the live pack.

All automated gameplay fixtures, worlds and external test-driver sources are separate from the distributable mod and live saved worlds. No artifact is published by the build or release scripts.


## 0.5.0 equipment and metallurgy verification

Java 21; Fabric Loader 0.20.1 / NeoForge 21.1.267; Diegesis beta.17. Production JARs were used in isolated runtime fixtures. No live profile or user world was changed.

| Check | Evidence and scope |
| --- | --- |
| Build and pure rules | Both loader distributions built; 24 unit tests passed, including complete-batch/output bounds and material conservation. Resource and artifact verifiers validate JSON, scene bindings, common tags, metadata and packaged alloy/catalog classes. |
| Enabled optional integrations | Both dedicated servers passed `/temperandtang verify` and `verify_metallurgy`. All 404 NeoForge / 374 Fabric available patterns were forged, quenched, finished and serialized by the diagnostic. NeoForge includes 30 Simply More patterns; both include 274 Epic Knights and 45 Simply Swords patterns. |
| Standalone | Optional weapon mods removed from both isolated fixtures; verification and metallurgy passed with 55 patterns, including 20 armor patterns, and zero optional patterns. |
| Integrations disabled | Optional mods restored, their support switches and recipe integration disabled; both servers started and verification passed with zero routed patterns. |
| Alloy correctness | JSON/network recipe codec roundtrips; actual raw copper/iron/tin and bronze-blend smelting; six copper plus three tin gives eight bronze and one spare tin; eight iron plus eight carbon gives eight steel and costs two separate fuel. Hot stock works. Missing fuel, partial batches and overflow are refused. |
| Consumption and cancellation | Closing cancels without producing alloy; stock/additive are returned, burned fuel stays spent. Input mutation invalidates the running plan. Output collection is single-use; hot state survives serialization. |
| Mining and repairs | Every new catalog item accepts its common-tag repair metal. Bronze mines iron but not diamond; steel mines diamond but not obsidian. |
| Native compatibility | 77 actual Epic Knights component recipes matched and assembled using workshop steel/nuggets. Native armet gilding retains workmanship, steel material, trait, finish, wear and raw durability provenance. Removed/custom native steel recipes are not recreated or rewritten. |
| Component preservation | Upgrade fixture preserves external durability adjustments, foreign damage affixes, portable metadata and custom data. Authored result values win; previous quality does not multiply into a new raw baseline. Repeated stat rebuilds do not stack bonuses. |
| Actual NeoForge controls | Hidden survival clients completed raw tin/copper → bronze and raw iron → steel → sword → quench → finish using the physical Diegesis slots and timing controls. Expected fuel and leftover quantities were checked. All 26 newly added equipment models loaded; bronze/steel armor was worn and rendered. |
| Actual Fabric controls | Hidden survival client completed raw iron → steel → sword → quench → finish, with exact fuel/ingredient checks and all 26 new models loaded. |
| Disk persistence | Bronze and steel NeoForge workflows were restarted and compared against six saved actual inventory/equipment records plus mastery. The steel check used its own world to avoid integrated-server host inventory sharing between test identities. |
| Complete current pack | Copied New Test mods/configs into a separate directory and used the cached installed NeoForge runtime. Raw iron → steel → shaping → quenching → finishing passed through actual controls. Six worn/crafted records and mastery survived a full restart. Live New Test stayed on 0.4.2. |

The complete pack still logs Small Ships' `getFullPath()` config-unloading exception after saving during shutdown. The progression and subsequent saved-item reload passed; this revision does not change that mod or reinstall an unload workaround. The development launcher also produced unrelated model-transform errors not reproduced by the installed runtime. Offline verification identities can produce external-service authentication warnings.

Runtime logs and captures are retained in the isolated verification workspace: `final-050-preservation-metallurgy.log`, `standalone-050.log`, `flags-disabled-050.log`, `normal-pack-progression.log`, `normal-pack-reload.log`, and the bronze/steel progression capture directories. QA probes, hidden-window hooks, supplied fixture resources and video tools are not packaged in the mod.

These checks establish the implemented routes and local preservation behavior. They do not establish arbitrary third-party ability damage, remote-server latency balance, cross-version world migration or long-term economy balance. Worn armor currently deliberately uses vanilla gold/iron visual layers; the distinct item models and workstations use original geometry with Minecraft textures.


## 0.5.1 workstation presentation verification

Both Minecraft 1.21.1 loader distributions built and passed resource/artifact verification; 26 unit tests passed. This revision's graphical runtime checks used NeoForge 21.1.267 and Diegesis beta.19 in isolated copies. No live profile or player world was modified.

- Actual survival bronze heating, shaping, quenching and finishing completed with the packaged build. Twelve rendered hammer impacts across both anvils were checked against the workpiece at the clang event, with sparks present. Right-handed and mirrored left-handed presentations completed. Actual submerged tong jaws stayed aligned with the workpiece.
- A harder diamond workflow completed with triple strikes and three deliberately missed beats. The recorded accuracy was 72.7%; quality remained a server-authoritative random outcome.
- Server insertion tests rejected invalid alloy additives and finishing materials through left click, right click, shift transfer and invalid swaps, preserving existing stacks. Valid tin, coal, charcoal and treatment insertion passed. All four stations' collision bounds stayed within their block in all four orientations.
- The copied complete pack passed direct pattern search, reverse-order keyword matching, Escape handling and selection while holding hot stock. The hot-stock timestamp was preserved rather than extended. Recreated shaping logic retained a valid saved pattern. Mouse release did not leak into a neighboring station.
- Rendered rhythm positions remained on the same track across phrase changes; actual bellows compression produced directed air bursts. Pure clock tests cover jitter at 30, 60 and 144 frames per second. This verifies the implemented interpolation, not every possible hardware frame-time spike.

The final hammer uses the supplied model's proportions and a main-hand side swing, leaving the front-facing workpiece view clearer. Both release JARs exclude test probes and recording hooks. Fabric was built and package-checked for this revision; its graphical runtime was not repeated in this pass.
