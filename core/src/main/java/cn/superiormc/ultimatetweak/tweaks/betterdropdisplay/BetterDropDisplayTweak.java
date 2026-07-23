package cn.superiormc.ultimatetweak.tweaks.betterdropdisplay;

import cn.superiormc.ultimatetweak.tweaks.AbstractTweak;
import cn.superiormc.ultimatetweak.tweaks.TweakEventType;
import cn.superiormc.ultimatetweak.tweaks.config.BetterDropDisplayConfig;
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
        if (isEnabled()) {
            registerPacketListener();
        }
    }

    private void registerPacketListener() {
        if (listenerRegistered) {
            return;
        }
        PacketEvents.getAPI().getEventManager().registerListener(packetListener);
        listenerRegistered = true;
    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        packetListener.clear(event.getPlayer().getUniqueId());
    }

    @Override
    public void onItemSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();
        itemEntities.put(item.getUniqueId(), new WeakReference<>(item));
    }

    void resolveGroundTranslation(UUID entityUuid,
                                  double packetY,
                                  float clearance,
                                  Consumer<Float> callback) {
        if (entityUuid == null) {
            callback.accept(clearance);
            return;
        }
        WeakReference<Item> reference = itemEntities.get(entityUuid);
        if (reference == null) {
            callback.accept(clearance);
            return;
        }
        Item item = reference.get();
        if (item == null) {
            itemEntities.remove(entityUuid, reference);
            callback.accept(clearance);
            return;
        }
        SchedulerUtil.runSync(item, () -> {
            if (!item.isValid()) {
                itemEntities.remove(entityUuid, reference);
                callback.accept(clearance);
                return;
            }
            callback.accept(calculateGroundTranslation(item, packetY, clearance));
        });
    }

    private float calculateGroundTranslation(Item item, double packetY, float clearance) {
        Location location = item.getLocation();
        World world = location.getWorld();
        double x = location.getX();
        double z = location.getZ();
        int startY = Math.min(world.getMaxHeight() - 1,
                (int) Math.floor(Math.max(location.getY(), packetY) + 0.5D));
        int endY = Math.max(world.getMinHeight(), startY - 3);
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
            return clearance;
        }
        double surfaceCorrection = Math.max(0.0D, highestSurface - packetY);
        return (float) Math.min(1.5D, clearance + surfaceCorrection);
    }

    @Override
    public void onDisable() {
        if (listenerRegistered) {
            PacketEvents.getAPI().getEventManager().unregisterListener(packetListener);
            listenerRegistered = false;
        }
        packetListener.clear();
        itemEntities.clear();
    }
}
