# 🪽Multi Armor

{% hint style="info" %}
This tweak is provided by TweakExpansion.
{% endhint %}

Multi Armor automatically swaps between a chestplate and an Elytra. It equips an Elytra from the player's storage inventory after the configured fall distance, then restores the chestplate after the player lands. The item that was replaced is kept in the same inventory slot for the next swap.

## Permission

`ultimatetweak.multi-armor` is required by default. Set `permission` to an empty string to allow every player who passes `conditions`.

## Config

{% code title="tweaks/multi-armor.yml" %}
```yaml
enabled: true

worlds:
  mode: blacklist
  list: []

permission: "ultimatetweak.multi-armor"

minimum-fall-distance: 4.0
cooldown-ticks: 60

switch-actions: []
conditions: []
```
{% endcode %}

* `minimum-fall-distance`: distance the player must fall before a worn chestplate is replaced with an Elytra.
* `cooldown-ticks`: delay after each successful swap. `60` ticks equals 3 seconds.
* `switch-actions`: actions executed after either direction of swap completes. See [Action Format](../format/action-format.md).
* `conditions`: conditions that must pass before the tweak can activate. See [Condition Format](../format/condition-format.md).
