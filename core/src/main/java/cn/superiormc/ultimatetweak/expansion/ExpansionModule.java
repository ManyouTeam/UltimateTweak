package cn.superiormc.ultimatetweak.expansion;

import cn.superiormc.ultimatetweak.UltimateTweak;
import cn.superiormc.ultimatetweak.managers.ErrorManager;
import cn.superiormc.ultimatetweak.managers.LanguageManager;
import cn.superiormc.ultimatetweak.utils.TextUtil;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public abstract class ExpansionModule {

    private UltimateTweak plugin;

    public final void enable(UltimateTweak plugin) {
        if (this.plugin != null) {
            throw new IllegalStateException("Expansion module is already enabled: " + getClass().getName());
        }
        this.plugin = plugin;
        try {
            onEnable();
        } catch (RuntimeException | Error throwable) {
            cleanupAfterFailedEnable();
            throw throwable;
        }
    }

    public final void disable() {
        if (plugin == null) {
            return;
        }
        try {
            onDisable();
        } finally {
            plugin = null;
        }
    }

    protected abstract void onEnable();

    protected abstract void onDisable();

    /**
     * Resource directory inside the final plugin jar, for example {@code premium}.
     */
    protected abstract String getResourceDirectory();

    /**
     * Old standalone plugin data folder used for one-time config migration.
     */
    protected String getLegacyDataFolderName() {
        return null;
    }

    protected final UltimateTweak getPlugin() {
        if (plugin == null) {
            throw new IllegalStateException("Expansion module is not enabled: " + getClass().getName());
        }
        return plugin;
    }

    protected final void prepareConfig(String path) {
        File file = new File(getPlugin().getDataFolder(), path);
        if (file.exists()) {
            return;
        }

        String legacyFolderName = getLegacyDataFolderName();
        if (legacyFolderName != null && !legacyFolderName.isBlank()) {
            File pluginsDirectory = getPlugin().getDataFolder().getParentFile();
            File legacy = new File(new File(pluginsDirectory, legacyFolderName), path);
            if (copyLegacyConfig(legacy, file, path)) {
                return;
            }
        }

        String resourcePath = getResourcePath(path);
        try (InputStream input = getPlugin().getResource(resourcePath)) {
            if (input == null) {
                throw new IOException("Missing bundled resource: " + resourcePath);
            }
            Files.createDirectories(file.toPath().getParent());
            Files.copy(input, file.toPath());
        } catch (IOException exception) {
            throw new IllegalStateException("Could not create " + file, exception);
        }
    }

    protected final void registerLanguageDefaults(String language) {
        String path = getResourcePath("languages/" + language + ".yml");
        try (InputStream input = getPlugin().getResource(path)) {
            if (input == null) {
                ErrorManager.errorManager.sendErrorMessage(
                        "§cError: Missing bundled language resource: " + path);
                return;
            }
            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(input, StandardCharsets.UTF_8));
            LanguageManager.languageManager.registerLanguageDefaults(language, defaults);
        } catch (Exception exception) {
            ErrorManager.errorManager.sendErrorMessage("§cError: Could not load language defaults from "
                    + path + ": " + exception.getMessage());
        }
    }

    private boolean copyLegacyConfig(File legacy, File destination, String path) {
        if (!legacy.isFile()) {
            return false;
        }
        try {
            Files.createDirectories(destination.toPath().getParent());
            Files.copy(legacy.toPath(), destination.toPath());
            TextUtil.sendMessage(null, TextUtil.pluginPrefix()
                    + " §fMigrated " + path + " into UltimateTweak.");
            return true;
        } catch (IOException exception) {
            ErrorManager.errorManager.sendErrorMessage("§cError: Could not migrate " + path + ": "
                    + exception.getMessage());
            return false;
        }
    }

    private String getResourcePath(String path) {
        String directory = getResourceDirectory();
        String normalizedDirectory = directory == null ? "" : directory.replaceAll("^/+|/+$", "");
        String normalizedPath = path.replaceAll("^/+", "");
        return normalizedDirectory.isEmpty()
                ? normalizedPath
                : normalizedDirectory + "/" + normalizedPath;
    }

    private void cleanupAfterFailedEnable() {
        try {
            onDisable();
        } catch (Throwable throwable) {
            ErrorManager.errorManager.sendErrorMessage("§cError: Could not clean up expansion module "
                    + getClass().getName() + ": " + throwable.getMessage());
        } finally {
            plugin = null;
        }
    }
}
