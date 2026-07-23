# 🔍Match Item Format

The **Match Item Format** provided by **UltimateTweak** are almost identical to those in MythicChanger. Therefore, they will not be elaborated on in this wiki. Please refer to MythicChanger's wiki for a detailed introduction about them. Click [here](https://mythicchanger.superiormc.cn/match-item-rules/none) to view.

For simple list of them, please view below:

#### material

Match by material key (namespace key, case-insensitive).

```yaml
material:
  - diamond_sword
  - netherite_sword
```

#### material-tag

Match by vanilla item/block tag.

```yaml
material-tag:
  - minecraft:swords
  - minecraft:planks
```

#### items

Match by external item system ID (resolved by HookManager).

```yaml
items:
  - mmoitems;;SWORD;;FLAME_BLADE
  - nexo;;legend_blade
```

#### use-tier-identify

Whether item tier is included while parsing item IDs.

```yaml
use-tier-identify: true
```

#### has-name

Require item display name.

```yaml
has-name: true
```

#### contains-name

Item display name must contain any listed text (ignores color/format).

```yaml
contains-name:
  - Epic
  - Flame
```

#### has-lore

Require lore to exist.

```yaml
has-lore: true
```

#### contains-lore

Any lore line containing listed text will pass.

```yaml
contains-lore:
  - Lifesteal
  - Curse
```

#### has-enchants

Require the item to contain any listed enchantment.

* `*` means "has any enchantment"

```yaml
has-enchants:
  - sharpness
  - unbreaking
```

```yaml
has-enchants:
  - "*"
```

#### has-stored-enchants

For enchanted books, require stored enchantments.

* only valid for stored-enchant metadata
* `*` means any stored enchantment

```yaml
has-stored-enchants:
  - mending
```

#### contains-enchants

Check enchant level rules.

* `contains-enchants.<enchant>: <number>` means level `>` number
* `contains-enchants.<enchant>: [list]` means level must be in list

```yaml
contains-enchants:
  sharpness: 3
```

```yaml
contains-enchants:
  protection: [1, 2, 4]
```

#### contains-enchants-amount

Check enchantment count.

* number: `>=`
* list: must match one listed value

```yaml
contains-enchants-amount: 3
```

```yaml
contains-enchants-amount: [1, 2, 5]
```

#### enchantable

Check whether the item can accept listed enchantments.

```yaml
enchantable:
  - sharpness
  - looting
```

#### rarity (1.20.5+)

Match item rarity (`NONE` when no rarity exists).

```yaml
rarity: RARE
```

#### contains-nbt (requires NBTAPI)

Check whether an NBT path exists.

Path format: `parent;;child;;final-key`

```yaml
contains-nbt:
  - CustomModelData
  - PublicBukkitValues;;myplugin:key
```

#### nbt-string (requires NBTAPI)

Match NBT string value.

* top-level: `key;;value`
* nested: `a;;b;;key;;value`

```yaml
nbt-string:
  - PublicBukkitValues;;myplugin:key;;special
```

#### nbt-int / nbt-double / nbt-float / nbt-long / nbt-short / nbt-byte (requires NBTAPI)

Match numeric NBT values with compare rules.

```yaml
nbt-int:
  - CustomModelData;;>=1000
```

#### pdc

Match `PersistentDataContainer` values.

```yaml
pdc:
  myplugin:key: value
```

#### custom-model-data

Match custom model data.

```yaml
custom-model-data: 1001
```

#### amount

Match stack amount.

```yaml
amount: ">=1"
```

#### damage

Match item damage value.

```yaml
damage: "<=10"
```

#### unbreakable

Require unbreakable state.

```yaml
unbreakable: true
```

#### any

OR logic: match any sub-rule group.

```yaml
any:
  1:
    material:
      - diamond_sword
  2:
    has-enchants:
      - sharpness
```

#### not

Negation block: if inner rule matches, this block fails.

```yaml
not:
  material-tag:
    - minecraft:logs
```

#### none

Always fail (useful as placeholder/temporary disable).

```yaml
none: true
```
