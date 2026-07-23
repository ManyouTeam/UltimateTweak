# ⛏️Vein Mine

{% hint style="info" %}
Since Vein Mine almost share same code from Tree Cutter, so, please also view **Limitation** section from this [page](tree-cutter.md).
{% endhint %}

## Config

{% code title="tweaks/vein-mine.yml" %}
```yaml
enabled: true

# Minimum interval between multiblock detections for the same player.
cooldown-ticks: 5

# Maximum number of blocks broken in the same tick.
# Set smaller value if you are running large servers.
blocks-per-tick: 32

require-shift: true

rules:
  ores:
    match-item:
      material-tag:
        - 'pickaxes'
    match-block:
      - 'minecraft:coal_ore'
      - 'minecraft:deepslate_coal_ore'
      - 'minecraft:iron_ore'
      - 'minecraft:deepslate_iron_ore'
      - 'minecraft:copper_ore'
      - 'minecraft:deepslate_copper_ore'
      - 'minecraft:gold_ore'
      - 'minecraft:deepslate_gold_ore'
      - 'minecraft:redstone_ore'
      - 'minecraft:deepslate_redstone_ore'
      - 'minecraft:emerald_ore'
      - 'minecraft:deepslate_emerald_ore'
      - 'minecraft:lapis_ore'
      - 'minecraft:deepslate_lapis_ore'
      - 'minecraft:diamond_ore'
      - 'minecraft:deepslate_diamond_ore'
      - 'minecraft:nether_gold_ore'
      - 'minecraft:nether_quartz_ore'

search:
  # Whether diagonally touching blocks belong to the same vein.
  diagonal: true
  max-blocks: 64

damage-glow:
  enabled: true
  hide-breaking-block: true
  glow-color: '#FFFFFF'
  duration-ticks: 40
  view-distance: 24

# Each additional block increases mining time by percent-per-block, limited by max-percent.
mining-time:
  enabled: true
  percent-per-block: 100.0
  max-percent: 5000.0

damage-actions: []

break-actions: []

conditions: []
```
{% endcode %}

* `require-shift`: requires sneaking by default.
* `search.diagonal`: includes diagonally touching blocks in the same vein.
* `search.max-blocks`: maximum blocks handled in one operation.
* `mining-time`: increases mining time for additional blocks.
* `break-actions`: Supported placeholders:
  * `{block}`
  * `{block-amount}`
  * `{mining-block-amount}`

Each entry under `rules` associates item matching rules with block IDs:

```yaml
rules:
  ores:
    match-item:
      material-tag:
        - 'pickaxes'
    match-block:
      - 'minecraft:diamond_ore'
      - 'minecraft:deepslate_diamond_ore'
```

For `match-item` option, should use [Match Item Format](../format/match-item-format.md) here.

For `match-block` option, supports:

* `minecraft:<Minecraft Block ID>`, like minecraft:stone.
* `itemsadder:<NamespaceID>:<Block ID>`, like `itemsadder:blocks:block_1`.
* `oraxen:<Item ID>`, like `oraxen:block_1`.
* `mmoitems:<Block ID>`, like `mmoitems:10`, **block id (is a number, not item id)** is a number been set by your block configs, not item ID.
* `craftengine:<NamespaceID>:<Block Item ID>`, like `craftengine:default::palm_log`.

## Showcase

<figure><img src="https://raw.githubusercontent.com/ManyouTeam/UltimateTweak/refs/heads/master/show/VeinMine.gif" alt=""><figcaption></figcaption></figure>
