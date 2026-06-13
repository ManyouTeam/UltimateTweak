package cn.superiormc.ultimatetweak.tweaks.config;

import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DynamicLightConfig extends AbstractTweakConfig {

    private List<LightRule> rules;

    public DynamicLightConfig(File file) {
        super("DynamicLight", file);
        reload();
    }

    @Override
    public void reload() {
        super.reload();
        List<LightRule> loadedRules = new ArrayList<>();
        ConfigurationSection rulesSection = getConfig().getConfigurationSection("rules");
        if (rulesSection != null) {
            for (String key : rulesSection.getKeys(false)) {
                ConfigurationSection ruleSection = rulesSection.getConfigurationSection(key);
                if (ruleSection != null) {
                    addRule(loadedRules, ruleSection.getConfigurationSection("match-item"),
                            ruleSection.getInt("light-level", 15));
                }
            }
        }
        if (loadedRules.isEmpty()) {
            addRule(loadedRules, getConfig().getConfigurationSection("match-item"),
                    getConfig().getInt("light-level", 15));
        }
        rules = Collections.unmodifiableList(loadedRules);
    }

    private void addRule(List<LightRule> loadedRules, ConfigurationSection matchItem, int lightLevel) {
        if (matchItem != null) {
            loadedRules.add(new LightRule(matchItem, Math.max(0, Math.min(15, lightLevel))));
        }
    }

    public List<LightRule> getRules() {
        return rules;
    }

    public int getCheckIntervalTicks() {
        return Math.max(1, getInt("check-interval-ticks", 2));
    }

    public int getViewDistance() {
        return Math.max(0, getInt("view-distance", 64));
    }

    public record LightRule(ConfigurationSection matchItem, int lightLevel) {
    }
}
