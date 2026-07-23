# 🛡️Structure Auto Protect

## Config

```yaml
enabled: false

worlds:
  mode: blacklist
  list: []

# The protection hook used for creating regions.
protection-hook: Dominion

# Empty list means all structures.
structures: []

# How often loaded structures around online players are checked.
check-interval-ticks: 100

# Prefix for generated protection region ids.
region-id-prefix: "ut_structure_"

# Expands the created region vertically beyond the generated structure bounding box.
expand-y: 0

# Print create/already-exists results to console.
debug: false
```

For list of supported `protection-hook`, please [click here](../info/compatibility.md). Some plugins does not support this. The Dominion plugin is tested.
