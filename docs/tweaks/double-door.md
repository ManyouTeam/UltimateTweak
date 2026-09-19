# 🚪Double Door

Double Door opens or closes adjacent matching doors together. Its optional display animation can also be applied to a single door.

{% hint style="info" %}
{% endhint %}

## Config

{% code title="tweaks/double-door.yml" %}
```yaml
enabled: true

worlds:
  mode: blacklist
  list: []

permission: "ultimatetweak.double-door"

animation:
  enabled: true
  single-door: false
  duration-ticks: 16
  interval-ticks: 1
  view-distance: 24

conditions: []
```
{% endcode %}

* `permission`: permission required to use Double Door. Leave it blank to disable the permission check.
* `animation.enabled`: enables the display-entity door animation.
* `animation.single-door`: also animates doors that do not have an adjacent matching door.
* `animation.duration-ticks`: total animation duration.
* `animation.interval-ticks`: interval between animation updates.
* `animation.view-distance`: maximum distance at which players receive the animation.
* `conditions`: conditions that must pass before the tweak can activate. See [Condition Format](../format/condition-format.md).
