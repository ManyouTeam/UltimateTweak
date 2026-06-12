package cn.superiormc.ultimatetweak.tweaks.dynamiclight;

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
import java.util.LinkedHashSet;
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
        SchedulerUtil.runSync(event.getPlayer(), () -> {
            for (Map.Entry<LightLocation, Integer> light : getActiveLightLevels().entrySet()) {
                sendLight(event.getPlayer(), light.getKey(), light.getValue());
            }
        });
    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        removePlayerLight(event.getPlayer().getUniqueId());
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
        Set<LightLocation> locations = getActiveLightLevels().keySet();
        synchronized (activeLights) {
            activeLights.clear();
        }
        for (LightLocation location : locations) {
            broadcastRestore(location);
        }
    }

    private void tick() {
        for (Map.Entry<LightLocation, Integer> light : getActiveLightLevels().entrySet()) {
            broadcastLight(light.getKey(), light.getValue());
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            SchedulerUtil.runSync(player, () -> updateLight(player));
        }
    }

    private void updateLight(Player player) {
        if (!player.isOnline()) {
            removePlayerLight(player.getUniqueId());
            return;
        }

        int lightLevel = getLightLevel(player);
        Block target = lightLevel > 0 ? getTargetBlock(player) : null;
        LightSource desired = target == null ? null : new LightSource(LightLocation.of(target), lightLevel);
        Set<LightLocation> affected = new LinkedHashSet<>();
        Map<LightLocation, Integer> before = new LinkedHashMap<>();
        Map<LightLocation, Integer> after = new LinkedHashMap<>();

        synchronized (activeLights) {
            LightSource current = activeLights.get(player.getUniqueId());
            if (Objects.equals(current, desired)) {
                return;
            }
            if (current != null) {
                affected.add(current.location());
            }
            if (desired != null) {
                affected.add(desired.location());
            }
            for (LightLocation location : affected) {
                before.put(location, getLightLevelAtLocked(location));
            }
            if (desired == null) {
                activeLights.remove(player.getUniqueId());
            } else {
                activeLights.put(player.getUniqueId(), desired);
            }
            for (LightLocation location : affected) {
                after.put(location, getLightLevelAtLocked(location));
            }
        }
        publishChanges(affected, before, after);
    }

    private void removePlayerLight(UUID playerId) {
        LightLocation location;
        int before;
        int after;
        synchronized (activeLights) {
            LightSource current = activeLights.get(playerId);
            if (current == null) {
                return;
            }
            location = current.location();
            before = getLightLevelAtLocked(location);
            activeLights.remove(playerId);
            after = getLightLevelAtLocked(location);
        }
        if (before != after) {
            if (after > 0) {
                broadcastLight(location, after);
            } else {
                broadcastRestore(location);
            }
        }
    }

    private Map<LightLocation, Integer> getActiveLightLevels() {
        synchronized (activeLights) {
            Map<LightLocation, Integer> result = new LinkedHashMap<>();
            for (LightSource light : activeLights.values()) {
                result.merge(light.location(), light.level(), Math::max);
            }
            return result;
        }
    }

    private int getLightLevelAtLocked(LightLocation location) {
        int result = 0;
        for (LightSource light : activeLights.values()) {
            if (light.location().equals(location)) {
                result = Math.max(result, light.level());
            }
        }
        return result;
    }

    private void publishChanges(Set<LightLocation> affected, Map<LightLocation, Integer> before,
                                Map<LightLocation, Integer> after) {
        for (LightLocation location : affected) {
            int previous = before.get(location);
            int current = after.get(location);
            if (previous == current) {
                continue;
            }
            if (current > 0) {
                broadcastLight(location, current);
            } else {
                broadcastRestore(location);
            }
        }
    }

    private void broadcastLight(LightLocation location, int level) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            SchedulerUtil.runSync(viewer, () -> sendLight(viewer, location, level));
        }
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

    private void broadcastRestore(LightLocation location) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            SchedulerUtil.runSync(viewer, () -> restoreBlock(viewer, location));
        }
    }

    private void restoreBlock(Player player, LightLocation location) {
        int level;
        synchronized (activeLights) {
            level = getLightLevelAtLocked(location);
        }
        if (level > 0) {
            sendLight(player, location, level);
            return;
        }
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
