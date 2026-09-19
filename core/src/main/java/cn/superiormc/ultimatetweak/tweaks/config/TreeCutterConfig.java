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

    public int getAnimationGlowColor() {
        return getGlowColor("animation.glow-color", "#FFFFFF");
    }

    public String getAnimationDirection() {
        return getString("animation.direction", "random").trim().toLowerCase();
    }

    public int getAnimationDurationTicks(Player player) {
        return Math.max(1, getInt("animation.duration-ticks", 30, player));
    }

    public int getAnimationIntervalTicks(Player player) {
        return Math.max(1, getInt("animation.interval-ticks", 1, player));
    }

    public int getAnimationViewDistance(Player player) {
        return Math.max(1, getInt("animation.view-distance", 48, player));
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

    public double getFallDamageAmount(Player player) {
        return Math.max(0.0, getDouble("animation.fall-damage.damage", 6.0, player));
    }

    public double getFallDamageMinAngle(Player player) {
        return Math.max(0.0, Math.min(92.0, getDouble("animation.fall-damage.min-angle", 15.0, player)));
    }

    public double getFallDamageHitRadius(Player player) {
        return Math.max(0.0, getDouble("animation.fall-damage.hit-radius", 0.5, player));
    }

    public int getFallDamageCheckIntervalTicks(Player player) {
        return Math.max(1, getInt("animation.fall-damage.check-interval-ticks", 2, player));
    }

    @Override
    public boolean shouldHideBreakingBlockFromDamageGlow() {
        return getBoolean("damage-glow.hide-breaking-block", false);
    }

    public double getMiningTimePercentPerLog(Player player) {
        String path = getConfig().contains("mining-time.percent-per-log")
                ? "mining-time.percent-per-log"
                : "mining-time.multiplier-per-log";
        return Math.max(0.0, getDouble(path, 10.0, player));
    }

    @Override
    public double getMiningTimePercentPerBlock(Player player) {
        return getMiningTimePercentPerLog(player);
    }

    @Override
    public double getMaxMiningTimePercent(Player player) {
        String path = getConfig().contains("mining-time.max-percent")
                ? "mining-time.max-percent"
                : "mining-time.max-multiplier";
        return Math.max(0.0, getDouble(path, 500.0, player));
    }

    @Override
    public int getDamageGlowViewDistance(Player player) {
        return Math.max(1, getInt("damage-glow.view-distance", getAnimationViewDistance(player), player));
    }
}
