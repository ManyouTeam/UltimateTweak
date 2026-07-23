# 🌱Tree Replant

## Config

{% code title="tweaks/tree-replant.yml" %}
```yaml
enabled: true

worlds:
  mode: blacklist
  list: []

# Wait before checking a newly dropped sapling.
cooldown-ticks: 20

# How often dropped saplings are checked, and how long they may stay airborne.
check-interval-ticks: 20
max-wait-ticks: 60

# Each group contains its own saplings and valid soils.
rules:
  overworld-trees:
    saplings:
      oak:
        match-item:
          material:
            - oak_sapling
        block: minecraft:oak_sapling
      spruce:
        match-item:
          material:
            - spruce_sapling
        block: minecraft:spruce_sapling
      birch:
        match-item:
          material:
            - birch_sapling
        block: minecraft:birch_sapling
      jungle:
        match-item:
          material:
            - jungle_sapling
        block: minecraft:jungle_sapling
      acacia:
        match-item:
          material:
            - acacia_sapling
        block: minecraft:acacia_sapling
      dark-oak:
        match-item:
          material:
            - dark_oak_sapling
        block: minecraft:dark_oak_sapling
      cherry:
        match-item:
          material:
            - cherry_sapling
        block: minecraft:cherry_sapling
      pale-oak:
        match-item:
          material:
            - pale_oak_sapling
        block: minecraft:pale_oak_sapling
      azalea:
        match-item:
          material:
            - azalea
        block: minecraft:azalea
      flowering-azalea:
        match-item:
          material:
            - flowering_azalea
        block: minecraft:flowering_azalea
    valid-soilds:
      - minecraft:dirt
      - minecraft:grass_block
      - minecraft:podzol
      - minecraft:coarse_dirt
      - minecraft:rooted_dirt
      - minecraft:mycelium
      - minecraft:moss_block
      - minecraft:mud

  mangrove:
    saplings:
      mangrove:
        match-item:
          material:
            - mangrove_propagule
        block: minecraft:mangrove_propagule
    valid-soilds:
      - minecraft:mud
      - minecraft:clay

  custom:
    saplings:
      palm:
        match-item:
          items:
            - palm_sapling
        block: craftengine:default:palm_sapling
    valid-soilds:
      - minecraft:dirt
      - minecraft:grass_block
      - minecraft:sand

```
{% endcode %}

Each rule defines sapling items, the block placed for each sapling, and valid soil blocks. Dropped saplings are checked every `check-interval-ticks` until `max-wait-ticks` is reached.

For each rule `sapling.block` and `valid-soilds` option, supports:

* `minecraft:<Minecraft Block ID>`, like minecraft:stone.
* `itemsadder:<NamespaceID>:<Block ID>`, like `itemsadder:blocks:block_1`.
* `oraxen:<Item ID>`, like `oraxen:block_1`.
* `mmoitems:<Block ID>`, like `mmoitems:10`, **block id (is a number, not item id)** is a number been set by your block configs, not item ID.
* `craftengine:<NamespaceID>:<Block Item ID>`, like `craftengine:default::palm_log`.

For each rule `saplings.match-item` option, should use [Match Item Format](../format/match-item-format.md) here.
