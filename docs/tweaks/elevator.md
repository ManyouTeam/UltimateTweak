# ↕️Elevator

{% hint style="info" %}
This tweak is provided by TweakExpansion.
{% endhint %}

Elevator turns configured blocks into vertical transport points. Stand above an elevator block and jump to travel upward, or start sneaking to travel downward. The destination must have enough room for the player.

Vanilla material IDs and IDs from supported custom-block hooks can be used in `blocks`.

## Permissions

* `ultimatetweak.elevator`: use elevators. This is the default value of `permission`.
* `ultimatetweak.elevator.bypass.same-block`: ignore `require-same-block`.
* `ultimatetweak.elevator.bypass.blocks-between`: ignore `allow-blocks-between: false`.

Set `permission` to an empty string to allow every player who passes `conditions`.

## Config

{% code title="tweaks/elevator.yml" %}
```yaml
enabled: true

worlds:
  mode: blacklist
  list: []

permission: "ultimatetweak.elevator"

blocks:
  - "minecraft:iron_block"

max-distance: 64
require-same-block: false
allow-blocks-between: true
cooldown-ticks: 10
center-player: true
protection-check: true

effects:
  enabled: true

conditions: []
```
{% endcode %}

* `blocks`: blocks treated as elevator floors, such as `minecraft:iron_block` or `oraxen:my_elevator`.
* `max-distance`: maximum vertical search distance for the next elevator.
* `require-same-block`: requires the source and destination to resolve to the same block ID.
* `allow-blocks-between`: allows elevator floors to connect through solid blocks. When disabled, only air may be between them.
* `cooldown-ticks`: prevents the arrival movement from immediately triggering another teleport.
* `center-player`: moves the player to the center of the destination block.
* `protection-check`: requires the destination to pass all loaded protection hooks.
* `effects.enabled`: enables departure and arrival particles and sounds.
* `conditions`: conditions that must pass before the tweak can activate. See [Condition Format](../format/condition-format.md).
