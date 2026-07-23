package cn.superiormc.ultimatetweak.tweaks.dynamiclight;

import cn.superiormc.ultimatetweak.UltimateTweak;
import cn.superiormc.ultimatetweak.managers.MatchItemManager;
import cn.superiormc.ultimatetweak.tweaks.AbstractTweak;
import cn.superiormc.ultimatetweak.tweaks.TweakEventType;
import cn.superiormc.ultimatetweak.tweaks.config.DynamicLightConfig;
import cn.superiormc.ultimatetweak.tweaks.config.DynamicLightConfig.LightRule;
import cn.superiormc.ultimatetweak.utils.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Light;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.PlayerInventory;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DynamicLightTweak extends AbstractTweak<DynamicLightConfig> {

    private static final BlockFace[] ADJACENT_FACES = {
            BlockFace.UP,
            BlockFace.DOWN,
            BlockFace.NORTH,
            BlockFace.SOUTH,
            BlockFace.EAST,
            BlockFace.WEST
    };

    private final Map<UUID, LightSource> activeLights = new ConcurrentHashMap<>();

    private volatile Map<LightLocation, Integer> activeLightLevels = Map.of();

    private final Map<UUID, Map<LightLocation, Integer>> visibleLights = new ConcurrentHashMap<>();

    private SchedulerUtil task;

    public DynamicLightTweak(DynamicLightConfig config) {
        super("DynamicLight", config);
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
    public void onPlayerJoin(PlayerJoinEvent event) {
        SchedulerUtil.runSync(event.getPlayer(), () -> updatePlayer(event.getPlayer()));
    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        removePlayerLight(event.getPlayer().getUniqueId());
        visibleLights.remove(event.getPlayer().getUniqueId());
    }

    private void startTask() {
        if (task != null) {
            return;
        }
        int interval = getConfig().getCheckIntervalTicks();
        task = SchedulerUtil.runTaskTimer(this::tick, 1L, interval);
    }

    private void stopTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        synchronized (activeLights) {
            activeLights.clear();
            activeLightLevels = Map.of();
        }
        for (Map.Entry<UUID, Map<LightLocation, Integer>> entry : visibleLights.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null) {
                continue;
            }
            Set<LightLocation> locations = Set.copyOf(entry.getValue().keySet());
            SchedulerUtil.runSync(player, () -> locations.forEach(location -> restoreRealBlock(player, location)));
        }
        visibleLights.clear();
    }

    private void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (UltimateTweak.isFolia) {
                SchedulerUtil.runSync(player, () -> updatePlayer(player));
            } else {
                updatePlayer(player);
            }
        }
    }

    private void updatePlayer(Player player) {
        if (!isWorldEnabled(player.getWorld())) {
            removePlayerLight(player.getUniqueId());
            visibleLights.remove(player.getUniqueId());
            return;
        }
        updateLight(player);
        syncViewerLights(player);
    }

    private void updateLight(Player player) {
        if (!player.isOnline()) {
            removePlayerLight(player.getUniqueId());
            return;
        }

        int lightLevel = getLightLevel(player);
        Block target = lightLevel > 0 ? getTargetBlock(player) : null;
        LightSource desired = target == null ? null : new LightSource(LightLocation.of(target), lightLevel);

        synchronized (activeLights) {
            LightSource current = activeLights.get(player.getUniqueId());
            if (Objects.equals(current, desired)) {
                return;
            }
            if (desired == null) {
                activeLights.remove(player.getUniqueId());
            } else {
                activeLights.put(player.getUniqueId(), desired);
            }
            if (getConfig().getViewDistance() > 0) {
                rebuildActiveLightLevelsLocked();
            }
        }
    }

    private void removePlayerLight(UUID playerId) {
        synchronized (activeLights) {
            if (activeLights.remove(playerId) != null && getConfig().getViewDistance() > 0) {
                rebuildActiveLightLevelsLocked();
            }
        }
    }

    private Map<LightLocation, Integer> getActiveLightLevels() {
        return activeLightLevels;
    }

    private void rebuildActiveLightLevelsLocked() {
        Map<LightLocation, Integer> result = new LinkedHashMap<>();
        for (LightSource light : activeLights.values()) {
            result.merge(light.location(), light.level(), Math::max);
        }
        activeLightLevels = Map.copyOf(result);
    }

    private void sendLight(Player viewer, LightLocation location, int level) {
        World world = Bukkit.getWorld(location.worldId());
        if (world == null || !viewer.isOnline() || !viewer.getWorld().equals(world)) {
            return;
        }
        Light light = (Light) Material.LIGHT.createBlockData();
        light.setLevel(level);
        light.setWaterlogged(world.getBlockAt(location.x(), location.y(), location.z()).getType() == Material.WATER);
        viewer.sendBlockChange(new org.bukkit.Location(
                world, location.x(), location.y(), location.z()), light);
    }

    private Block getTargetBlock(Player player) {
        Block block = player.getEyeLocation().getBlock();
        if (canPlaceLight(block)) {
            return block;
        }
        if (!player.isInWater()) {
            return null;
        }
        for (BlockFace face : ADJACENT_FACES) {
            Block adjacent = block.getRelative(face);
            if (canPlaceLight(adjacent)) {
                return adjacent;
            }
        }
        return null;
    }

    private boolean canPlaceLight(Block block) {
        return block.isEmpty() || block.getType() == Material.WATER;
    }

    private int getLightLevel(Player player) {
        PlayerInventory inventory = player.getInventory();
        int level = 0;
        for (LightRule rule : getConfig().getRules()) {
            if (MatchItemManager.matchItemManager.getMatch(rule.matchItem(), inventory.getItemInMainHand())
                    || MatchItemManager.matchItemManager.getMatch(rule.matchItem(), inventory.getItemInOffHand())) {
                level = Math.max(level, rule.lightLevel());
            }
        }
        return level;
    }

    private void syncViewerLights(Player player) {
        Map<LightLocation, Integer> desired = new LinkedHashMap<>();
        if (getConfig().getViewDistance() == 0) {
            LightSource ownLight = activeLights.get(player.getUniqueId());
            if (ownLight != null) {
                desired.put(ownLight.location(), ownLight.level());
            }
        } else {
            for (Map.Entry<LightLocation, Integer> entry : getActiveLightLevels().entrySet()) {
                if (isVisibleTo(player, entry.getKey())) {
                    desired.put(entry.getKey(), entry.getValue());
                }
            }
        }

        Map<LightLocation, Integer> previous = visibleLights.getOrDefault(player.getUniqueId(), Map.of());
        for (LightLocation location : previous.keySet()) {
            if (!desired.containsKey(location)) {
                restoreRealBlock(player, location);
            }
        }
        for (Map.Entry<LightLocation, Integer> entry : desired.entrySet()) {
            if (!Objects.equals(previous.get(entry.getKey()), entry.getValue())) {
                sendLight(player, entry.getKey(), entry.getValue());
            }
        }
        if (desired.isEmpty()) {
            visibleLights.remove(player.getUniqueId());
        } else {
            visibleLights.put(player.getUniqueId(), Map.copyOf(desired));
        }
    }

    private boolean isVisibleTo(Player player, LightLocation location) {
        World world = Bukkit.getWorld(location.worldId());
        if (world == null || !player.isOnline() || !player.getWorld().equals(world)) {
            return false;
        }
        double viewDistance = getConfig().getViewDistance();
        double dx = player.getLocation().getX() - (location.x() + 0.5);
        double dy = player.getLocation().getY() - (location.y() + 0.5);
        double dz = player.getLocation().getZ() - (location.z() + 0.5);
        return dx * dx + dy * dy + dz * dz <= viewDistance * viewDistance;
    }

    private void restoreRealBlock(Player player, LightLocation location) {
        World world = Bukkit.getWorld(location.worldId());
        if (world == null || !player.getWorld().equals(world)) {
            return;
        }
        Block block = world.getBlockAt(location.x(), location.y(), location.z());
        player.sendBlockChange(block.getLocation(), block.getBlockData());
    }

    private record LightLocation(UUID worldId, int x, int y, int z) {

        private static LightLocation of(Block block) {
            return new LightLocation(block.getWorld().getUID(), block.getX(), block.getY(), block.getZ());
        }
    }

    private record LightSource(LightLocation location, int level) {
    }
}
