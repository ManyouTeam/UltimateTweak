package cn.superiormc.ultimatetweak.paper;

import cn.superiormc.ultimatetweak.UltimateTweak;
import cn.superiormc.ultimatetweak.managers.ConfigManager;
import cn.superiormc.ultimatetweak.paper.utils.PaperTextUtil;
import cn.superiormc.ultimatetweak.utils.SchedulerUtil;
import cn.superiormc.ultimatetweak.utils.SpecialMethodUtil;
import cn.superiormc.ultimatetweak.utils.TextUtil;
import com.destroystokyo.paper.profile.PlayerProfile;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class PaperMethodUtil implements SpecialMethodUtil {

    @Override
    public String methodID() {
        return "paper";
    }

    @Override
    public void dispatchCommand(String command) {
        if (UltimateTweak.isFolia) {
            Bukkit.getGlobalRegionScheduler().run(UltimateTweak.instance, task -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
            return;
        }
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    @Override
    public void dispatchCommand(Player player, String command) {
        if (UltimateTweak.isFolia) {
            player.getScheduler().run(UltimateTweak.instance, task -> Bukkit.dispatchCommand(player, command), () -> {
            });
            return;
        }
        Bukkit.dispatchCommand(player, command);
    }

    @Override
    public void dispatchOpCommand(Player player, String command) {
        if (UltimateTweak.isFolia) {
            player.getScheduler().run(UltimateTweak.instance, task -> {
                boolean playerIsOp = player.isOp();
                try {
                    player.setOp(true);
                    Bukkit.dispatchCommand(player, command);
                } finally {
                    player.setOp(playerIsOp);
                }
            }, () -> {
            });
            return;
        }
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
        if (UltimateTweak.isFolia) {
            Bukkit.getRegionScheduler().run(UltimateTweak.instance, location, task -> location.getWorld().spawnEntity(location, entity));
            return;
        }
        location.getWorld().spawnEntity(location, entity);
    }

    @Override
    public void playerTeleport(Player player, Location location) {
        if (UltimateTweak.isFolia) {
            player.teleportAsync(location);
        } else {
            player.teleport(location);
        }
    }

    public Map<String, PlayerProfile> playerProfiles = Collections.synchronizedMap(
            new LinkedHashMap<>(256, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, PlayerProfile> eldest) {
                    return size() > 256;
                }
            });

    @Override
    public void sendChat(Player player, String text) {
        if (player == null) {
            Bukkit.getConsoleSender().sendMessage(PaperTextUtil.modernParse(text));
        } else {
            player.sendMessage(PaperTextUtil.modernParse(text, player));
        }
    }

    @Override
    public void sendTitle(Player player, String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        if (player == null) {
            return;
        }
        player.showTitle(Title.title(PaperTextUtil.modernParse(title, player),
                PaperTextUtil.modernParse(subTitle, player),
                Title.Times.times(Ticks.duration(fadeIn),
                        Ticks.duration(stay),
                        Ticks.duration(fadeOut))));
    }

    @Override
    public void sendActionBar(Player player, String message) {
        if (player == null) {
            return;
        }
        player.sendActionBar(PaperTextUtil.modernParse(message, player));
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

        if (style != null && style.equalsIgnoreCase("SOLID")) {
            style = "PROGRESS";
        }

        BossBar bar = BossBar.bossBar(
                title == null ? Component.empty() : PaperTextUtil.modernParse(title, player),
                Math.max(0f, Math.min(1f, progress)),
                color == null ? BossBar.Color.PINK : BossBar.Color.valueOf(color.toUpperCase()),
                style == null ? BossBar.Overlay.PROGRESS : BossBar.Overlay.valueOf(style.toUpperCase())
        );

        player.showBossBar(bar);
        SchedulerUtil.runTaskLater(() -> player.hideBossBar(bar), 60);
    }

    @Override
    public String legacyParse(String text) {
        if (text == null) {
            return "";
        }
        if (!ConfigManager.configManager.getBoolean("config-files.force-parse-mini-message")) {
            return TextUtil.colorize(text);
        }
        return LegacyComponentSerializer.legacySection().serialize(PaperTextUtil.modernParse(text));
    }

    @Override
    public String getItemName(ItemMeta meta) {
        return PaperTextUtil.changeToString(meta.displayName());
    }

    @Override
    public String getItemItemName(ItemMeta meta) {
        return PaperTextUtil.changeToString(meta.itemName());
    }

    @Override
    public List<String> getItemLore(ItemMeta meta) {
        return PaperTextUtil.changeToString(meta.lore());
    }

    @Override
    public String getEntityName(LivingEntity entity) {
        if (entity.customName() != null) {
            return PaperTextUtil.changeToString(entity.customName());
        }
        return PaperTextUtil.changeToString(entity.name());
    }

    @Override
    public double getDestroySpeed(Block block, ItemStack itemStack) {
        return block.getDestroySpeed(itemStack, true);
    }
}
