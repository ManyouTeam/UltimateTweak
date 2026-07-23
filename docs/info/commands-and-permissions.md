# ⌨️Commands & Permissions

Main command: `/ultimatetweak`\
Aliases: `/ut`, `/tweak`

| Command                                   | Permission                         | Description                                                              |
| ----------------------------------------- | ---------------------------------- | ------------------------------------------------------------------------ |
| `/ut reload`                              | `ultimatetweak.reload`             | Reloads the main config, tweaks, tree definitions, items, and languages. |
| `/ut saveitem <ID> [bukkit\|itemformat]`  | `ultimatetweak.saveitem`           | Saves the main-hand item under `items/`.                                 |
| `/ut givesaveitem <ID> [player] [amount]` | `ultimatetweak.givesaveitem`       | Gives a previously saved item.                                           |
| `/ut generateitemformat`                  | `ultimatetweak.generateitemformat` | Writes the held item format to `generated-item-format.yml`.              |
| `/ut debug <tree definition ID\|off>`     | `ultimatetweak.debug`              | Enables or disables tree recognition diagnostics.                        |

`ultimatetweak.bypass.protection` bypasses checks from all hooked protection plugins. Server operators bypass these checks automatically.
