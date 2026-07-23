# 🔍Match Entity Format

The **Match Item Format** provided by **UltimateTweak** are almost identical to those in MythicRewards. Therefore, they will not be elaborated on in this wiki. Please refer to MythicRewards's wiki for a detailed introduction about them. Click [here](https://manyou.gitbook.io/mythicrewards/match-entity-rules/none) to view.

For simple list of them, please view below:

#### **entity-types**

Match by entity type (for example `zombie`, `skeleton`).

```yaml
entity-types:
  - zombie
  - husk
```

#### **entity-tag**

Match by vanilla entity tag.

```yaml
entity-tag:
  - minecraft:undead
  - minecraft:raiders
```

## **entity-contains-name**

Entity custom name contains listed text (ignores color/format).

```yaml
entity-contains-name:
  - Boss
  - Elite
```

#### **entity-health**

Minimum current health check (`>=`).

```yaml
entity-health: 10
```

#### **ranged (Paper only)**

Require ranged or non-ranged entities.

* `true`: must be ranged
* `false`: must be non-ranged

```yaml
ranged: true
```

#### **equip**

Match entity equipment slots (each slot uses Match Item rules).

Available slots:

* `main-hand`
* `off-hand`
* `helmet`
* `chestplate`
* `leggings`
* `boots`

```yaml
equip:
  main-hand:
    material:
      - bow
  helmet:
    has-enchants:
      - protection
```

> Current behavior: any configured slot match can return `true`.

#### **entity-pdc**

Match `PersistentDataContainer` values (string/number/boolean supported).

**String rules**

* exact match
* contains match
* wildcard `*`

```yaml
entity-pdc:
  myplugin:role: boss*
```

**Number rules**

Supports `5~10`, `>=5`, `<=10`, `>5`, `<10`, `=8`, or `8`.

```yaml
entity-pdc:
  myplugin:level: ">=20"
  myplugin:phase: "2~4"
```

**Boolean rules**

```yaml
entity-pdc:
  myplugin:awakened: true
```

#### **mythicmobs (requires MythicMobs)**

Match MythicMobs internal mob names.

```yaml
mythicmobs:
  - FireDemon
  - IceBoss
```

#### **levelled-mobs (requires LevelledMobs)**

Match LevelledMobs level.

Supports:

* exact value: `10`
* range: `5~20`
* comparisons: `>=15`, `<=30`

```yaml
levelled-mobs: ">=25"
```

#### **any**

OR logic (any sub-group can match).

```yaml
any:
  1:
    entity-types:
      - zombie
  2:
    entity-tag:
      - minecraft:raiders
```

or same-level OR:

```yaml
any:
  ranged: true
  entity-health: 30
```

#### **not**

Negation block (if any rule inside `not` matches, overall match fails).

```yaml
not:
  entity-types:
    - villager
```

#### **none**

Always fail (placeholder/temporary disable).

```yaml
none: true
```
