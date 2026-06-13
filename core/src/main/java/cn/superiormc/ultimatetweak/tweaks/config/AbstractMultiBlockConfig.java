package cn.superiormc.ultimatetweak.tweaks.config;

import cn.superiormc.ultimatetweak.objects.ObjectAction;
import cn.superiormc.ultimatetweak.objects.ObjectCondition;
import cn.superiormc.ultimatetweak.utils.CommonUtil;
import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;

public abstract class AbstractMultiBlockConfig extends AbstractTweakConfig {

    private ObjectAction breakActions;

    private ObjectAction damageActions;

    private ObjectCondition conditions;

    protected AbstractMultiBlockConfig(String id, File file) {
        super(id, file);
    }

    @Override
    public void reload() {
        super.reload();
        breakActions = new ObjectAction(getSection("break-actions"));
        damageActions = new ObjectAction(getSection("damage-actions"));
        conditions = new ObjectCondition(getSection("conditions"));
    }

    public ObjectAction getBreakActions() {
        return breakActions;
    }

    public ObjectAction getDamageActions() {
        return damageActions;
    }

    public ObjectCondition getConditions() {
        return conditions;
    }

    public boolean isDamageGlowEnabled() {
        return getBoolean("damage-glow.enabled", true);
    }

    public boolean shouldHideBreakingBlockFromDamageGlow() {
        return getBoolean("damage-glow.hide-breaking-block", true);
    }

    public int getDamageGlowColor() {
        return getGlowColor("damage-glow.glow-color", "#FFFFFF");
    }

    protected int getGlowColor(String path, String defaultValue) {
        try {
            return CommonUtil.parseColor(getString(path, defaultValue)).asRGB();
        } catch (IllegalArgumentException exception) {
            return Color.WHITE.asRGB();
        }
    }

    public int getDamageGlowDurationTicks() {
        return Math.max(1, getInt("damage-glow.duration-ticks", 40));
    }

    public int getDamageGlowViewDistance() {
        return Math.max(1, getInt("damage-glow.view-distance", 24));
    }

    public boolean isMiningTimeEnabled() {
        return getBoolean("mining-time.enabled", true);
    }

    public int getCooldownTicks() {
        return Math.max(0, getInt("cooldown-ticks", 5));
    }

    public int getBlocksPerTick() {
        return Math.max(1, getInt("blocks-per-tick", 32));
    }

    public abstract double getMiningTimePercentPerBlock();

    public double getMaxMiningTimePercent() {
        return Math.max(0.0, getDouble("mining-time.max-percent", 5000.0));
    }

    public boolean isRequireSneaking() {
        return getBoolean("require-shift", false);
    }

    public boolean hasTriggerItem() {
        return getConfig().getConfigurationSection("match-item.trigger") != null;
    }

    public ConfigurationSection getTriggerItem() {
        return getSection("match-item.trigger");
    }
}
