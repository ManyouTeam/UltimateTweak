# 💎Better Drop Display

Better Drop Display keeps the server-side dropped item unchanged, but renders a settled item as an `ItemDisplay` for each player. Item pickup, merging, despawn time, and other server mechanics therefore continue to use the real item entity.

While an item is moving through the air it is rendered normally. It is changed into an `ItemDisplay` only after it has landed and remained within the configured movement threshold for the configured delay. If it starts moving again, it is rendered as a normal item until it settles again.

{% hint style="warning" %}
Better Drop Display requires Paper, Purpur, or Folia. It is not registered on Spigot because its display-entity library is unavailable there.
{% endhint %}

## Config

{% code title="tweaks/better-drop-display.yml" %}
```yaml
enabled: false

worlds:
  mode: blacklist
  list: []

render:
  transform: fixed

  scale:
    x: 0.65
    y: 0.65
    z: 0.65

  translation:
    x: 0.0
    y: 0.16
    z: 0.0

  rotation:
    x: -90.0
    y: 0.0
    z: 0.0

  random-yaw: true
  full-bright: false
  view-range: 1.0
  interpolation-duration: 0

  shadow:
    radius: 0.15
    strength: 0.35

settling:
  delay-ticks: 10
  movement-threshold: 0.03

label:
  enabled: true
  show-amount: true
  format: "{name} ×{amount}"
  height: 0.55
```
{% endcode %}

## Behavior

* `settling.delay-ticks`: how long the item must remain stable before it is rendered as an `ItemDisplay`. `20` ticks equals one second.
* `settling.movement-threshold`: maximum cumulative position change during the settling period, in blocks. Exceeding it restarts the timer.
* A displayed item that moves beyond the threshold is restored to the normal item rendering and must settle again.
* The display height is calculated from the collision shape below the real item. Snow layers, carpets, slabs, trapdoors, and other non-full blocks are handled automatically.

## Render

* `render.transform`: the item display transform. `fixed` preserves resource-pack 3D models and works with the default flat rotation.
* `render.scale`: model scale on each axis.
* `render.translation`: additional translation after the ground surface has been calculated. The `y` value is extra clearance above that surface.
* `render.rotation`: rotation in degrees. The default `x: -90.0` lays the model flat.
* `render.random-yaw`: gives each dropped entity a stable random horizontal rotation.
* `render.full-bright`: renders the display at maximum block and sky light.
* `render.view-range`: client display view-range multiplier.
* `render.interpolation-duration`: display transformation and position interpolation duration in ticks.
* `render.shadow`: controls the display shadow radius and strength.

## Label

* `label.enabled`: shows the dropped item name above the display.
* `label.show-amount`: appends the current stack amount.
* `label.format`: supports `{name}` and `{amount}`. Native translated names and custom item names are preserved.
* `label.height`: controls the display bounding-box height and therefore the label position.

When dropped stacks merge, the displayed quantity is updated from the real item metadata.

## Showcase

<figure><img src="https://raw.githubusercontent.com/ManyouTeam/UltimateTweak/refs/heads/master/show/DropDisplay.gif" alt=""><figcaption></figcaption></figure>
