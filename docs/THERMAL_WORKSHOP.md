# Standalone thermal workshop guide

Temper & Tang supplies its own tools, heat, shaping and quenching. It requires Diegesis, Fzzy Config and your loader's Kotlin runtime. It does not require Overgeared.

The station normally shows only your step, next action and ingredients. **H** opens extra help. Hold **Shift** over forged equipment for detailed stats and maker information. Colors highlight quantities and important actions without replacing the words.

## Make the tools and workstations

- **Smithing Tongs:** two iron nuggets in the top corners, two iron ingots beneath them, and a stick in the bottom centre. Common iron and wooden-rod tags work. Repair the tongs with an iron ingot at a vanilla anvil.
- **Smithing Hammer:** the normal bronze-and-stick recipe shown in your recipe viewer.
- **Charcoal Hearth:** seven cobblestone around coal or charcoal; leave the top-middle space empty.
- **Shaping Anvil:** three bronze ingots above one smooth stone and three planks.
- **Quenching Trough:** five planks in a boat shape with two sticks beneath its corners.

## Heat → shape → quench → finish

The temporary pattern board is a preparation guide. When hammer work starts, the ingredients become a hot billet, then a drawn blank. The completed equipment appears only after the final shaping blow. It still needs quenching before use.

1. Hold **Smithing Tongs in your offhand**. Either hand protects you, but the offhand leaves your cursor free to operate the stations.
2. Put up to eight refined ingots and enough coal or charcoal into the hearth. Click the bellows or press **Space** with an empty cursor. Base heating is five seconds per item; steel, diamond and netherite stock take longer. A coal pays for up to 1,600 heating ticks; fuel is spent when work starts.
3. Retrieve the hot stock. It has about **60 seconds** of working heat by default. Its tooltip shows the remaining time. Reheat if it cools before shaping.
4. Open the shaping anvil. The ghost pattern shows the selected equipment. Use **Left/Right** to change patterns and **Tab** to filter weapons or armor. Advanced Epic Knights patterns require a matching reusable blueprint. Put heated metal and the remaining ingredients in the shown 3×3 arrangement, then insert the hammer.
5. Click the hammer or press **Space** when the moving timing marker reaches the central target. Timing varies between blows. Take time to prepare: missed-beat penalties begin only after your first strike. Accurate strikes improve the quality odds; missed beats reduce accuracy. Clicking repeatedly cannot accelerate the work. Most basic gear takes six blows; armor and harder materials take more. The final blow rolls quality and one possible equipment trait once on the server.
6. The final blow commits an **Unquenched Workpiece**. It is not yet a usable weapon. Carry it to the trough with your tongs, add a water bucket, then click the trough tongs or press **Space**. Default quenching takes two seconds, consumes the water and leaves an empty bucket.
7. Retrieve the usable cooled weapon and empty bucket. You may now finish the equipment at the finishing anvil using a finishing material, paced blows and experience.

The tongs shown at the trough are its control. Your handheld Smithing Tongs protect you while moving hot stock between workstations.

## Hot-metal handling

Without held tongs, carrying hot stock deals **half a heart every two seconds** by default. This is contact damage, not persistent fire. Cursor-held hot stock also counts. Multiple stacks do not multiply damage or tool wear.

Held tongs take one durability when first handling stock, then at most one per **ten seconds of hot handling**, regardless of stack count. They have 256 durability, can be repaired, and do not wear while carrying only cold items. One tool lasts roughly 42 minutes of active hot handling at the default rate. Creative/spectator players are exempt; Fire Resistance prevents burns.

Hot stock automatically becomes cold in your inventory or on your cursor when its working heat expires. Chest and dropped-item heat also expires with world time; moving it back into your inventory normalizes its saved state. A cold unquenched workpiece stays unusable: reheat it, then quench it. Air cooling cannot skip that step.

## Closing and saving

- Closing returns owned inputs, tools, buckets and completed workpieces through Diegesis. Keep inventory space available; its normal overflow handling applies.
- Partial shaping loses progress without consuming materials or hammer wear. The final blow consumes both and commits the workpiece, so closing cannot reroll it for free.
- Fuel and water already spent are not refunded by cancelling.
- Working heat, encoded workpieces, worn tongs, names and foreign item data survive saving and reloading.
- Workstations use session inventories rather than unattended persistent block storage. Close them before leaving.

## Settings and compatibility

