# 🌲Tree Cutter

## Config

{% code title="tweaks/tree-cuter.yml" %}
```yaml
enabled: true

# Minimum interval between multiblock detections for the same player.
cooldown-ticks: 5

# Maximum number of blocks broken in the same tick.
# Set smaller value if you are running large servers.
blocks-per-tick: 32

require-shift: false

match-item:
  trigger:
    material-tag:
      - 'axes'
  leaf-break:
    has-enchants:
      - 'custom:tree_shears'

animation:
  # If this lag your server, try to disable it.
  enabled: true
  # random, same-as-player, away-from-player
  direction: random
  glow: true
  glow-color: '#FFFFFF'
  duration-ticks: 30
  interval-ticks: 1
  view-distance: 24
  # Will cost extra server performance.
  fall-damage:
    enabled: false
    players: true
    entities: true
    damage: 6.0
    min-angle: 15.0
    hit-radius: 0.5
    check-interval-ticks: 2

damage-glow:
  enabled: true
  hide-breaking-block: true
  glow-color: '#FFFFFF'
  duration-ticks: 40
  view-distance: 24

# Each log compounds the mining time by percent-per-log, limited by max-percent.
mining-time:
  enabled: true
  percent-per-log: 100.0
  max-percent: 5000.0

damage-actions: []

break-actions:
  1:
    type: sound
    sound: entity.zombie.break_wooden_door
    volume: 1.0
    pitch: 1.0

conditions: []
```
{% endcode %}

* `require-shift`: whether the player must sneak.
* `match-item.trigger`: item rules that can trigger Tree Cutter. Should use [Match Item Format](../format/match-item-format.md) here.
* `match-item.leaf-break`: breaks leaves when the held item matches these rules. Should use [Match Item Format](../format/match-item-format.md) here.
* `animation`: falling direction, glow, duration, and optional falling-tree damage.
* `damage-glow`: visual feedback while the tree is being mined.
* `mining-time`: increases mining time based on the number of logs.
* `damage-actions` / `break-actions`: actions run while mining or after completion. Should use [Action Format](../format/action-format.md) here. Supported placeholders:
  * `{log-amount}`
  * `{leaf-amount}`
  * `{tree-amount}`
  * `{tree-id}`
  * `{block-amount}`
  * `{mining-block-amount}`
* `conditions`: conditions that must pass to active this tweak. Should use [Condition Format](../format/condition-format.md) here.

## Limitation

* The accumulation of **Mining Time** can only be based on the time of the block being mined by the player. For mangrove in the swamp, if the player is destroying mangrove roots, it will also be destroyed very quickly.
* Due to the fact that we can only determine whether a player is digging trees after the client starts digging blocks, enabling the **Mining Time** feature may result in the server attempting to change the player's digging speed only after the client has already started digging. Therefore, you may find that the player starts digging faster and then follows the new digging time, which is a limitation of the game itself and cannot be fixed.
* In order to display the cracks in the blocks that players are destroying, the Damage Glow feature will not glow the blocks that players are mining, which is a limitation of the game itself and cannot be fixed.
* When using tools such as diamond axes (which can quickly cut down mushrooms) destroy custom trees from CraftEngine, there may be a situation where blocks suddenly disappear but immediately recover.  This is a limitation of the game itself and cannot be fixed.

## Tree Determine

{% hint style="warning" %}
Only custom trees from **CustomEngine** plugin is supported!
{% endhint %}

Tree recognition rules live in `tree_determine_settings/`. Defaults cover common Overworld trees, Nether fungi, and a CraftEngine palm example.

Example:

```yaml
logs:
  - minecraft:acacia_log
  - minecraft:acacia_wood
leaves:
  - minecraft:acacia_leaves
radius: 6
height: 24
min-log-amount: 4
max-log-amount: 80
min-leaf-amount: 16
max-leaf-amount: 256
player-leave: false
```

For `logs` and `leaves` option, support:

* `minecraft:<Minecraft Block ID>`, like minecraft:stone.
* `itemsadder:<NamespaceID>:<Block ID>`, like `itemsadder:blocks:block_1`.
* `oraxen:<Item ID>`, like `oraxen:block_1`.
* `mmoitems:<Block ID>`, like `mmoitems:10`, **block id (is a number, not item id)** is a number been set by your block configs, not item ID.
* `craftengine:<NamespaceID>:<Block Item ID>`, like `craftengine:default::palm_log`.

Run `/ut debug <tree definition ID>` and try mining a tree to see why a definition passes or fails and can the block ID.

## Showcase

<figure><img src="../.gitbook/assets/屏幕录制 2026-06-12 233951.gif" alt=""><figcaption></figcaption></figure>
