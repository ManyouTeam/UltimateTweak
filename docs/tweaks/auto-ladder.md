# 🪜Auto Ladder

{% hint style="info" %}
This tweak is provided by TweakExpansion.
{% endhint %}

Auto Ladder accelerates climbing and sliding after the player has climbed a short distance normally. Look up to climb, look down to slide, or sneak to stop the automatic movement. At the top of a climbable column, the optional top-exit boost pushes the player onto the floor ahead.

## Permission

`ultimatetweak.auto-ladder` is required by default. Set `permission` to an empty string to allow every player who passes `conditions`.

## Config

{% code title="tweaks/auto-ladder.yml" %}
```yaml
enabled: true

worlds:
  mode: blacklist
  list: []

permission: "ultimatetweak.auto-ladder"

activation-distance: 1.0

movement:
  climb-speed: 0.50
  slide-speed: 0.60

top-exit:
  enabled: true
  forward-speed: 0.38
  vertical-speed: 0.28

look:
  activation-angle: 30.0
  release-angle: 15.0

include-scaffolding: false

conditions: []
```
{% endcode %}

* `activation-distance`: distance the player must first climb normally. Set it to `0` to activate immediately.
* `movement.climb-speed` and `movement.slide-speed`: vertical velocity in blocks per tick.
* `top-exit`: controls the forward and upward boost at the top of a climbable column.
* `look.activation-angle`: how far the player must look up or down to start moving.
* `look.release-angle`: lower threshold used to keep the current direction stable near the activation angle.
* `include-scaffolding`: whether scaffolding is controlled in addition to vanilla climbable blocks.
* `conditions`: conditions that must pass before the tweak can activate. See [Condition Format](../format/condition-format.md).
