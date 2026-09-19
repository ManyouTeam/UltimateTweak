package cn.superiormc.ultimatetweak.packets.entity;

import cn.superiormc.ultimatetweak.packets.entity.meta.EntityMeta;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class PacketEntity {

    private static final AtomicInteger NEXT_ENTITY_ID = new AtomicInteger(Integer.MAX_VALUE);

    private final int entityId;

    private final UUID uuid = UUID.randomUUID();

    private final EntityType type;

    private final EntityMeta meta;

    private final Set<UUID> viewers = new LinkedHashSet<>();

    private Location location;

    private boolean spawned;

    public PacketEntity(EntityType type) {
        this.entityId = NEXT_ENTITY_ID.getAndDecrement();
        this.type = type;
        this.meta = EntityMeta.createMeta(entityId, type);
    }

    public <T extends EntityMeta> T getEntityMeta(Class<T> type) {
        return type.cast(meta);
    }

    public void addViewer(UUID viewerId) {
        if (viewers.add(viewerId) && spawned) {
            Player viewer = Bukkit.getPlayer(viewerId);
            if (viewer != null) {
                sendSpawn(viewer);
            }
        }
    }

    public boolean spawn(Location spawnLocation) {
        location = spawnLocation;
        spawned = true;
        forEachViewer(this::sendSpawn);
        return true;
    }

    public void teleport(Location destination) {
        location = destination;
        send(new WrapperPlayServerEntityTeleport(entityId, destination, false));
    }

    public void refresh() {
        send(new WrapperPlayServerEntityMetadata(entityId, meta));
    }

    public void remove() {
        if (spawned) {
            send(new WrapperPlayServerDestroyEntities(entityId));
        }
        spawned = false;
        viewers.clear();
    }

    private void sendSpawn(Player viewer) {
        PacketEvents.getAPI().getPlayerManager().sendPacketSilently(viewer,
                new WrapperPlayServerSpawnEntity(entityId, uuid, type, location,
                        0.0F, 0, Vector3d.zero()));
        PacketEvents.getAPI().getPlayerManager().sendPacketSilently(viewer,
                new WrapperPlayServerEntityMetadata(entityId, meta));
    }

    private void send(PacketWrapper<?> packet) {
        forEachViewer(viewer ->
                PacketEvents.getAPI().getPlayerManager().sendPacketSilently(viewer, packet));
    }

    private void forEachViewer(java.util.function.Consumer<Player> action) {
        for (UUID viewerId : viewers) {
            Player viewer = Bukkit.getPlayer(viewerId);
            if (viewer != null && viewer.isOnline()) {
                action.accept(viewer);
            }
        }
    }
}
