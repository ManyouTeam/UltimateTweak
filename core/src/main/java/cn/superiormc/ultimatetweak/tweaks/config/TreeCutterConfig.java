package cn.superiormc.ultimatetweak.tweaks.config;

import cn.superiormc.ultimatetweak.managers.ConfigManager;
import cn.superiormc.ultimatetweak.managers.MatchItemManager;
import cn.superiormc.ultimatetweak.managers.TreeDetermineManager.TreeDefinition;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class TreeCutterConfig extends AbstractMultiBlockConfig {

    public TreeCutterConfig(File file) {
        super("TreeCutter", file);
        reload();
    }

    public List<TreeDefinition> getTreeDefinitions() {
        if (ConfigManager.configManager == null) {
            return Collections.emptyList();
        }
        return ConfigManager.configManager.getTreeDefinitions();
    }

    public boolean shouldBreakLeaves(Player player) {
        return getConfig().getConfigurationSection("match-item.leaf-break") != null
                && MatchItemManager.matchItemManager.getMatch(
                getSection("match-item.leaf-break"),
                player.getInventory().getItemInMainHand()
        );
    }

    public boolean isAnimationEnabled() {
        return getBoolean("animation.enabled", true);
    }

    public boolean isAnimationGlowEnabled() {
        return getBoolean("animation.glow", false);
    }

    public int getCooldownTicks() {
        return Math.max(0, getInt("cooldown-tick", 5));
    }

    public int getAnimationGlowColor() {
        return getGlowColor("animation.glow-color", "#FFFFFF");
    }

    public String getAnimationDirection() {
        return getString("animation.direction", "random").trim().toLowerCase();
    }

    public int getAnimationDurationTicks() {
        return Math.max(1, getInt("animation.duration-ticks", 30));
    }

    public int getAnimationIntervalTicks() {
        return Math.max(1, getInt("animation.interval-ticks", 1));
    }

    public int getAnimationViewDistance() {
        return Math.max(1, getInt("animation.view-distance", 48));
    }

    public boolean isFallDamageEnabled() {
        return getBoolean("animation.fall-damage.enabled", false);
    }

    public boolean shouldFallDamagePlayers() {
        return getBoolean("animation.fall-damage.players", true);
    }

    public boolean shouldFallDamageEntities() {
        return getBoolean("animation.fall-damage.entities", true);
    }

    public double getFallDamageAmount() {
        return Math.max(0.0, getDouble("animation.fall-damage.damage", 6.0));
    }

    public double getFallDamageMinAngle() {
        return Math.max(0.0, Math.min(92.0, getDouble("animation.fall-damage.min-angle", 15.0)));
    }

    public double getFallDamageHitRadius() {
        return Math.max(0.0, getDouble("animation.fall-damage.hit-radius", 0.5));
    }

    @Override
    public boolean shouldHideBreakingBlockFromDamageGlow() {
        return getBoolean("damage-glow.hide-breaking-block", false);
    }

    public double getMiningTimePercentPerLog() {
        String path = getConfig().contains("mining-time.percent-per-log")
                ? "mining-time.percent-per-log"
                : "mining-time.multiplier-per-log";
        return Math.max(0.0, getDouble(path, 10.0));
    }

    @Override
    public double getMiningTimePercentPerBlock() {
        return getMiningTimePercentPerLog();
    }

    @Override
    public double getMaxMiningTimePercent() {
        String path = getConfig().contains("mining-time.max-percent")
                ? "mining-time.max-percent"
                : "mining-time.max-multiplier";
        return Math.max(0.0, getDouble(path, 500.0));
    }

    @Override
    public int getDamageGlowViewDistance() {
        return Math.max(1, getInt("damage-glow.view-distance", getAnimationViewDistance()));
    }
}
