package cn.superiormc.ultimatetweak.objects.matchitem;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BlockMaterial extends AbstractMatchItemRule {

    @Override
    public boolean getMatch(ConfigurationSection section, ItemStack item, ItemMeta meta) {
        return item.getType().isBlock() == section.getBoolean("is-block");
    }

    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return !section.contains("is-block");
    }
}
