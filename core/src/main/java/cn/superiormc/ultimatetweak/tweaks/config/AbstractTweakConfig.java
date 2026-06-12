package cn.superiormc.ultimatetweak.tweaks.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public abstract class AbstractTweakConfig {

    private final String id;

    private final File file;

    private FileConfiguration config;

    protected AbstractTweakConfig(String id, File file) {
        this.id = id;
        this.file = file;
        reload();
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(file);
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

    public ConfigurationSection getSection(String path) {
        ConfigurationSection section = config.getConfigurationSection(path);
        return section == null ? new MemoryConfiguration() : section;
    }
}
