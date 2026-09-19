# 📦Better Drop Display

{% hint style="info" %}
This tweak is provided by TweakExpansion.
{% endhint %}

Better Drop Display replaces the visual model of dropped items with animated packet displays. The original item entity remains authoritative for gravity, movement, merging, despawning, hopper collection, and player pickup.

Items can tumble while moving, settle naturally on the ground, show a name and amount label, and shrink toward the player when picked up. Model profiles allow different transforms and dimensions for selected items.

## Config

{% code title="tweaks/better-drop-display.yml" %}
```yaml
enabled: true

worlds:
  mode: blacklist
  list: []

limits:
  view-distance: 32
  max-tracked: 2048
  max-per-viewer: 128
  max-per-chunk: 64

motion:
  tumble: true
  update-interval-ticks: 1
  interpolation-ticks: 1
  airborne-degrees-per-block: 300
  ground-degrees-per-block: 360
  spin:
    x: 160
    y: 120
    z: 100
  variance: 0.2
  velocity-influence: 0.35
  submerged-multiplier: 0.35

settling:
  delay-ticks: 4
  enter-movement-threshold: 0.03
  wake-threshold: 0.04
  face-attraction: 0.35
  moving-face-attraction: 0.08
  alignment-degrees: 0.5
  poll-interval-ticks: 10

label:
  enabled: true
  show-amount: true
  format: "&f{name} &7×{amount}"
  height: 0.55

pickup-animation:
  duration-ticks: 4
  final-scale: 0.15

render:
  transform: fixed
  landing-duration-ticks: 4
  random-yaw: true
  full-bright: false
  shadow:
    radius: 0.15
    strength: 0.35
  view-range: 1.0
  models:
    blocks:
      priority: 10
      match-item:
        is-block: true
      display: item
      landing-mode: natural
      transform: fixed
      scale:
        x: 0.65
        y: 0.65
        z: 0.65
      dimensions:
        x: 1.0
        y: 1.0
        z: 1.0
      center:
        x: 0.0
        y: 0.0
        z: 0.0
      translation:
        x: 0.0
        z: 0.0
      clearance: 0.02
      rotation:
        x: 0.0
        y: 0.0
        z: 0.0
      landing-start:
        rotation:
          x: 0.0
          y: 0.0
          z: 0.0
        offset-y: 0.0
    default:
      display: item
      landing-mode: natural
      transform: fixed
      scale:
        x: 0.65
        y: 0.65
        z: 0.65
      dimensions:
        x: 1.0
        y: 1.0
        z: 0.0625
      center:
        x: 0.0
        y: 0.0
        z: 0.0
      translation:
        x: 0.0
        z: 0.0
      clearance: 0.02
      rotation:
        x: -90.0
        y: 0.0
        z: 0.0
      landing-start:
        rotation:
          x: -90.0
          y: 0.0
          z: 0.0
        offset-y: 0.0
```
{% endcode %}

### Limits

* `view-distance`: maximum distance at which a player receives a display.
* `max-tracked`: server-wide tracked-item limit.
* `max-per-viewer`: maximum displays sent to one player.
* `max-per-chunk`: maximum tracked items in one chunk.

### Motion and settling

* `motion.tumble`: enables animated item rotation.
* `motion.update-interval-ticks` and `interpolation-ticks`: control update frequency and client interpolation.
* `motion.airborne-degrees-per-block`: compatibility baseline; `300` gives a `1.0` multiplier to the configured spin values.
* `motion.ground-degrees-per-block`: rolling rotation rate while touching the ground.
* `motion.spin`: X, Y, and Z spin rates.
* `motion.variance`: random per-item spin variation.
* `motion.velocity-influence`: how strongly movement affects rotation.
* `motion.submerged-multiplier`: motion multiplier while submerged.
* `settling.delay-ticks`: delay before a nearly stationary item starts settling.
* `settling.enter-movement-threshold` and `wake-threshold`: speed thresholds for entering and leaving the settled state.
* `settling.face-attraction` and `moving-face-attraction`: strength used to rotate an item toward a stable landing face.
* `settling.alignment-degrees`: angular tolerance for considering an item aligned.
* `settling.poll-interval-ticks`: interval between checks for settled items.

### Label and pickup

* `label.show-amount`: makes `{amount}` available in the main label.
* `label.format`: supports `{name}` and `{amount}`.
* `label.height`: vertical label offset.
* `pickup-animation.duration-ticks`: length of the animation toward the collector.
* `pickup-animation.final-scale`: display scale at the end of pickup.

### Rendering and model profiles

* `render.transform`: default Bukkit item display transform.
* `render.landing-duration-ticks`: transition length when an item settles.
* `render.random-yaw`: gives newly tracked items a random horizontal rotation.
* `render.full-bright`: renders models at full brightness.
* `render.shadow.radius` and `strength`: display shadow settings.
* `render.view-range`: display entity view-range multiplier.
* `render.models`: model profiles checked by `priority`. The `default` profile is used when no other profile matches.
* `match-item`: selects items for a profile using [Match Item Format](../format/match-item-format.md).
* `display`: display type used by the profile. The defaults use `item` to preserve resource-pack item models.
* `landing-mode`: controls how the model chooses a resting face.
* `scale`, `dimensions`, `center`, `translation`, `clearance`, and `rotation`: describe the model's geometry and transform. Ground placement automatically includes the vanilla item-display transform scale, so `fixed` models no longer float above their calculated support surface.
* `landing-start.rotation` and `landing-start.offset-y`: starting transform for the landing transition.
