package cn.superiormc.ultimatetweak.tweaks.betterdropdisplay;

import cn.superiormc.ultimatetweak.managers.MatchItemManager;
import cn.superiormc.ultimatetweak.tweaks.AbstractTweak;
import cn.superiormc.ultimatetweak.tweaks.TweakEventType;
import cn.superiormc.ultimatetweak.tweaks.config.BetterDropDisplayConfig;
import cn.superiormc.ultimatetweak.tweaks.config.BetterDropDisplayConfig.ModelProfile;
import cn.superiormc.ultimatetweak.utils.SchedulerUtil;
import com.github.retrooper.packetevents.PacketEvents;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.BoundingBox;

import java.lang.ref.WeakReference;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class BetterDropDisplayTweak extends AbstractTweak<BetterDropDisplayConfig> {

    private final BetterDropDisplayPacketListener packetListener = new BetterDropDisplayPacketListener(this);

    private final Map<UUID, WeakReference<Item>> itemEntities = new ConcurrentHashMap<>();

    private final Map<UUID, ModelProfile> modelProfiles = new ConcurrentHashMap<>();

    private boolean listenerRegistered;

    public BetterDropDisplayTweak(BetterDropDisplayConfig config) {
        super("BetterDropDisplay", config);
    }

    @Override
    public Set<TweakEventType> getEventTypes() {
        return EnumSet.of(TweakEventType.PLAYER_QUIT, TweakEventType.ITEM_SPAWN);
    }

    @Override
    public void onLoad() {
        registerPacketListener();
    }

    @Override
    public void onReload() {
        super.onReload();
        modelProfiles.clear();
        for (Map.Entry<UUID, WeakReference<Item>> entry : itemEntities.entrySet()) {
            Item item = entry.getValue().get();
            if (item == null || !item.isValid()) {
                itemEntities.remove(entry.getKey(), entry.getValue());
                continue;
            }
            SchedulerUtil.runSync(item, () -> cacheModelProfile(item));
        }
        if (isEnabled()) {
            registerPacketListener();
        } else {
            unregisterPacketListener();
            packetListener.clear();
        }
    }

    private void registerPacketListener() {
        if (listenerRegistered) {
            return;
        }
        PacketEvents.getAPI().getEventManager().registerListener(packetListener);
        listenerRegistered = true;
    }

    private void unregisterPacketListener() {
        if (!listenerRegistered) {
            return;
        }
        PacketEvents.getAPI().getEventManager().unregisterListener(packetListener);
        listenerRegistered = false;
    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        packetListener.clear(event.getPlayer().getUniqueId());
    }

    @Override
    public void onItemSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();
        itemEntities.put(item.getUniqueId(), new WeakReference<>(item));
        cacheModelProfile(item);
    }

    void resolveGroundTranslation(UUID entityUuid,
                                  double packetY,
                                  float yawRadians,
                                  ModelProfile profile,
                                  Consumer<Float> callback) {
        float fallback = fallbackTranslation(profile, yawRadians);
        if (entityUuid == null) {
            callback.accept(fallback);
            return;
        }
        WeakReference<Item> reference = itemEntities.get(entityUuid);
        if (reference == null) {
            callback.accept(fallback);
            return;
        }
        Item item = reference.get();
        if (item == null) {
            itemEntities.remove(entityUuid, reference);
            modelProfiles.remove(entityUuid);
            callback.accept(fallback);
            return;
        }
        SchedulerUtil.runSync(item, () -> {
            if (!item.isValid()) {
                itemEntities.remove(entityUuid, reference);
                modelProfiles.remove(entityUuid);
                callback.accept(fallback);
                return;
            }
            ModelProfile currentProfile = cacheModelProfile(item);
            callback.accept(calculateGroundTranslation(item, packetY, yawRadians, currentProfile));
        });
    }

    ModelProfile getModelProfile(UUID entityUuid) {
        if (entityUuid == null) {
            return getConfig().getDefaultProfile();
        }
        return modelProfiles.getOrDefault(entityUuid, getConfig().getDefaultProfile());
    }

    private ModelProfile cacheModelProfile(Item item) {
        ModelProfile resolved = getConfig().getDefaultProfile();
        for (ModelProfile candidate : getConfig().getModelProfiles()) {
            if (MatchItemManager.matchItemManager.getMatch(candidate.getMatcher(), item.getItemStack())) {
                resolved = candidate;
                break;
            }
        }
        modelProfiles.put(item.getUniqueId(), resolved);
        return resolved;
    }

    private float calculateGroundTranslation(Item item,
                                             double packetY,
                                             float yawRadians,
                                             ModelProfile profile) {
        Location location = item.getLocation();
        World world = location.getWorld();
        double x = location.getX();
        double z = location.getZ();
        int startY = Math.min(world.getMaxHeight() - 1,
                (int) Math.floor(Math.max(location.getY(), packetY) + 0.5D));
        int endY = Math.max(world.getMinHeight(), startY - 32);
        double highestSurface = Double.NEGATIVE_INFINITY;

        for (int blockY = startY; blockY >= endY; blockY--) {
            Block block = world.getBlockAt((int) Math.floor(x), blockY, (int) Math.floor(z));
            double localX = x - block.getX();
            double localZ = z - block.getZ();
            for (BoundingBox box : block.getCollisionShape().getBoundingBoxes()) {
                if (localX + 1.0E-5D < box.getMinX() || localX - 1.0E-5D > box.getMaxX()
                        || localZ + 1.0E-5D < box.getMinZ() || localZ - 1.0E-5D > box.getMaxZ()) {
                    continue;
                }
                double surface = blockY + box.getMaxY();
                if (surface <= location.getY() + 0.5D && surface > highestSurface) {
                    highestSurface = surface;
                }
            }
        }

        if (!Double.isFinite(highestSurface)) {
            return fallbackTranslation(profile, yawRadians);
        }
        float halfExtent = profile.isModelBoundsEnabled()
                ? DropDisplayPose.verticalHalfExtent(
                profile, DropDisplayPose.targetRotation(profile, yawRadians)) : 0.0F;
        double translation = highestSurface - packetY + halfExtent + profile.getClearance();
        return (float) Math.max(-1.5D, Math.min(1.5D, translation));
    }

    private float fallbackTranslation(ModelProfile profile, float yawRadians) {
        return profile.getClearance() + (profile.isModelBoundsEnabled()
                ? DropDisplayPose.verticalHalfExtent(
                profile, DropDisplayPose.targetRotation(profile, yawRadians)) : 0.0F);
    }

    @Override
    public void onDisable() {
        unregisterPacketListener();
        packetListener.clear();
        itemEntities.clear();
        modelProfiles.clear();
    }
}
