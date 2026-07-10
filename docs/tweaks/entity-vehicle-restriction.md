# 🚫Entity Vehicle Restriction

## Config

{% code title="tweaks/entity-vehicle-restriction.yml" %}
```yaml
enabled: false

# Supported values: boat, minecart
vehicles:
  - boat
  - minecart

# Omit match-entity to match all living entities.
```
{% endcode %}

Set `enabled` to `true`, select `boat` and/or `minecart` under `vehicles`, and optionally add `match-entity`. Omitting `match-entity` matches every living entity.

For `match-entity` option, should use [Match Entity Format](../format/match-entity-format.md) here.
