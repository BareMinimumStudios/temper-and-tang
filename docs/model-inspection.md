# Model inspection — 0.6.0

All 108 shipped models were validated and rendered in an isolated NeoForge 1.21.1 client. All nine gallery pages were visually inspected. The hammer, tongs, hearth, water barrel, furnace, both anvils, and separate bellows components were additionally inspected from four angles (36 views).

- No missing baked models or unresolved face textures were found.
- Model parents, texture aliases, UV ranges, element bounds, legal rotations, display scales, and the five station scene references passed validation.
- The hammer shaft and thicker grip previously sampled contrasting pale bands in the shared tool-board atlas. They now use stripped spruce with longitudinal grain and end-grain caps. A second pass uses the same wood for both pieces and aligns their sampled grain with their actual length, removing the abrupt light/dark grip bands. The metal head retains the supplied artwork. Geometry, impact position, and animation pivots are unchanged.
- Tool thickness and station proportions were checked in the rendered gallery; the tongs retain uniform three-dimensional scaling. The supplied barrel and bellows support remain textured.
- Both Fabric and NeoForge distributions build successfully, and the existing unit checks pass.

These are static and rendered-model checks. They do not replace the ongoing survival, animation, recipe-viewer, and multiplayer acceptance tests. The live New Test profile was not modified.

## Current-build recheck

All 108 resources were baked again from four angles (432 renders across 36 pages), including face-direction quads rather than only unculled faces. All pages were visually inspected. Enlarged tool renders were inspected before and after the handle correction. No missing baked textures, inverted surfaces, visibly stretched station textures, or new sizing defects were found in these views. The held-item hammer inherits the same model as both anvil scenes. Its head, striking face, proportions and animation pivots remain unchanged.

Both loader builds and checks pass after the texture change. These checks were run in the hidden isolated runtime; New Test remains untouched.
