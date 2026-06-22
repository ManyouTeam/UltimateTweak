package cn.superiormc.ultimatetweak.tweaks.config;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class StructureAutoProtectConfig extends AbstractTweakConfig {

    private Set<String> structures;

    public StructureAutoProtectConfig(File file) {
        super("StructureAutoProtect", file);
    }

    @Override
    public void reload() {
        super.reload();
        structures = normalizeStructures(getConfig().getStringList("structures"));
    }

    private Set<String> normalizeStructures(List<String> rawStructures) {
        Set<String> result = new HashSet<>();
        for (String rawStructure : rawStructures) {
            if (rawStructure == null || rawStructure.isBlank()) {
                continue;
            }
            String structure = rawStructure.toLowerCase(Locale.ENGLISH);
            result.add(structure);
            if (structure.startsWith("minecraft:")) {
                result.add(structure.substring("minecraft:".length()));
            } else if (!structure.contains(":")) {
                result.add("minecraft:" + structure);
            }
        }
        return result;
    }

    public boolean isStructureAllowed(String structure) {
        return structures.isEmpty() || structures.contains(structure.toLowerCase(Locale.ENGLISH));
    }

    public int getCheckIntervalTicks() {
        return Math.max(1, getInt("check-interval-ticks", 100));
    }

    public String getProtectionHook() {
        return getString("protection-hook", "WorldGuard");
    }

    public String getRegionIdPrefix() {
        return getString("region-id-prefix", "ut_structure_");
    }

    public int getExpandY() {
        return Math.max(0, getInt("expand-y", 0));
    }

    public boolean isDebugEnabled() {
        return getBoolean("debug", false);
    }
}
