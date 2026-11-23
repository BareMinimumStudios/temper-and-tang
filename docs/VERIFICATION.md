# Verification — 0.3.1

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
