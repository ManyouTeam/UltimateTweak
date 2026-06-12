package cn.superiormc.ultimatetweak.managers;

import cn.superiormc.ultimatetweak.UltimateTweak;
import cn.superiormc.ultimatetweak.managers.TreeDetermineManager.TreeDefinition;
import cn.superiormc.ultimatetweak.utils.TextUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class ConfigManager {

    public static ConfigManager configManager;

    public FileConfiguration config;

    private List<TreeDefinition> treeDefinitions = Collections.emptyList();

    public ConfigManager() {
        configManager = this;
        config = UltimateTweak.instance.getConfig();
        initTreeDetermineSettings();
    }

    private void initTreeDetermineSettings() {
        File dir = new File(UltimateTweak.instance.getDataFolder(), "tree_determine_settings");
        if (!dir.exists()) {
            dir.mkdir();
        }
        loadTreeDetermineSettings(dir);
    }

    public void loadTreeDetermineSettings(File folder) {
        List<TreeDefinition> definitions = new ArrayList<>();
        loadTreeDetermineSettings(folder, definitions);
        treeDefinitions = Collections.unmodifiableList(definitions);
    }

    private void loadTreeDetermineSettings(File folder, List<TreeDefinition> definitions) {
        File[] files = folder.listFiles();
        if (files == null) {
            return;
        }
        Arrays.sort(files, Comparator.comparing(File::getName));
        for (File file : files) {
            if (file.isDirectory()) {
                loadTreeDetermineSettings(file, definitions);
                continue;
            }
            String fileName = file.getName();
            if (!fileName.endsWith(".yml") && !fileName.endsWith(".yaml")) {
                continue;
            }
            String fallbackId = fileName.substring(0, fileName.lastIndexOf('.'));
            TreeDefinition treeDefinition = TreeDefinition.fromSection(
                    fallbackId,
                    YamlConfiguration.loadConfiguration(file));
            if (treeDefinition == null) {
                ErrorManager.errorManager.sendErrorMessage("§cError: Invalid tree determine setting: " + fileName + "!");
                continue;
            }
            definitions.add(treeDefinition);
            TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fLoaded tree determine setting: " + fileName + "!");
        }
    }

    public List<TreeDefinition> getTreeDefinitions() {
        return treeDefinitions;
    }

    public TreeDefinition getTreeDefinition(String id) {
        if (id == null) {
            return null;
        }
        for (TreeDefinition treeDefinition : treeDefinitions) {
            if (treeDefinition.getId().equalsIgnoreCase(id)) {
                return treeDefinition;
            }
        }
        return null;
    }

    public List<TreeDefinition> getTreeDefinitionsByLog(String blockId) {
        if (blockId == null) {
            return Collections.emptyList();
        }
        String normalizedBlockId = TreeDefinition.normalizeBlockId(blockId);
        List<TreeDefinition> result = new ArrayList<>();
        for (TreeDefinition treeDefinition : treeDefinitions) {
            if (treeDefinition.getLogs().contains(normalizedBlockId)) {
                result.add(treeDefinition);
            }
        }
        return result;
    }

    public boolean getBoolean(String path) {
        return config.getBoolean(path, false);
    }

    public boolean getBoolean(String path, boolean defaultValue) {
        return config.getBoolean(path, defaultValue);
    }

    public String getString(String path, String... args) {
        String s = config.getString(path);
        if (s == null) {
            if (args.length == 0) {
                return null;
            }
            s = args[0];
        }
        for (int i = 1 ; i < args.length ; i += 2) {
            String var = "{" + args[i] + "}";
            if (args[i + 1] == null) {
                s = s.replace(var, "");
            }
            else {
                s = s.replace(var, args[i + 1]);
            }
        }
        return s.replace("{plugin_folder}", String.valueOf(UltimateTweak.instance.getDataFolder()));
    }

    public int getInt(String path, int defaultValue) {
        return config.getInt(path, defaultValue);
    }

    public double getDouble(String path, double defaultValue) {
        return config.getDouble(path, defaultValue);
    }

    public ConfigurationSection getSection(String path) {
        if (config.getConfigurationSection(path) == null) {
            return new MemoryConfiguration();
        }
        return config.getConfigurationSection(path);
    }
}
