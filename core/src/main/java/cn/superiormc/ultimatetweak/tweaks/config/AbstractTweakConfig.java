package cn.superiormc.ultimatetweak.tweaks.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public abstract class AbstractTweakConfig {

    private final String id;

    private final File file;

    private FileConfiguration config;

    private volatile WorldMode worldMode = WorldMode.BLACKLIST;

    private volatile Set<String> worlds = Collections.emptySet();

    protected AbstractTweakConfig(String id, File file) {
        this.id = id;
        this.file = file;
        reload();
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(file);
        String configuredMode = getString("worlds.mode", "blacklist");
        worldMode = "whitelist".equalsIgnoreCase(configuredMode)
                ? WorldMode.WHITELIST : WorldMode.BLACKLIST;
        Set<String> loadedWorlds = new HashSet<>();
        for (String world : getStringList("worlds.list")) {
            if (world != null && !world.isBlank()) {
                loadedWorlds.add(world.trim().toLowerCase(Locale.ENGLISH));
            }
        }
        worlds = Collections.unmodifiableSet(loadedWorlds);
    }

    public String getId() {
        return id;
    }

    public File getFile() {
        return file;
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public boolean isEnabled() {
        return getBoolean("enabled", true);
    }

    public boolean getBoolean(String path, boolean defaultValue) {
        return config.getBoolean(path, defaultValue);
    }

    public int getInt(String path, int defaultValue) {
        return config.getInt(path, defaultValue);
    }

    public double getDouble(String path, double defaultValue) {
        return config.getDouble(path, defaultValue);
    }

    public String getString(String path, String defaultValue) {
        return config.getString(path, defaultValue);
    }

    public List<String> getStringList(String path) {
        return config.getStringList(path);
    }

    public ConfigurationSection getSection(String path) {
        ConfigurationSection section = config.getConfigurationSection(path);
        return section == null ? new MemoryConfiguration() : section;
    }

    public boolean isWorldEnabled(String worldName) {
        if (worldName == null) {
            return false;
        }
        boolean listed = worlds.contains(worldName.toLowerCase(Locale.ENGLISH));
        return worldMode == WorldMode.WHITELIST ? listed : !listed;
    }

    private enum WorldMode {
        WHITELIST,
        BLACKLIST
    }
}
