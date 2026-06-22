package cn.superiormc.ultimatetweak.tweaks.config;

import cn.superiormc.ultimatetweak.objects.ObjectAction;
import cn.superiormc.ultimatetweak.objects.ObjectCondition;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class StructureAnnouncerConfig extends AbstractTweakConfig {

    private ObjectCondition conditions;

    private ObjectAction structureEnterActions;

    private Set<String> structures;

    private Map<String, String> structureMessages;

    public StructureAnnouncerConfig(File file) {
        super("StructureAnnouncer", file);
    }

    @Override
    public void reload() {
        super.reload();
        conditions = new ObjectCondition(getSection("conditions"));
        structureEnterActions = new ObjectAction(getSection("structure-enter-actions"));
        structures = normalizeStructures(getConfig().getStringList("structures"));
        structureMessages = normalizeStructureMessages();
    }

    private Set<String> normalizeStructures(List<String> rawStructures) {
        Set<String> result = new HashSet<>();
        for (String rawStructure : rawStructures) {
            result.addAll(normalizeStructure(rawStructure));
        }
        return result;
    }

    private Map<String, String> normalizeStructureMessages() {
        Map<String, String> result = new HashMap<>();
        for (String rawStructure : getSection("structure-messages").getKeys(false)) {
            String message = getSection("structure-messages").getString(rawStructure);
            if (message == null || message.isEmpty()) {
                continue;
            }
            for (String structure : normalizeStructure(rawStructure)) {
                result.put(structure, message);
            }
        }
        return result;
    }

    private Set<String> normalizeStructure(String rawStructure) {
        Set<String> result = new HashSet<>();
        if (rawStructure == null || rawStructure.isBlank()) {
            return result;
        }
        String structure = rawStructure.toLowerCase(Locale.ENGLISH);
        result.add(structure);
        if (structure.startsWith("minecraft:")) {
            result.add(structure.substring("minecraft:".length()));
        } else if (!structure.contains(":")) {
            result.add("minecraft:" + structure);
        }
        return result;
    }

    public ObjectCondition getConditions() {
        return conditions;
    }

    public ObjectAction getStructureEnterActions() {
        return structureEnterActions;
    }

    public boolean isStructureAllowed(String structure) {
        if (structures.isEmpty()) {
            return true;
        }
        String normalizedStructure = structure.toLowerCase(Locale.ENGLISH);
        return structures.contains(normalizedStructure) || structures.contains(getStructureKeyPath(normalizedStructure));
    }

    public int getCheckIntervalTicks() {
        return Math.max(1, getInt("check-interval-ticks", 20));
    }

    public String getMessage(String structure) {
        String normalizedStructure = structure.toLowerCase(Locale.ENGLISH);
        String message = structureMessages.get(normalizedStructure);
        if (message == null) {
            message = structureMessages.get(getStructureKeyPath(normalizedStructure));
        }
        return message == null ? getString("message", "{lang:structure-announcer}") : message;
    }

    private String getStructureKeyPath(String structure) {
        int namespaceSeparator = structure.indexOf(':');
        return namespaceSeparator < 0 ? structure : structure.substring(namespaceSeparator + 1);
    }
}
