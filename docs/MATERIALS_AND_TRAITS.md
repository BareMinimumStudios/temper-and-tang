# Materials and workmanship

Choose a pattern, use its ingredients, and forge it carefully. **Craftsmanship is the larger equipment-quality layer.** Material differences and one optional trait add smaller strengths and trade-offs. Enchantments remain a separate enhancement.

The normal tooltip shows the grade, material and trait names. Hold **Shift** for effects, accuracy and maker information. **H** opens additional help at a workstation. Normal station captions stay short.

## Material choices

These are the default additional workshop effects, separate from the original item's tier. Attack and armor adjustments use the item's authored attribute contributions. Handling applies to attack speed and, for mining tools, block-break speed. Movement applies per worn armor piece or held shield. Existing native material properties and enchantments still apply.

| Material | Durability | Damage | Handling | Armor | Movement |
| --- | ---: | ---: | ---: | ---: | ---: |
| Iron | — | — | — | — | — |
| Bronze | +10% | -3% | — | — | — |
| Steel | +12% | +6% | -4% | +3% | -0.2% |
| Gold | -12% | — | +6% | -2% | +0.2% |
| Copper | -8% | — | +5% | -2% | +0.1% |
| Tin | -10% | — | +3% | -3% | +0.1% |
| Diamond | +6% | — | -2% | — | -0.1% |
| Netherite | +8% | — | -3% | +2% | -0.2% |

Material recognition does not add missing third-party equipment or materials. The original equipment recipes determine what is available. Equipment tags and recognizable item material names establish a profile; otherwise the dominant recognized refined input supplies it. Unknown materials keep their authored stats without an extra material adjustment.

## A single trait

Traits are rolled once at the final shaping blow and saved inside the unquenched workpiece. Closing, cooling, quenching or finishing does not reroll them. A paid reforge can replace quality and the trait together; original wear is preserved.

Below 40% strike accuracy, no trait rolls. At or above that threshold, the chance rises with accuracy, reaching 80% at perfect accuracy. A trait is never guaranteed. Material influences the weighting within the appropriate equipment pool; it does not replace skill.

| Trait | Main benefit | Trade-off |
| --- | --- | --- |
| Sturdy | +10% durability | Weapons: -2% damage, -1% attack speed; mining tools: -2% mining speed; armor/shields: -0.1% movement |
| Honed | +4% damage | -6% durability |
| Balanced | +3% attack speed; mining tools +2% mining speed | -3% durability, -1% weapon damage |
| Reinforced | +3.5% armor, +4% durability | -0.3% movement per piece |
| Supple | +0.4% movement per armor piece | -5% durability |
| Industrious | +6% mining speed | -6% durability, -1% attack speed where applicable |

Weapons use Sturdy, Honed or Balanced. Mining tools use Sturdy, Industrious or Balanced. Armor uses Sturdy, Reinforced or Supple. Shields use Sturdy. Only attributes appropriate to the equipment apply.

## Preservation and configuration

`materialPower` and `traitPower` in Fzzy Config scale the corresponding effects on newly forged or reforged equipment. Zero disables the new effect layer for that work. The chosen strengths are recorded on the item; later configuration changes do not silently rewrite its stamp. Existing unstamped gear stays unchanged.

Finishing preserves craftsmanship, material and trait records. Netherite upgrades retain the trait and rebuild against the new material and base stats. Repeated processing replaces owned modifiers rather than stacking them. Gear too worn to survive the worst allowed reforge result must be repaired first.
