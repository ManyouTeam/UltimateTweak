package cn.superiormc.ultimatetweak.tweaks.biomeannouncer;

import cn.superiormc.ultimatetweak.UltimateTweak;
import cn.superiormc.ultimatetweak.tweaks.AbstractTweak;
import cn.superiormc.ultimatetweak.tweaks.TweakEventType;
import cn.superiormc.ultimatetweak.tweaks.config.BiomeAnnouncerConfig;
import cn.superiormc.ultimatetweak.utils.CommonUtil;
import cn.superiormc.ultimatetweak.utils.SchedulerUtil;
import cn.superiormc.ultimatetweak.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BiomeAnnouncerTweak extends AbstractTweak<BiomeAnnouncerConfig> {

    private final Map<UUID, Biome> lastBiomes = new ConcurrentHashMap<>();

    private SchedulerUtil task;

    public BiomeAnnouncerTweak(BiomeAnnouncerConfig config) {
        super("BiomeAnnouncer", config);
    }

    @Override
    public Set<TweakEventType> getEventTypes() {
        return EnumSet.of(TweakEventType.PLAYER_JOIN, TweakEventType.PLAYER_QUIT);
    }

    @Override
    public void onLoad() {
        startTask();
    }

    @Override
    public void onReload() {
        stopTask();
        super.onReload();
        if (isEnabled()) {
            startTask();
        }
    }

    @Override
    public void onDisable() {
        stopTask();
    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        removePlayer(event.getPlayer().getUniqueId());
    }

    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        SchedulerUtil.runSync(event.getPlayer(), () -> updatePlayer(event.getPlayer(), true));
    }

    private void startTask() {
        if (task != null) {
            return;
        }
        task = SchedulerUtil.runTaskTimer(this::tick, 1L, getConfig().getCheckIntervalTicks());
    }

    private void stopTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        lastBiomes.clear();
    }

    private void removePlayer(UUID playerId) {
        lastBiomes.remove(playerId);
    }

    private void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (UltimateTweak.isFolia) {
                SchedulerUtil.runSync(player, () -> updatePlayer(player, false));
            } else {
                updatePlayer(player, false);
            }
        }
    }

    private void updatePlayer(Player player, boolean forceAnnounce) {
        if (!player.isOnline()) {
            removePlayer(player.getUniqueId());
            return;
        }
        if (!isWorldEnabled(player.getWorld())) {
            removePlayer(player.getUniqueId());
            return;
        }

        Biome currentBiome = player.getLocation().getBlock().getBiome();
        UUID playerId = player.getUniqueId();
        Biome previousBiome = lastBiomes.put(playerId, currentBiome);
        if (!forceAnnounce && previousBiome != null
                && normalizeBiome(previousBiome).equals(normalizeBiome(currentBiome))) {
            return;
        }

        announceBiome(player, previousBiome == null ? currentBiome : previousBiome, currentBiome);
    }

    private void announceBiome(Player player, Biome fromBiome, Biome toBiome) {
        if (!getConfig().getConditions().getAllBoolean(player)) {
            return;
        }

        String toBiomeId = normalizeBiome(toBiome);
        if (!getConfig().isBiomeAllowed(toBiomeId)) {
            return;
        }

        String fromBiomeName = "";
        if (UltimateTweak.methodUtil.methodID().equals("paper")) {
            fromBiomeName = "<lang:" + fromBiome.translationKey() + ">";
        }
        String toBiomeName = "";
        if (UltimateTweak.methodUtil.methodID().equals("paper")) {
            toBiomeName = "<lang:" + toBiome.translationKey() + ">";
        }
        String fromBiomeId = normalizeBiome(fromBiome);
        String message = CommonUtil.modifyString(player, getConfig().getMessage(toBiomeId),
                "biome", toBiomeId,
                "from-biome", fromBiomeId,
                "to-biome", toBiomeId,
                "biome-name", toBiomeName,
                "from-biome-name", fromBiomeName,
                "to-biome-name", toBiomeName);
        TextUtil.sendMessage(player, TextUtil.withPAPI(message, player));
        getConfig().getBiomeChangeActions().runAllActions(player,
                "biome", toBiomeId,
                "from-biome", fromBiomeId,
                "to-biome", toBiomeId,
                "biome-name", toBiomeName,
                "from-biome-name", fromBiomeName,
                "to-biome-name", toBiomeName);
    }

    private String normalizeBiome(Biome biome) {
        return biome.getKey().toString().toLowerCase(Locale.ENGLISH);
    }
}
