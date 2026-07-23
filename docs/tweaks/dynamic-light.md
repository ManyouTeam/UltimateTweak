# 🔦Dynamic Light

## Config

{% code title="tweaks/dynamic-light.yml" %}
```yaml
enabled: true

worlds:
  mode: blacklist
  list: []

# How often held items and the player's position are checked.
check-interval-ticks: 2

# Only players within this distance receive fake light block updates, which costs extra server performance.
# Set 0 to send each light only to its owning player.
view-distance: 0

# Every rule is checked against both the main hand and off hand.
rules:
  torches:
    light-level: 14
    match-item:
      material:
        - torch
        - soul_torch
  lanterns:
    light-level: 15
    match-item:
      material:
        - lantern
        - soul_lantern

```
{% endcode %}

Each rule contains `match-item` and `light-level`. Light levels are clamped between `0` and `15`, and both hands are checked.

For `match-item` option, should use [Match Item Format](../format/match-item-format.md) here.

## Showcase

<figure><img src="../.gitbook/assets/屏幕录制 2026-06-12 234554.gif" alt=""><figcaption></figcaption></figure>
