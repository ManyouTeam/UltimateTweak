package cn.superiormc.ultimatetweak.spigot;

import cn.superiormc.ultimatetweak.utils.SchedulerUtil;
import cn.superiormc.ultimatetweak.utils.SpecialMethodUtil;
import cn.superiormc.ultimatetweak.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.profile.PlayerProfile;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpigotMethodUtil implements SpecialMethodUtil {

    private static final Pattern TEXTURE_URL_PATTERN = Pattern.compile("\"url\"\\s*:\\s*\"(https?://textures\\.minecraft\\.net/texture/[^\"]+)\"");

    @Override
    public String methodID() {
        return "spigot";
    }

    @Override
    public void dispatchCommand(String command) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    @Override
    public void dispatchCommand(Player player, String command) {
        Bukkit.dispatchCommand(player, command);
    }

    @Override
    public void dispatchOpCommand(Player player, String command) {
        boolean playerIsOp = player.isOp();
        try {
            player.setOp(true);
            Bukkit.dispatchCommand(player, command);
        } finally {
            player.setOp(playerIsOp);
        }
    }

    @Override
    public void spawnEntity(Location location, EntityType entity) {
        location.getWorld().spawnEntity(location, entity);
    }

    @Override
    public void playerTeleport(Player player, Location location) {
        player.teleport(location);
    }

    public Map<String, PlayerProfile> playerProfiles = Collections.synchronizedMap(
            new LinkedHashMap<>(256, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, PlayerProfile> eldest) {
                    return size() > 256;
                }
            });

    private URL resolveSkinUrl(String skull) throws Exception {
        if (skull == null) {
            return null;
        }

        String trimmedSkull = skull.trim();
        if (trimmedSkull.isEmpty()) {
            return null;
        }

        if (trimmedSkull.startsWith("http://textures.minecraft.net/texture/")
                || trimmedSkull.startsWith("https://textures.minecraft.net/texture/")) {
            return new URL(trimmedSkull);
        }

        String json = new String(Base64.getDecoder().decode(trimmedSkull), StandardCharsets.UTF_8);
        Matcher matcher = TEXTURE_URL_PATTERN.matcher(json);

        if (matcher.find()) {
            return new URL(matcher.group(1));
        }
        return null;
    }

    @Override
    public void sendChat(Player player, String text) {
        if (player == null) {
            Bukkit.getConsoleSender().sendMessage(TextUtil.parse(text));
        } else {
            player.sendMessage(TextUtil.parse(text, player));
        }
    }

    @Override
    public void sendTitle(Player player, String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        if (player == null) {
            return;
        }
        player.sendTitle(TextUtil.parse(title, player), TextUtil.parse(subTitle, player), fadeIn, stay, fadeOut);
    }

    @Override
    public void sendActionBar(Player player, String message) {
        if (player == null) {
            return;
        }
        player.spigot().sendMessage(
                net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                net.md_5.bungee.api.chat.TextComponent.fromLegacyText(TextUtil.parse(message, player))
        );
    }

    @Override
    public void sendBossBar(Player player,
                            String title,
                            float progress,
                            String color,
                            String style) {
        if (player == null) {
            return;
        }
        BossBar bar = Bukkit.createBossBar(
                TextUtil.parse(title, player),
                color == null ? BarColor.WHITE : BarColor.valueOf(color.toUpperCase()),
                style == null ? BarStyle.SOLID : BarStyle.valueOf(style.toUpperCase())
        );

        bar.setProgress(Math.max(0.0, Math.min(1.0, progress)));
        bar.addPlayer(player);
        bar.setVisible(true);

        SchedulerUtil.runTaskLater(bar::removeAll, 60);
    }

    @Override
    public String legacyParse(String text) {
        if (text == null)
            return "";
        return TextUtil.colorize(text);
    }

    @Override
    public String getItemName(ItemMeta meta) {
        return meta.getDisplayName();
    }

    @Override
    public String getItemItemName(ItemMeta meta) {
        return meta.getItemName();
    }

    @Override
    public List<String> getItemLore(ItemMeta meta) {
        return meta.getLore();
    }

    @Override
    public String getEntityName(LivingEntity entity) {
        if (entity.getCustomName() != null) {
            return entity.getCustomName();
        }
        return entity.getName();
    }

    @Override
    public double getDestroySpeed(Block block, ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir() || !block.isPreferredTool(itemStack)) {
            return 1.0;
        }
        double speed = getToolTierSpeed(itemStack.getType());
        int efficiencyLevel = itemStack.getEnchantmentLevel(Enchantment.EFFICIENCY);
        if (speed > 1.0 && efficiencyLevel > 0) {
            speed += efficiencyLevel * efficiencyLevel + 1;
        }
        return speed;
    }

    private double getToolTierSpeed(Material material) {
        String name = material.name();
        if (name.startsWith("GOLDEN_")) {
            return 12.0;
        }
        if (name.startsWith("NETHERITE_")) {
            return 9.0;
        }
        if (name.startsWith("DIAMOND_")) {
            return 8.0;
        }
        if (name.startsWith("IRON_")) {
            return 6.0;
        }
        if (material == Material.SHEARS) {
            return 5.0;
        }
        if (name.startsWith("STONE_")) {
            return 4.0;
        }
        if (name.startsWith("WOODEN_")) {
            return 2.0;
        }
        return 1.0;
    }
}
