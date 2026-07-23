package cn.superiormc.ultimatetweak.hooks.protection;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public abstract class AbstractProtectionHook {

    protected String pluginName;

    public AbstractProtectionHook(String pluginName) {
        this.pluginName = pluginName;
    }

    public abstract boolean canBreak(Player player, Location location);

    public abstract ProtectionRegionResult createRegion(ProtectionRegion region);

    public String getPluginName() {
        return pluginName;
    }
}
