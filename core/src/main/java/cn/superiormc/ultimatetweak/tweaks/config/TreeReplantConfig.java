package cn.superiormc.ultimatetweak.tweaks.config;

import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class TreeReplantConfig extends AbstractTweakConfig {

    private List<ReplantRule> rules = Collections.emptyList();

    public TreeReplantConfig(File file) {
        super("TreeReplant", file);
        reload();
    }

    @Override
    public void reload() {
        super.reload();
        List<ReplantRule> loadedRules = new ArrayList<>();
        ConfigurationSection rulesSection = getConfig().getConfigurationSection("rules");
        if (rulesSection != null) {
            for (String key : rulesSection.getKeys(false)) {
                ConfigurationSection ruleSection = rulesSection.getConfigurationSection(key);
                if (ruleSection != null) {
                    addRule(loadedRules, ruleSection);
                }
            }
        }
        if (loadedRules.isEmpty()) {
            addRule(loadedRules, getConfig());
        }
        rules = Collections.unmodifiableList(loadedRules);
    }

    private void addRule(List<ReplantRule> loadedRules, ConfigurationSection ruleSection) {
        List<SaplingDefinition> saplings = new ArrayList<>();
        ConfigurationSection saplingsSection = ruleSection.getConfigurationSection("saplings");
        if (saplingsSection != null) {
            for (String key : saplingsSection.getKeys(false)) {
                ConfigurationSection sapling = saplingsSection.getConfigurationSection(key);
                if (sapling == null) {
                    continue;
                }
                String block = sapling.getString("block", "");
                ConfigurationSection item = sapling.getConfigurationSection("match-item");
                if (!block.isEmpty() && item != null) {
                    saplings.add(new SaplingDefinition(item, block));
                }
            }
        }
        List<String> configuredSoils = ruleSection.getStringList("valid-soilds");
        if (configuredSoils.isEmpty()) {
            configuredSoils = ruleSection.getStringList("valid-soils");
        }
        Set<String> validSoils = new LinkedHashSet<>(configuredSoils);
        if (!saplings.isEmpty() && !validSoils.isEmpty()) {
            loadedRules.add(new ReplantRule(
                    Collections.unmodifiableList(saplings),
                    Collections.unmodifiableSet(validSoils)));
        }
    }

    public int getCheckIntervalTicks() {
        return Math.max(1, getInt("check-interval-ticks", 10));
    }

    public int getMaxWaitTicks() {
        return Math.max(getCheckIntervalTicks(), getInt("max-wait-ticks", 600));
    }

    public int getCooldownTicks() {
        return Math.max(0, getInt("cooldown-ticks", 20));
    }

    public List<ReplantRule> getRules() {
        return rules;
    }

    public record SaplingDefinition(ConfigurationSection item, String block) {
    }

    public record ReplantRule(List<SaplingDefinition> saplings, Set<String> validSoils) {
    }
}
