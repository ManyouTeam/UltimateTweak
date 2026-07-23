# 🛠️Configuration files

The plugin generates the following configuration files:

* `tweaks`: Contains one configuration file for each tweak.
* `tree_determine_settings`: Contains tree recognition definitions.
* `languages`: The location for storing language files. You can set the language file used by the plugin through the `config-files.language` option in the `config.yml` file. You can customize various messages within the plugin game through language files. For per player language, or send action bar/title/boss bar/sound, please view [this page](../features/advanced-language-managment.md).
* `config.yml` file: The location for main common settings for plugins.

## Common tweak options

Every file under `tweaks/` supports the same `enabled` and `worlds` options:

```yaml
enabled: true

worlds:
  mode: blacklist
  list: []
```

* `worlds.mode: blacklist`: the tweak works in every world except those in `worlds.list`.
* `worlds.mode: whitelist`: the tweak works only in worlds in `worlds.list`.
* World names are matched case-insensitively.
* An empty blacklist enables the tweak in all worlds. An empty whitelist disables it in all worlds.

For example, to enable a tweak only in `world` and `resource_world`:

```yaml
worlds:
  mode: whitelist
  list:
    - world
    - resource_world
```