`config/temperandtang/config.json5` exposes heating/cooling time, shaping rhythm, quenching time, contact damage/interval, handling enablement and tongs wear through Fzzy Config. Server values govern gameplay. Turning off contact handling still allows existing stock to cool. Disabling thermal shaping or recipe integration and reloading restores ordinary equipment crafting recipes.

Other tool authors can add tongs to `c:tools/tongs` or `temperandtang:tongs`. Refined stock uses additive `temperandtang:heatable_stock`, populated by common iron, gold, copper, tin and bronze ingot tags. `temperandtang:shaping` uses vanilla shaped-recipe fields and is independent of another smithing mod.

Simply Swords and Epic Knights equipment use their original item identities and patterns. Supported metal gear recipes are routed into shaping; the mod does not recreate missing third-party recipes or override a pack recipe blacklist. Simply More weapons also support finishing. No external smithing mod supplies the workflow.

Operators can run `/temperandtang verify`, `/temperandtang verify_station` and `/temperandtang verify_thermal`. The first includes isolated tongs, cooling and burn checks; the thermal scenarios take about a minute with default settings.

## Workstations

![Charcoal hearth and bellows](images/charcoal-hearth.png)

![Shaping anvil and working hammer](images/shaping-anvil.png)

![Quenching trough](images/quenching-trough.png)

## Quality, mastery and blueprints

Craftsmanship has six grades: **Rough, Serviceable, Fine, Superior, Masterwork and Exceptional**. Accuracy provides most of the result, with a smaller server-random contribution and a modest mastery bonus. Perfect play improves the odds rather than guaranteeing the highest grade. Quality changes the gear's own base durability and appropriate damage, armor or toughness attributes. Enchantments remain a separate final enhancement. Repeated processing replaces the previous craftsmanship modifiers rather than stacking them.

Use `/temperandtang mastery` to see your smithing level and experience. Completing new equipment grants more mastery than reforging existing gear. The maximum level is 20. The crafted item records its maker, strike accuracy and the maker's level when it was made.

Craft **Blank Blueprints** from paper and black dye. Select an advanced pattern, insert a blank and press **B** to inscribe it once you reach the configured mastery requirement (level 1 by default). The finished template is reusable and selects its exact recipe when inserted. You still supply every recipe ingredient and a hammer. Templates do not create equipment for free.

To reforge, heat existing supported equipment, press **R** at an empty shaping anvil and insert the gear, hammer and **Smithing Flux**. Flux is made from sand, clay and coal. Complete the timing work and quench the new workpiece. Reforging spends flux and hammer durability, rolls a new grade and never repairs existing damage; heavily worn gear may need repairs first. Closing before the final blow returns inputs without a new quality roll.

Metal armor accepts **Tempered** for durability or **Lightened** for a small movement benefit. **Keen** and **Weighted** require weapons. Finishing precision controls its smaller finishing bonus; it does not reroll craftsmanship. Completed work and paid costs remain committed when the station closes.

Sneak-use an ordinary anvil to open the smithing workshop when enabled. Normal use still opens vanilla repair and enchanting.

Material differences and traits are described in [Materials and workmanship](MATERIALS_AND_TRAITS.md). Mining tools, shields and each armor slot have their own intermediate metal shapes.

## Rhythm forging

Press Space once for each gold strike marker as it crosses the fixed green target. All incoming strikes travel along the same rail. Two markers mean two separate timed presses; three mean three. The green target brightens as the active strike approaches. Later beats in a phrase stay fixed even when the first strike is early or late. A new phrase receives a fresh lead-in. Randomness changes the rhythm, not the required key.

Gold, copper and tin are forgiving. Bronze is a gentler starting material; iron is faster and tighter. Steel is more demanding, and diamond/netherite have the fastest cadence and most frequent multi-hit phrases. The perfect band follows the server's actual scoring window. Waiting before the first strike is free; missed beats after starting reduce accuracy. Pressing repeatedly cannot bypass the strike cooldown.

Each accepted strike drives a physical hammer swing. The exact contact frame triggers its clang, a brief warm surface flash and sparks; perfect strikes produce a brighter spark burst. Timing affects craftsmanship through the existing bounded quality roll, rather than guaranteeing a rarity. A perfect double earns the same quality chance as equally accurate singles; its closer spacing makes accuracy harder to maintain. Missing a beat lowers accuracy and therefore the chance of masterwork.

`materialDifficulty` and `rhythmPhrases` can be disabled independently in the existing Fzzy configuration. These options do not change ingredient costs, equipment preservation, or saved item metadata. The default interface remains short; H opens further guidance and Shift expands equipment details.
