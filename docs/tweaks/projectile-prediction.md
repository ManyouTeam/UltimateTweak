# 🏹Projectile Prediction

{% hint style="info" %}
This tweak is provided by TweakExpansion.
{% endhint %}

Projectile Prediction renders a packet-only trajectory made of translucent, camera-facing square displays. The squares grow toward the predicted impact point. Bows are previewed while being drawn. Sneak to preview a loaded crossbow, trident, snowball, egg, ender pearl, splash or lingering potion, or experience bottle.

The path is white by default. When the simulation predicts an entity hit, the entire path changes to yellow.

## Permission

`ultimatetweak.projectile-prediction` is required by default. Set `permission` to an empty string to allow every player who passes `conditions`.

## Config

{% code title="tweaks/projectile-prediction.yml" %}
```yaml
enabled: true

worlds:
  mode: blacklist
  list: []

permission: "ultimatetweak.projectile-prediction"

velocity:
  bow: 3.0
  crossbow: 3.15
  trident: 2.5
  light-throw: 1.5
  heavy-throw: 0.5

collision:
  entity-hitbox-expansion: 0.3

display:
  point-spacing: 0.85
  start-scale: 0.08
  end-scale: 0.75
  glyph: "■"
  color: "#FFFFFF"
  hit-color: "#FFD54A"
  opacity: 112
  glowing: true
  interpolation-ticks: 2
  view-range: 1.0

performance:
  update-interval-ticks: 2
  idle-refresh-ticks: 10
  max-simulation-ticks: 80
  simulation-substeps: 3
  max-points: 64
  max-renders-per-tick: 4
  max-ray-traces-per-tick: 480

conditions: []
```
{% endcode %}

* `velocity`: initial speed used to simulate each projectile category.
* `collision.entity-hitbox-expansion`: extra margin added when detecting predicted entity impacts.
* `display.point-spacing`: approximate distance between rendered path points.
* `display.start-scale` and `end-scale`: display size at the beginning and end of the path.
* `display.glyph`: square character rendered by each packet-only text display.
* `display.color`: normal path color.
* `display.hit-color`: path color used when an entity will be hit.
* `display.opacity`: square opacity from `0` to `255`.
* `display.glowing`: enables the entity glow outline. Its glow color follows `color` and changes to `hit-color` for predicted entity hits.
* `display.interpolation-ticks`: client interpolation duration when the path moves.
* `display.view-range`: display entity view-range multiplier.
* `performance.update-interval-ticks`: interval between active trajectory updates.
* `performance.idle-refresh-ticks`: interval used when no supported projectile is being previewed.
* `performance.max-simulation-ticks`, `simulation-substeps`, and `max-points`: bound the detail and length of each simulation.
* `performance.max-renders-per-tick` and `max-ray-traces-per-tick`: server-wide work limits.
* `conditions`: conditions that must pass before the tweak can activate. See [Condition Format](../format/condition-format.md).
