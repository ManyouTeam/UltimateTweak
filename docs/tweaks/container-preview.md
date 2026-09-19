# 👁️Container Preview

{% hint style="info" %}
This tweak is provided by TweakExpansion.
{% endhint %}

Container Preview shows the contents of the container the player is looking at. Supported processing blocks can also show live furnace, brewing stand, and beehive status below the inventory panel. The preview uses packet displays and does not create persistent display entities.

## Permission

`ultimatetweak.container-preview` is required by default. Set `permission` to an empty string to allow every player who passes `conditions`.

## Config

{% code title="tweaks/container-preview.yml" %}
```yaml
enabled: true

worlds:
  mode: blacklist
  list: []

permission: "ultimatetweak.container-preview"
look-distance: 8.0
scan-interval-ticks: 2
protection-check: true

display:
  scale: 1.0
  distance: 1.5
  show-amounts: true
  show-durability: true
  background-color: "50000000"
  max-rows: 6
  status:
    enabled: true
    bar-length: 10
    bar-filled: "&a█"
    bar-partial: "&a{char}"
    bar-empty: "&8░"
    furnace-format: "{lang:container-preview-status-furnace}"
    brewing-format: "{lang:container-preview-status-brewing}"
    beehive-format: "{lang:container-preview-status-beehive}"

conditions: []
```
{% endcode %}

* `look-distance`: maximum targeting distance in blocks.
* `scan-interval-ticks`: interval between target and inventory refreshes.
* `protection-check`: hides the preview when any loaded protection hook denies access.
* `display.scale`: size multiplier for the complete panel.
* `display.distance`: reference distance used to place and proportionally scale the panel in front of nearby blocks.
* `display.show-amounts`: shows stack amounts on item entries.
* `display.show-durability`: shows the vanilla-style durability bar beneath damaged items, including the original green-to-red color transition.
* `display.background-color`: panel color in ARGB hexadecimal format.
* `display.max-rows`: maximum number of inventory rows displayed.
* `display.status.enabled`: shows processing information for supported machine inventories.
* `display.status.bar-length`: number of cells in the progress bar.
* `display.status.bar-filled`, `bar-partial`, and `bar-empty`: progress bar characters and colors. `{char}` is available in `bar-partial`.
* `display.status.furnace-format`: supports `{bar}`, `{percent}`, `{current}`, `{total}`, `{remaining}`, and `{fuel}`.
* `display.status.brewing-format`: supports the same general placeholders; brewing fuel uses its native charge level.
* `display.status.beehive-format`: additionally supports `{honey}`, `{max_honey}`, `{bees}`, and `{max_bees}`.
* `conditions`: conditions that must pass before the tweak can activate. See [Condition Format](../format/condition-format.md).
