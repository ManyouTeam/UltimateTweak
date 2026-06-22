package cn.superiormc.ultimatetweak.tweaks.config;

import cn.superiormc.ultimatetweak.objects.ObjectCondition;
import cn.superiormc.ultimatetweak.objects.ObjectAction;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class BiomeAnnouncerConfig extends AbstractTweakConfig {

    private ObjectCondition conditions;

    private ObjectAction biomeChangeActions;

    private Set<String> biomes;

    private Map<String, String> biomeMessages;

    public BiomeAnnouncerConfig(File file) {
        super("BiomeAnnouncer", file);
    }

    @Override
    public void reload() {
        super.reload();
        conditions = new ObjectCondition(getSection("conditions"));
        biomeChangeActions = new ObjectAction(getSection("biome-change-actions"));
        biomes = normalizeBiomes(getConfig().getStringList("biomes"));
        biomeMessages = normalizeBiomeMessages();
    }

    private Set<String> normalizeBiomes(List<String> rawBiomes) {
        Set<String> result = new HashSet<>();
        for (String rawBiome : rawBiomes) {
            result.addAll(normalizeBiome(rawBiome));
        }
        return result;
    }

    private Map<String, String> normalizeBiomeMessages() {
        Map<String, String> result = new HashMap<>();
        for (String rawBiome : getSection("biome-messages").getKeys(false)) {
            String message = getSection("biome-messages").getString(rawBiome);
            if (message == null || message.isEmpty()) {
                continue;
            }
            for (String biome : normalizeBiome(rawBiome)) {
                result.put(biome, message);
            }
        }
        return result;
    }

    private Set<String> normalizeBiome(String rawBiome) {
        Set<String> result = new HashSet<>();
        if (rawBiome == null || rawBiome.isBlank()) {
            return result;
        }
        String biome = rawBiome.toLowerCase(Locale.ENGLISH);
        result.add(biome);
        if (biome.startsWith("minecraft:")) {
            result.add(biome.substring("minecraft:".length()));
        } else if (!biome.contains(":")) {
            result.add("minecraft:" + biome);
        }
        return result;
    }

    public ObjectCondition getConditions() {
        return conditions;
    }

    public ObjectAction getBiomeChangeActions() {
        return biomeChangeActions;
    }

    public boolean isBiomeAllowed(String biome) {
        return biomes.isEmpty() || biomes.contains(biome.toLowerCase(Locale.ENGLISH));
    }

    public int getCheckIntervalTicks() {
        return Math.max(1, getInt("check-interval-ticks", 20));
    }

    public String getMessage(String biome) {
        String message = biomeMessages.get(biome.toLowerCase(Locale.ENGLISH));
        return message == null ? getString("message", "{lang:biome-announcer}") : message;
    }
}
