package cn.superiormc.ultimatetweak;

import cn.superiormc.ultimatetweak.managers.*;
import cn.superiormc.ultimatetweak.utils.*;
import com.github.retrooper.packetevents.PacketEvents;
import me.tofaa.entitylib.APIConfig;
import me.tofaa.entitylib.EntityLib;
import me.tofaa.entitylib.spigot.SpigotEntityLibPlatform;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class UltimateTweak extends JavaPlugin {

    public static UltimateTweak instance;

    private Metrics metrics;

    public static int yearVersion;

    public static int majorVersion;

    public static int minorVersion;

    public static SpecialMethodUtil methodUtil;

    public static boolean isFolia = false;

    private static boolean entityLibAvailable = false;

    @Override
    public void onEnable() {
        instance = this;
        try {
            String[] versionParts = Bukkit.getBukkitVersion().split("-")[0].split("\\.");
            yearVersion = versionParts.length > 0 && versionParts[0].matches("\\d+") ? Integer.parseInt(versionParts[0]) : 1;
            majorVersion = versionParts.length > 1 && versionParts[1].matches("\\d+") ? Integer.parseInt(versionParts[1]) : 0;
            minorVersion = versionParts.length > 2 && versionParts[2].matches("\\d+") ? Integer.parseInt(versionParts[2]) : 0;
        } catch (Throwable throwable) {
            Bukkit.getConsoleSender().sendMessage("§x§9§8§F§B§9§8[UltimateTweak] §cError: Can not get your Minecraft version! Default set to 1.0.0.");
        }
        if (CommonUtil.getClass("com.destroystokyo.paper.PaperConfig") && CommonUtil.getMinorVersion(18, 2)) {
            try {
                Class<?> paperClass = Class.forName("cn.superiormc.ultimatetweak.paper.PaperMethodUtil");
                methodUtil = (SpecialMethodUtil) paperClass.getDeclaredConstructor().newInstance();
                TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fPaper is found, entering Paper plugin mode...");
            } catch (Throwable throwable) {
                Bukkit.getConsoleSender().sendMessage(TextUtil.pluginPrefix() + " §cError: The plugin seems break, please download it again from site.");
                Bukkit.getPluginManager().disablePlugin(this);
            }
        } else {
            try {
                Class<?> spigotClass = Class.forName("cn.superiormc.ultimatetweak.spigot.SpigotMethodUtil");
                methodUtil = (SpecialMethodUtil) spigotClass.getDeclaredConstructor().newInstance();
                TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fSpigot is found, entering Spigot plugin mode...");
            } catch (Throwable throwable) {
                Bukkit.getConsoleSender().sendMessage(TextUtil.pluginPrefix() + " §cError: The plugin seems break, please download it again from site.");
                Bukkit.getPluginManager().disablePlugin(this);
            }
        }
        if (CommonUtil.getClass("io.papermc.paper.threadedregions.RegionizedServer")) {
            TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fFolia is found, enabled Folia compatibility feature!");
            TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §6Warning: Folia support is not fully test, major bugs maybe found! " +
                    "Please do not use in production environment!");
            isFolia = true;
        }
        if (UltimateTweak.methodUtil.methodID().equals("paper")) {
            initEntityLib();
        } else {
            TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §6Warning: EntityLib does not support Spigot servers. " +
                    "Block animations and block glow effects have been disabled.");
        }
        new ErrorManager();
        new InitManager();
        new ActionManager();
        new ConditionManager();
        new ConfigManager();
        new MatchItemManager();
        new TreeDetermineManager();
        new AttributeModifyManager();
        new SwingItemManager();
        new TweakManager();
        new MatchEntityManager();
        new HookManager();
        new LanguageManager();
        new CommandManager();
        new ListenerManager();
        metrics = new Metrics(UltimateTweak.instance, 32804);
        TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fYour server version is: " + yearVersion + "." + majorVersion + "." + minorVersion + "!");
        TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fPlugin is loaded. Author: PQguanfang.");
    }

    private void initEntityLib() {
        if (EntityLib.getOptionalApi().isPresent()) {
            entityLibAvailable = true;
            return;
        }

        SpigotEntityLibPlatform platform = new SpigotEntityLibPlatform(this);
        APIConfig settings = new APIConfig(PacketEvents.getAPI())
                .tickTickables()
                .usePlatformLogger();
        EntityLib.init(platform, settings);
        entityLibAvailable = true;
    }

    public static boolean isEntityLibAvailable() {
        return entityLibAvailable;
    }

    @Override
    public void onDisable() {
        entityLibAvailable = false;
        if (metrics != null) {
            metrics.shutdown();
            metrics = null;
        }
        if (TweakManager.tweakManager != null) {
            TweakManager.tweakManager.shutdown();
        }
        if (AttributeModifyManager.attributeModifyManager != null) {
            AttributeModifyManager.attributeModifyManager.shutdown();
        }
        if (SwingItemManager.swingItemManager != null) {
            SwingItemManager.swingItemManager.shutdown();
        }
        if (ListenerManager.listenerManager != null) {
            ListenerManager.listenerManager.shutdown();
        }
        TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fPlugin is disabled. Author: PQguanfang.");
    }
}
