package cn.superiormc.ultimatetweak.tweaks.structureannouncer;

import cn.superiormc.ultimatetweak.UltimateTweak;
import cn.superiormc.ultimatetweak.tweaks.AbstractTweak;
import cn.superiormc.ultimatetweak.tweaks.TweakEventType;
import cn.superiormc.ultimatetweak.tweaks.config.StructureAnnouncerConfig;
import cn.superiormc.ultimatetweak.utils.CommonUtil;
import cn.superiormc.ultimatetweak.utils.SchedulerUtil;
import cn.superiormc.ultimatetweak.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.generator.structure.GeneratedStructure;
import org.bukkit.util.Vector;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StructureAnnouncerTweak extends AbstractTweak<StructureAnnouncerConfig> {

    private final Map<UUID, String> lastStructures = new ConcurrentHashMap<>();

    private SchedulerUtil task;

    public StructureAnnouncerTweak(StructureAnnouncerConfig config) {
        super("StructureAnnouncer", config);
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
        lastStructures.remove(event.getPlayer().getUniqueId());
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
        lastStructures.clear();
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
            lastStructures.remove(player.getUniqueId());
            return;
        }

        Optional<String> currentStructure = getCurrentStructure(player);
        UUID playerId = player.getUniqueId();
        String previousStructure = lastStructures.get(playerId);
        if (currentStructure.isEmpty()) {
            lastStructures.remove(playerId);
            return;
        }

        String structure = currentStructure.get();
        lastStructures.put(playerId, structure);
        if (!forceAnnounce && structure.equals(previousStructure)) {
            return;
        }
        announceStructure(player, previousStructure == null ? "" : previousStructure, structure);
    }

    private Optional<String> getCurrentStructure(Player player) {
        Location location = player.getLocation();
        Vector position = location.toVector();
        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;
        return player.getWorld().getStructures(chunkX, chunkZ).stream()
                .filter(structure -> structure.getBoundingBox().contains(position))
                .map(this::normalizeStructure)
                .filter(getConfig()::isStructureAllowed)
                .min(Comparator.naturalOrder());
    }

    private void announceStructure(Player player, String fromStructure, String toStructure) {
        if (!getConfig().getConditions().getAllBoolean(player)) {
            return;
        }

        String message = CommonUtil.modifyString(player, getConfig().getMessage(toStructure),
                "structure", toStructure,
                "from-structure", fromStructure,
                "to-structure", toStructure);
        TextUtil.sendMessage(player, TextUtil.withPAPI(message, player));
        getConfig().getStructureEnterActions().runAllActions(player,
                "structure", toStructure,
                "from-structure", fromStructure,
                "to-structure", toStructure);
    }

    private String normalizeStructure(GeneratedStructure structure) {
        return structure.getStructure().getKey().toString().toLowerCase(Locale.ENGLISH);
    }
}
