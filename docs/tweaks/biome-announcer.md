# 🌍Biome Announcer

## Config

```yaml
enabled: true

# Empty list means all biomes.
# Supports names like plains or minecraft:plains.
biomes: []

# How often players' current biomes are checked.
check-interval-ticks: 20

# All conditions must pass before sending the biome change message.
conditions: []

# Default message.
message: "[title]&e{lang:biome-announcer}[/title]"

# Executed after entering a different biome.
# You can use {biome}, {from-biome}, and {to-biome} in action strings.
biome-change-actions:
  1:
    type: sound
    sound: entity.experience_orb.pickup
    volume: 1.0
    pitch: 1.2

# Optional per-biome messages. These override message above.
# Supports names like plains or minecraft:plains.
biome-messages:
  "terralith:alpha_islands": "[title]&e{lang:biome-announcer-terralith-alpha_islands}[/title]"
  "terralith:alpha_islands_winter": "[title]&e{lang:biome-announcer-terralith-alpha_islands_winter}[/title]"
  "terralith:alpine_grove": "[title]&e{lang:biome-announcer-terralith-alpine_grove}[/title]"
  "terralith:alpine_highlands": "[title]&e{lang:biome-announcer-terralith-alpine_highlands}[/title]"
  "terralith:amethyst_canyon": "[title]&e{lang:biome-announcer-terralith-amethyst_canyon}[/title]"
  "terralith:amethyst_rainforest": "[title]&e{lang:biome-announcer-terralith-amethyst_rainforest}[/title]"
  "terralith:ancient_sands": "[title]&e{lang:biome-announcer-terralith-ancient_sands}[/title]"
  "terralith:arid_highlands": "[title]&e{lang:biome-announcer-terralith-arid_highlands}[/title]"
  "terralith:ashen_savanna": "[title]&e{lang:biome-announcer-terralith-ashen_savanna}[/title]"
  "terralith:basalt_cliffs": "[title]&e{lang:biome-announcer-terralith-basalt_cliffs}[/title]"
  "terralith:birch_taiga": "[title]&e{lang:biome-announcer-terralith-birch_taiga}[/title]"
  "terralith:blooming_plateau": "[title]&e{lang:biome-announcer-terralith-blooming_plateau}[/title]"
  "terralith:blooming_valley": "[title]&e{lang:biome-announcer-terralith-blooming_valley}[/title]"
  "terralith:brushland": "[title]&e{lang:biome-announcer-terralith-brushland}[/title]"
  "terralith:bryce_canyon": "[title]&e{lang:biome-announcer-terralith-bryce_canyon}[/title]"
  "terralith:caldera": "[title]&e{lang:biome-announcer-terralith-caldera}[/title]"
  "terralith:cloud_forest": "[title]&e{lang:biome-announcer-terralith-cloud_forest}[/title]"
  "terralith:cold_shrubland": "[title]&e{lang:biome-announcer-terralith-cold_shrubland}[/title]"
  "terralith:desert_oasis": "[title]&e{lang:biome-announcer-terralith-desert_oasis}[/title]"
  "terralith:desert_spires": "[title]&e{lang:biome-announcer-terralith-desert_spires}[/title]"
  "terralith:emerald_peaks": "[title]&e{lang:biome-announcer-terralith-emerald_peaks}[/title]"
  "terralith:forested_highlands": "[title]&e{lang:biome-announcer-terralith-forested_highlands}[/title]"
  "terralith:fractured_savanna": "[title]&e{lang:biome-announcer-terralith-fractured_savanna}[/title]"
  "terralith:frozen_cliffs": "[title]&e{lang:biome-announcer-terralith-frozen_cliffs}[/title]"
  "terralith:glacial_chasm": "[title]&e{lang:biome-announcer-terralith-glacial_chasm}[/title]"
  "terralith:granite_cliffs": "[title]&e{lang:biome-announcer-terralith-granite_cliffs}[/title]"
  "terralith:gravel_beach": "[title]&e{lang:biome-announcer-terralith-gravel_beach}[/title]"
  "terralith:gravel_desert": "[title]&e{lang:biome-announcer-terralith-gravel_desert}[/title]"
  "terralith:haze_mountain": "[title]&e{lang:biome-announcer-terralith-haze_mountain}[/title]"
  "terralith:highlands": "[title]&e{lang:biome-announcer-terralith-highlands}[/title]"
  "terralith:hot_shrubland": "[title]&e{lang:biome-announcer-terralith-hot_shrubland}[/title]"
  "terralith:ice_marsh": "[title]&e{lang:biome-announcer-terralith-ice_marsh}[/title]"
  "terralith:jungle_mountains": "[title]&e{lang:biome-announcer-terralith-jungle_mountains}[/title]"
  "terralith:lavender_forest": "[title]&e{lang:biome-announcer-terralith-lavender_forest}[/title]"
  "terralith:lavender_valley": "[title]&e{lang:biome-announcer-terralith-lavender_valley}[/title]"
  "terralith:lush_valley": "[title]&e{lang:biome-announcer-terralith-lush_valley}[/title]"
  "terralith:mirage_isles": "[title]&e{lang:biome-announcer-terralith-mirage_isles}[/title]"
  "terralith:moonlight_grove": "[title]&e{lang:biome-announcer-terralith-moonlight_grove}[/title]"
  "terralith:moonlight_valley": "[title]&e{lang:biome-announcer-terralith-moonlight_valley}[/title]"
  "terralith:mountain_steppe": "[title]&e{lang:biome-announcer-terralith-mountain_steppe}[/title]"
  "terralith:orchid_swamp": "[title]&e{lang:biome-announcer-terralith-orchid_swamp}[/title]"
  "terralith:painted_mountains": "[title]&e{lang:biome-announcer-terralith-painted_mountains}[/title]"
  "terralith:red_oasis": "[title]&e{lang:biome-announcer-terralith-red_oasis}[/title]"
  "terralith:rocky_jungle": "[title]&e{lang:biome-announcer-terralith-rocky_jungle}[/title]"
  "terralith:rocky_mountains": "[title]&e{lang:biome-announcer-terralith-rocky_mountains}[/title]"
  "terralith:rocky_shrubland": "[title]&e{lang:biome-announcer-terralith-rocky_shrubland}[/title]"
  "terralith:sakura_grove": "[title]&e{lang:biome-announcer-terralith-sakura_grove}[/title]"
  "terralith:sakura_valley": "[title]&e{lang:biome-announcer-terralith-sakura_valley}[/title]"
  "terralith:sandstone_valley": "[title]&e{lang:biome-announcer-terralith-sandstone_valley}[/title]"
  "terralith:savanna_badlands": "[title]&e{lang:biome-announcer-terralith-savanna_badlands}[/title]"
  "terralith:savanna_slopes": "[title]&e{lang:biome-announcer-terralith-savanna_slopes}[/title]"
  "terralith:scarlet_mountains": "[title]&e{lang:biome-announcer-terralith-scarlet_mountains}[/title]"
  "terralith:shield": "[title]&e{lang:biome-announcer-terralith-shield}[/title]"
  "terralith:shield_clearing": "[title]&e{lang:biome-announcer-terralith-shield_clearing}[/title]"
  "terralith:shrubland": "[title]&e{lang:biome-announcer-terralith-shrubland}[/title]"
  "terralith:siberian_grove": "[title]&e{lang:biome-announcer-terralith-siberian_grove}[/title]"
  "terralith:siberian_taiga": "[title]&e{lang:biome-announcer-terralith-siberian_taiga}[/title]"
  "terralith:skylands": "[title]&e{lang:biome-announcer-terralith-skylands}[/title]"
  "terralith:skylands_autumn": "[title]&e{lang:biome-announcer-terralith-skylands_autumn}[/title]"
  "terralith:skylands_spring": "[title]&e{lang:biome-announcer-terralith-skylands_spring}[/title]"
  "terralith:skylands_summer": "[title]&e{lang:biome-announcer-terralith-skylands_summer}[/title]"
  "terralith:skylands_winter": "[title]&e{lang:biome-announcer-terralith-skylands_winter}[/title]"
  "terralith:snowy_badlands": "[title]&e{lang:biome-announcer-terralith-snowy_badlands}[/title]"
  "terralith:snowy_maple_forest": "[title]&e{lang:biome-announcer-terralith-snowy_maple_forest}[/title]"
  "terralith:snowy_shield": "[title]&e{lang:biome-announcer-terralith-snowy_shield}[/title]"
  "terralith:steppe": "[title]&e{lang:biome-announcer-terralith-steppe}[/title]"
  "terralith:stony_spires": "[title]&e{lang:biome-announcer-terralith-stony_spires}[/title]"
  "terralith:temperate_highlands": "[title]&e{lang:biome-announcer-terralith-temperate_highlands}[/title]"
  "terralith:tropical_jungle": "[title]&e{lang:biome-announcer-terralith-tropical_jungle}[/title]"
  "terralith:valley_clearing": "[title]&e{lang:biome-announcer-terralith-valley_clearing}[/title]"
  "terralith:volcanic_crater": "[title]&e{lang:biome-announcer-terralith-volcanic_crater}[/title]"
  "terralith:volcanic_peaks": "[title]&e{lang:biome-announcer-terralith-volcanic_peaks}[/title]"
  "terralith:warm_river": "[title]&e{lang:biome-announcer-terralith-warm_river}[/title]"
  "terralith:warped_mesa": "[title]&e{lang:biome-announcer-terralith-warped_mesa}[/title]"
  "terralith:white_cliffs": "[title]&e{lang:biome-announcer-terralith-white_cliffs}[/title]"
  "terralith:white_mesa": "[title]&e{lang:biome-announcer-terralith-white_mesa}[/title]"
  "terralith:windswept_spires": "[title]&e{lang:biome-announcer-terralith-windswept_spires}[/title]"
  "terralith:wintry_forest": "[title]&e{lang:biome-announcer-terralith-wintry_forest}[/title]"
  "terralith:yellowstone": "[title]&e{lang:biome-announcer-terralith-yellowstone}[/title]"
  "terralith:yosemite_cliffs": "[title]&e{lang:biome-announcer-terralith-yosemite_cliffs}[/title]"
  "terralith:yosemite_lowlands": "[title]&e{lang:biome-announcer-terralith-yosemite_lowlands}[/title]"
  "terralith:cave/andesite_caves": "[title]&e{lang:biome-announcer-terralith-cave-slash-andesite_caves}[/title]"
  "terralith:cave/desert_caves": "[title]&e{lang:biome-announcer-terralith-cave-slash-desert_caves}[/title]"
  "terralith:cave/crystal_caves": "[title]&e{lang:biome-announcer-terralith-cave-slash-crystal_caves}[/title]"
  "terralith:cave/diorite_caves": "[title]&e{lang:biome-announcer-terralith-cave-slash-diorite_caves}[/title]"
  "terralith:cave/frostfire_caves": "[title]&e{lang:biome-announcer-terralith-cave-slash-frostfire_caves}[/title]"
  "terralith:cave/fungal_caves": "[title]&e{lang:biome-announcer-terralith-cave-slash-fungal_caves}[/title]"
  "terralith:cave/granite_caves": "[title]&e{lang:biome-announcer-terralith-cave-slash-granite_caves}[/title]"
  "terralith:cave/infested_Caves": "[title]&e{lang:biome-announcer-terralith-cave-slash-infested_caves}[/title]"
  "terralith:cave/ice_caves": "[title]&e{lang:biome-announcer-terralith-cave-slash-ice_caves}[/title]"
  "terralith:cave/mantle_caves": "[title]&e{lang:biome-announcer-terralith-cave-slash-mantle_caves}[/title]"
  "terralith:cave/thermal_caves": "[title]&e{lang:biome-announcer-terralith-cave-slash-thermal_caves}[/title]"
  "terralith:cave/tuff_caves": "[title]&e{lang:biome-announcer-terralith-cave-slash-tuff_caves}[/title]"
  "terralith:cave.andesite_caves": "[title]&e{lang:biome-announcer-terralith-cave-dot-andesite_caves}[/title]"
  "terralith:cave.desert_caves": "[title]&e{lang:biome-announcer-terralith-cave-dot-desert_caves}[/title]"
  "terralith:cave.diorite_caves": "[title]&e{lang:biome-announcer-terralith-cave-dot-diorite_caves}[/title]"
  "terralith:cave.frostfire_caves": "[title]&e{lang:biome-announcer-terralith-cave-dot-frostfire_caves}[/title]"
  "terralith:cave.fungal_caves": "[title]&e{lang:biome-announcer-terralith-cave-dot-fungal_caves}[/title]"
  "terralith:cave.granite_caves": "[title]&e{lang:biome-announcer-terralith-cave-dot-granite_caves}[/title]"
  "terralith:cave.ice_caves": "[title]&e{lang:biome-announcer-terralith-cave-dot-ice_caves}[/title]"
  "terralith:cave.mantle_caves": "[title]&e{lang:biome-announcer-terralith-cave-dot-mantle_caves}[/title]"
  "terralith:cave.thermal_caves": "[title]&e{lang:biome-announcer-terralith-cave-dot-thermal_caves}[/title]"
  "terralith:cave.tuff_caves": "[title]&e{lang:biome-announcer-terralith-cave-dot-tuff_caves}[/title]"
```
