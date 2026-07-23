# 🎬Action Format

## Sound

Send sound to player.

```yaml
general-actions:
  1:
    type: sound
    sound: 'ui.button.click'
    volume: 1
    pitch: 1
```

## Message

Send a message to the player, support color code.

```yaml
general-actions:
  1:
    type: message
    message: 'Hello!'
```

## Title <a href="#title" id="title"></a>

Send title to the player, support the color code.

```yaml
general-actions:
  1:
    type: title
    main-title: 'Good day'
    sub-title: 'Not bad'
    fade-in: 10
    stay: 70
    fade-out: 30
```

## Particle <a href="#particle" id="particle"></a>

```yaml
actions:
  1: 
    type: particle
    particle: HEART
    count: 20
    offset-x: 0.3
    offset-y: 1.0
    offset-z: 0.3
    speed: 0.01
```

## Effect

Give players potion effect.

```yaml
general-actions:
  1:
    type: effect
    potion: BLINDNESS
    duration: 60
    level: 1
    ambient: true # Optional
    particles: true # Optional
    icon: true # Optional
```

## Teleport

Teleport player to specified location.

```yaml
general-actions:
  1:
    type: teleport
    world: LobbyWorld
    x: 100
    y: 30
    z: 300
    pitch: 90 # Optional
    yaw: 0 # Optional
```

## Player Command

Make the player excutes a command.

```yaml
general-actions:
  1:
    type: player_command
    command: 'tell Hello!'
```

## Op Command

Make the player excutes a command as OP.

```yaml
general-actions:
  1:
    type: op_command
    command: 'tell Hello!'
```

## Console Command

Make the console excutes a command.

```yaml
general-actions:
  1:
    type: console_command
    command: 'op {player}'
```

## Spawn vanilla mobs

Spawn vanilla mobs.

```yaml
general-actions:
  1:
    type: entity_spawn
    entity: ZOMBIE
    world: LOBBY # Optional
    x: 100.0 # Optional
    y: 2.0 # Optional
    z: -100.0 # Optional
```

## MythicMobs spawn

Require MythicMobs.

```yaml
general-actions:
  1:
    type: mythicmobs_spawn
    entity: Super_Skeleton
    level: 1 # Optional
    world: LOBBY # Optional
    x: 100.0 # Optional
    y: 2.0 # Optional
    z: -100.0 # Optional
```

## Delay

Make the action run after X ticks.

```yaml
general-actions:
  1:
    type: delay
    time: 50
    actions:
      1:
        type: entity_spawn
        entity: ZOMBIE
```

## Chance

Set the chance the action will be excuted, up to 100. 50 means this action has 50% chance to excute.

```yaml
genneal-actions:
  1:
    type: chance
    rate: 50
    actions:
      1:
        type: entity_spawn
        entity: ZOMBIE
```

## Any

Randomly choose specified amount of actions to execute.

```yaml
general-actions:
  1:
    type: any
    amount: 2
    actions:
      1:
        type: entity_spawn
        entity: ZOMBIE
      2:
        type: entity_spawn
        entity: SKELETON
      3:
        type: entity_spawn
        entity: WITHER
```

## Conditional

Only players meet the conditions you set here will be able to execute the action.

```yaml
general-actions:
  1:
    type: conditional
    conditions:
      1: 
        type: world
        world: lobby
    actions:
      1:
        type: entity_spawn
        entity: ZOMBIE
```

## Give Item

Should use ItemFormat in `item` option. For more info about Item Format, plaese [click here](https://ultimateshop.superiormc.cn/format/itemformat-tm).

```yaml
general-actions:
  1:
    type: give_item
    item:
      material: apple # Item Format here
```

## Drop Item

Should use ItemFormat in `item` option. For more info about Item Format, plaese [click here](https://ultimateshop.superiormc.cn/format/itemformat-tm).

```yaml
general-actions:
  1:
    type: drop_item
    item:
      material: apple # Item Format here
      world: world # Optional, support placeholder
      x: 15 # Optional, support placeholder
      y: 12 # Optional, support placeholder
      z: 10 # Optional, support placeholder
```
