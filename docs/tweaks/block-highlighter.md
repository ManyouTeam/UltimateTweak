# ✨Block Highlighter

{% hint style="info" %}
This tweak is provided by TweakExpansion.
{% endhint %}

Block Highlighter incrementally scans nearby blocks and gives matching blocks a colored glow visible only to the player. Each highlight group has its own block list, color, and optional permission.

## Permissions

* `ultimatetweak.block-highlighter.treasure`: grants access to the default `treasure` group.
* `ultimatetweak.block-highlighter.bypass.occlusion`: keeps highlights visible through obstructing blocks when `highlight-through-blocks` is disabled.

Use an empty group permission to make that group available to everyone.

## Config

{% code title="tweaks/block-highlighter.yml" %}
```yaml
enabled: true

worlds:
  mode: blacklist
  list: []

scan:
  range: 12
  blocks-per-tick: 384
  max-highlights: 12
  rescan-delay-ticks: 10

display:
  view-distance: 32

visibility:
  highlight-through-blocks: false
  check-interval-ticks: 2
  ray-trace:
    ignore-passable-blocks: true
    fluid-collision-mode: NEVER
  target-bounds-overrides: {}

highlight-groups:
  treasure:
    permission: "ultimatetweak.block-highlighter.treasure"
    blocks:
      - CHEST
      - TRAPPED_CHEST
      - BARREL
      - ENDER_CHEST
    glow-color: "#FFD54A"

conditions: []
```
{% endcode %}

* `scan.range`: search radius around the player.
* `scan.blocks-per-tick`: maximum number of blocks checked per tick for each incremental scan.
* `scan.max-highlights`: maximum simultaneously highlighted blocks per player.
* `scan.rescan-delay-ticks`: delay before starting a new nearby-block scan.
* `display.view-distance`: maximum distance at which a highlight is sent to the player.
* `visibility.highlight-through-blocks`: whether matching blocks glow through obstructions.
* `visibility.check-interval-ticks`: interval between line-of-sight checks for existing highlights.
* `visibility.ray-trace.ignore-passable-blocks`: excludes passable blocks from occlusion.
* `visibility.ray-trace.fluid-collision-mode`: Bukkit fluid collision mode: `NEVER`, `SOURCE_ONLY`, or `ALWAYS`.
* `visibility.target-bounds-overrides`: optional local `min-x`, `min-y`, `min-z`, `max-x`, `max-y`, and `max-z` bounds for blocks whose reported geometry is incorrect. Values must be between `0.0` and `1.0`.
* `highlight-groups`: maps group IDs to a `permission`, block material list, and hexadecimal `glow-color`.
* `conditions`: conditions that must pass before the tweak can activate. See [Condition Format](../format/condition-format.md).
