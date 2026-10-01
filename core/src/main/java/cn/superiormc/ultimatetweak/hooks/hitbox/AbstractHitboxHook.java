package cn.superiormc.ultimatetweak.hooks.hitbox;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractHitboxHook {

    private final String pluginName;

    protected AbstractHitboxHook(String pluginName) {
        this.pluginName = pluginName;
    }

    public String getPluginName() {
        return pluginName;
    }

    @Nullable
    public abstract HitboxResult resolve(Entity entity);
}
