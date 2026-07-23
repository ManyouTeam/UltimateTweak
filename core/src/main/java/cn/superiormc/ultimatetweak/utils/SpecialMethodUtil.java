package cn.superiormc.ultimatetweak.utils;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

public interface SpecialMethodUtil {

    String methodID();

    void dispatchCommand(String command);

    void dispatchCommand(Player player, String command);

    void dispatchOpCommand(Player player, String command);

    void spawnEntity(Location location, EntityType entity);

    void playerTeleport(Player player, Location location);

    void sendChat(Player player, String text);

    void sendTitle(Player player, String title, String subTitle, int fadeIn, int stay, int fadeOut);

    void sendActionBar(Player player, String message);

    void sendBossBar(Player player,
                     String title,
                     float progress,
                     String color,
                     String style);

    String legacyParse(String text);

    String getItemName(ItemMeta meta);

    String getItemItemName(ItemMeta meta);

    List<String> getItemLore(ItemMeta meta);

    String getEntityName(LivingEntity entity);

    double getDestroySpeed(Block block, ItemStack itemStack);
}
