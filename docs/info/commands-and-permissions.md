# ⌨️Commands & Permissions

Main command: `/ultimatetweak`\
Aliases: `/ut`, `/tweak`

| Command                               | Permission                 | Description                                       |
| ------------------------------------- | -------------------------- | ------------------------------------------------- |
| `/ut reload`                          | `ultimatetweak.reload`     | Reloads the main config, tweaks, tree definitions, and languages. |
| `/ut debug <tree definition ID\|off>` | `ultimatetweak.debug`      | Enables or disables tree recognition diagnostics. |
| `/ut guide ...`                       | `ultimatetweak.guide.admin` | Manages TweakExpansion Guide targets. See [Guide](../tweaks/guide.md) for all subcommands. |

`ultimatetweak.bypass.protection` bypasses checks from all hooked protection plugins. Server operators bypass these checks automatically.

## Tweak permissions

| Permission | Description | Default |
| --- | --- | --- |
| `ultimatetweak.best-tool` | Allows automatic best-tool selection. | Everyone |
| `ultimatetweak.double-door` | Allows opening and closing matching doors together. | Everyone |
| `ultimatetweak.tree-cutter` | Allows using Tree Cutter. | Everyone |
| `ultimatetweak.vein-mine` | Allows using Vein Mine. | Everyone |
| `ultimatetweak.dynamic-light` | Allows creating dynamic light from held items. | Everyone |
| `ultimatetweak.biome-announcer` | Allows receiving biome announcements. | Everyone |
| `ultimatetweak.structure-announcer` | Allows receiving structure announcements. | Everyone |

These permission values can be changed with the `permission` option in each tweak's configuration file. An empty value disables the permission check.

## TweakExpansion permissions

| Permission | Description | Default |
| --- | --- | --- |
| `ultimatetweak.container-preview` | Allows looking at container previews. | Everyone |
| `ultimatetweak.damage-indicator` | Allows viewing floating damage and healing numbers. | Everyone |
| `ultimatetweak.health-bar` | Allows viewing overhead entity health bars. | Everyone |
| `ultimatetweak.auto-ladder` | Allows automatic ladder climbing and sliding. | Everyone |
| `ultimatetweak.elevator` | Allows using configured elevator blocks. | Everyone |
| `ultimatetweak.elevator.bypass.same-block` | Bypasses the elevator same-block restriction. | Nobody |
| `ultimatetweak.elevator.bypass.blocks-between` | Bypasses the elevator blocks-between restriction. | Nobody |
| `ultimatetweak.multi-armor` | Allows automatic chestplate and Elytra swapping. | Everyone |
| `ultimatetweak.projectile-prediction` | Allows projectile path and impact prediction. | Everyone |
| `ultimatetweak.block-highlighter.treasure` | Allows the default treasure highlight group. | Everyone |
| `ultimatetweak.block-highlighter.bypass.occlusion` | Allows block highlights through obstructions. | Nobody |
| `ultimatetweak.guide.admin` | Allows managing Guide targets for online players. | Operators |

Normal use permissions and highlight-group permissions can be changed in their respective tweak files. Bypass permissions are fixed by the plugin and cannot be renamed in configuration.
