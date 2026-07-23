# 🛠️Best Tool

## Config

{% code title="tweaks/best-tool.yml" %}
```yaml
enabled: true

worlds:
  mode: blacklist
  list: []

# hotbar: search slots 0-8; inventory: search the entire player storage inventory.
# Please note that use inventory mode will cost ~x5 extra performance, don't recommended to use it.
search-scope: hotbar

# Minimum interval between detections for the same player.
cooldown-ticks: 5

# Candidate score:
# block destroy speed (including efficiency) * multiplier ^ fortune level
# Fortune only receives this bonus when the item is a preferred tool for the block.
fortune-multiplier-per-level: 1.5

conditions: []

# Executed after the held hotbar slot is changed.
switch-actions: []

```
{% endcode %}

* `search-scope`: `hotbar` searches slots 0-8; `inventory` searches the whole player storage inventory.
* `cooldown-ticks`: minimum interval between checks for the same player.
* `fortune-multiplier-per-level`: Fortune level multiplier used when scoring candidate tools.
* `conditions`: conditions that must pass to active this tweak. Should use [Condition Format](../format/condition-format.md) here.
* `switch-actions`: actions executed after the held slot changes. Should use [Action Format](../format/action-format.md) here.

## Showcase

<figure><img src="https://raw.githubusercontent.com/ManyouTeam/UltimateTweak/refs/heads/master/show/BestTools.gif" alt=""><figcaption></figcaption></figure>
