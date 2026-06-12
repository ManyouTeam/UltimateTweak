package cn.superiormc.ultimatetweak.tweaks.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.configuration.ConfigurationSection;

public class VeinMineConfig extends AbstractMultiBlockConfig {

    private List<VeinRule> rules;

    public VeinMineConfig(File file) {
        super("VeinMine", file);
        reload();
    }

    @Override
    public void reload() {
        super.reload();
        List<VeinRule> loadedRules = new ArrayList<>();
        ConfigurationSection rulesSection = getConfig().getConfigurationSection("rules");
        if (rulesSection != null) {
            for (String key : rulesSection.getKeys(false)) {
                ConfigurationSection ruleSection = rulesSection.getConfigurationSection(key);
                if (ruleSection != null) {
                    addRule(loadedRules, ruleSection.getConfigurationSection("match-item"),
                            ruleSection.getStringList("match-block"));
                }
            }
        }
        if (loadedRules.isEmpty()) {
            addRule(loadedRules, getConfig().getConfigurationSection("match-item.trigger"),
                    getConfig().getStringList("match-block"));
        }
        rules = Collections.unmodifiableList(loadedRules);
    }

    private void addRule(List<VeinRule> loadedRules, ConfigurationSection matchItem, List<String> matchBlocks) {
        Set<String> blocks = new LinkedHashSet<>(matchBlocks);
        if (matchItem != null && !blocks.isEmpty()) {
            loadedRules.add(new VeinRule(matchItem, Collections.unmodifiableSet(blocks)));
        }
    }

    public List<VeinRule> getRules() {
        return rules;
    }

    public int getMaxBlocks() {
        return Math.max(1, getInt("search.max-blocks", 64));
    }

    public boolean shouldSearchDiagonally() {
        return getBoolean("search.diagonal", true);
    }

    @Override
    public double getMiningTimePercentPerBlock() {
        return Math.max(0.0, getDouble("mining-time.percent-per-block", 100.0));
    }

    public record VeinRule(ConfigurationSection matchItem, Set<String> matchBlocks) {
    }
}
