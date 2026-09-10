package cn.superiormc.ultimatetweak.objects.matchitem;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Locale;

public class MaterialPattern extends AbstractMatchItemRule {

    @Override
    public boolean getMatch(ConfigurationSection section, ItemStack item, ItemMeta meta) {
        String key = item.getType().getKey().getKey().toLowerCase(Locale.ENGLISH);
        String namespacedKey = item.getType().getKey().toString().toLowerCase(Locale.ENGLISH);
        for (String configured : section.getStringList("material-pattern")) {
            if (configured == null) {
                continue;
            }
            String pattern = configured.trim().toLowerCase(Locale.ENGLISH);
            String value = pattern.indexOf(':') >= 0 ? namespacedKey : key;
            if (globMatches(pattern, value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return section.getStringList("material-pattern").isEmpty();
    }

    static boolean globMatches(String pattern, String value) {
        int patternIndex = 0;
        int valueIndex = 0;
        int starIndex = -1;
        int starValueIndex = -1;
        while (valueIndex < value.length()) {
            if (patternIndex < pattern.length()
                    && (pattern.charAt(patternIndex) == '?'
                    || pattern.charAt(patternIndex) == value.charAt(valueIndex))) {
                patternIndex++;
                valueIndex++;
            } else if (patternIndex < pattern.length() && pattern.charAt(patternIndex) == '*') {
                starIndex = patternIndex++;
                starValueIndex = valueIndex;
            } else if (starIndex >= 0) {
                patternIndex = starIndex + 1;
                valueIndex = ++starValueIndex;
            } else {
                return false;
            }
        }
        while (patternIndex < pattern.length() && pattern.charAt(patternIndex) == '*') {
            patternIndex++;
        }
        return patternIndex == pattern.length();
    }
}
