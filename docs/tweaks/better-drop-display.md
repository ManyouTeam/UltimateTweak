# 💎Better Drop Display

Better Drop Display keeps the server-side dropped item unchanged, but renders a settled item as an `ItemDisplay` for each player. Item pickup, merging, despawn time, and other server mechanics therefore continue to use the real item entity.

Each item is assigned to the highest-priority matching model profile. A profile controls its transform, dimensions, landing pose, clearance, lighting, shadow, and view range. Profiles use UltimateTweak's normal `match-item` format, including external item IDs.

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
  landing-duration-ticks: 8

  models:
    thin-blocks:
      priority: 400
      match-item:
        material-pattern:
          - "*_slab"
          - "*_carpet"
      transform: fixed
      scale: { x: 0.55, y: 0.55, z: 0.55 }
      rotation: { x: 0.0, y: 0.0, z: 0.0 }
      landing-start:
        rotation: { x: -18.0, y: 0.0, z: 12.0 }
        offset-y: 0.08
      dimensions: { x: 1.0, y: 0.125, z: 1.0 }
      translation: { x: 0.0, z: 0.0 }
      clearance: 0.01
      random-yaw: true

    blocks:
      priority: 300
      match-item:
        is-block: true
      transform: fixed
      scale: { x: 0.48, y: 0.48, z: 0.48 }
      rotation: { x: 0.0, y: 0.0, z: 0.0 }
      dimensions: { x: 1.0, y: 1.0, z: 1.0 }
      clearance: 0.01

    default:
      priority: -1
      transform: fixed
      scale: { x: 0.65, y: 0.65, z: 0.65 }
      rotation: { x: -90.0, y: 0.0, z: 0.0 }
      dimensions: { x: 1.0, y: 0.08, z: 1.0 }
      clearance: 0.02

  full-bright: false
  view-range: 1.0

  shadow:
    radius: 0.15
    strength: 0.35

settling:
  delay-ticks: 6
  enter-movement-threshold: 0.02
  exit-movement-threshold: 0.05

label:
  enabled: true
  show-amount: true
  format: "{name} ×{amount}"
  height: 0.55
```
{% endcode %}

## Behavior

* `settling.delay-ticks`: how long the item must remain stable before it is rendered as an `ItemDisplay`. `20` ticks equals one second.
* `settling.enter-movement-threshold`: maximum displacement allowed while waiting to display the item.
* `settling.exit-movement-threshold`: movement required to restore a displayed item to vanilla rendering. Keeping this larger than the enter threshold prevents flicker from tiny corrections.
* A displayed item that moves beyond the exit threshold is restored to normal item rendering and must settle again.
* The display height combines the collision surface below the item with the selected profile's transformed dimensions. Snow layers, carpets, slabs, trapdoors, and other non-full blocks are handled automatically.

## Render

* `render.landing-duration-ticks`: client interpolation time from `landing-start` to the final profile transform.
* `render.models.<id>.priority`: profile matching order. Higher values are tested first; `default` is always the fallback.
* `render.models.<id>.match-item`: standard [Match Item Format](../format/match-item-format.md). It is omitted from `default`.
* `transform`: the item display transform. `fixed` preserves resource-pack 3D models.
* `scale`, `rotation`, and `translation`: final model transformation. Translation X/Z is applied after positioning.
* `landing-start`: initial rotation and extra height before client interpolation reaches the final pose.
* `dimensions`: unscaled model dimensions used to keep the rotated model above its support surface. Custom resource-pack models should override these values.
* `clearance`: final gap in blocks between the transformed model and support surface.
* `random-yaw`: gives each dropped entity a stable random horizontal rotation.
* `full-bright`, `view-range`, and `shadow`: may be inherited from `render` or overridden by a profile.

Profiles are resolved from the real Bukkit item on its entity thread. Packet handlers only read the resolved profile, which keeps custom-item hooks and Folia region access thread-safe. Existing configurations without `render.models` continue to use their original single transform.

## Label

* `label.enabled`: shows the dropped item name above the display.
* `label.show-amount`: appends the current stack amount.
* `label.format`: supports `{name}` and `{amount}`. Native translated names and custom item names are preserved.
* `label.height`: controls the display bounding-box height and therefore the label position.

When dropped stacks merge, the displayed quantity is updated from the real item metadata.

## Showcase

<figure><img src="https://raw.githubusercontent.com/ManyouTeam/UltimateTweak/refs/heads/master/show/DropDisplay.gif" alt=""><figcaption></figcaption></figure>
