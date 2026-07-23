# 🛠️Configuration files

The plugin generates the following configuration files, some of which will only be generated after you first use this feature.

* `items`: The location for storing saved item files.
* `tweaks`: Save every tweak file.
* `tree_determine_settings`: Save every tree determine settings file.
* `languages`: The location for storing language files. You can set the language file used by the plugin through the `config-files.language` option in the `config.yml` file. You can customize various messages within the plugin game through language files. For per player language, or send action bar/title/boss bar/sound, please view [this page](../features/advanced-language-managment.md).
* `config.yml` file: The location for main common settings for plugins.
* `generated-item-format.yml` file: When using the `/ut generateeitemformat` command, we will parse the item you are holding into an **ItemFormat** and store the parsed **ItemFormat** content in this file.

