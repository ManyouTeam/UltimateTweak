# 🧭Guide

{% hint style="info" %}
This tweak is provided by TweakExpansion.
{% endhint %}

Guide places packet-only, camera-relative markers on the player's screen. Markers show their name, distance, and vertical direction. Targets outside the view, including targets behind the player, stay at a screen edge.

The tweak can display the player's bed spawn and last death location automatically. Administrators can also store custom targets in each player's persistent data.

## Commands and permission

The default management permission is `ultimatetweak.guide.admin`. The player being managed must be online.

| Command | Description |
| --- | --- |
| `/ut guide set <player> <id> <x> <y> <z> [world] [name]` | Creates or updates a target at specified coordinates. The target player's current world and the ID are used when the optional values are omitted. |
| `/ut guide here <player> <id> [name]` | Creates or updates a target at the command sender's location. This command must be run by a player. |
| `/ut guide remove <player> <id>` | Removes one custom target. |
| `/ut guide clear <player>` | Removes all custom targets from a player. |
| `/ut guide list <player>` | Lists a player's custom target IDs. |
| `/ut guide hide` | Hides all Guide markers for yourself, including bed spawn, last death, and administrator targets. The preference is saved across reconnects. |
| `/ut guide show` | Shows all Guide markers for yourself again. |
| `/ut guide hide <player>` / `/ut guide show <player>` | Hides or shows every marker for an online player. Requires the Guide admin permission. |

Players can use `hide` and `show` with the `ultimatetweak.guide.toggle` permission (granted to everyone by default). Managing another player's markers still requires `ultimatetweak.guide.admin`.

The `toggle-permission` option changes the permission required to hide or show your own markers. `permission` continues to control management of another player's targets and markers.

## Config

{% code title="tweaks/guide.yml" %}
```yaml
enabled: true

permission: "ultimatetweak.guide.admin"
toggle-permission: "ultimatetweak.guide.toggle"

worlds:
  mode: blacklist
  list: []

targets:
  maximum-visible: 5
  maximum-saved-per-player: 32

built-in:
  bed-spawn:
    enabled: true
    count-towards-limit: true
    name: "&aBed Spawn"
  last-death:
    enabled: true
    count-towards-limit: true
    name: "&cLast Death"

display:
  update-interval-ticks: 1
  anchor-distance: 6.0
  horizontal-angle-limit-degrees: 36
  vertical-angle-limit-degrees: 16
  tracking-response: 0.65
  stack-spacing: 0.16
  position-interpolation-ticks: 1
  motion-prediction-factor: 1.0
  near-scale: 0.85
  far-scale: 0.55
  far-distance: 500
  see-through: true
  text-shadow: false
  background-color: "#70000000"
  formats:
    admin: "&b◆ &f{name}\n&7{distance}m {vertical}"
    bed-spawn: "&a⌂ &f{name}\n&7{distance}m {vertical}"
    last-death: "&c✦ &f{name}\n&7{distance}m {vertical}"
```
{% endcode %}

* `targets.maximum-visible`: maximum number of targets that count toward the on-screen limit.
* `targets.maximum-saved-per-player`: maximum number of administrator-created targets stored per player.
* `built-in.*.count-towards-limit`: whether that built-in marker uses one of the visible target slots.
* `display.update-interval-ticks`: marker update interval.
* `display.anchor-distance`: distance of the camera-facing plane onto which markers are projected.
* `display.horizontal-angle-limit-degrees` and `vertical-angle-limit-degrees`: screen-edge limits for off-screen targets.
* `display.tracking-response`: marker response speed. `1.0` reacts immediately; lower values glide more slowly.
* `display.stack-spacing`: vertical separation between multiple markers.
* `display.position-interpolation-ticks`: client position interpolation duration. The default `1` smooths movement between server ticks and prevents sprinting jitter; `0` disables interpolation.
* `display.motion-prediction-factor`: horizontal movement prediction multiplier. The default `1.0` predicts one configured update window; `0` disables prediction.
* `display.near-scale`, `far-scale`, and `far-distance`: scale markers according to target distance.
* `display.see-through`: renders marker text through blocks.
* `display.text-shadow`: enables the vanilla TextDisplay glyph shadow. It is disabled by default because a moving shadow can make the marker look doubled or blurred.
* `display.background-color`: marker background in ARGB hexadecimal format.
* `display.formats`: supports `{name}`, `{distance}`, and `{vertical}`.
