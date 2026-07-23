package cn.superiormc.ultimatetweak.tweaks.betterdropdisplay;

import cn.superiormc.ultimatetweak.utils.SchedulerUtil;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityPositionSync;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMoveAndRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import me.tofaa.entitylib.meta.display.ItemDisplayMeta;
import me.tofaa.entitylib.meta.projectile.ItemEntityMeta;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

final class BetterDropDisplayPacketListener extends PacketListenerAbstract {

    private final BetterDropDisplayTweak tweak;

    private final Map<UUID, Map<Integer, DropView>> views = new ConcurrentHashMap<>();

    BetterDropDisplayPacketListener(BetterDropDisplayTweak tweak) {
        this.tweak = tweak;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled() || event.getUser().getUUID() == null) {
            return;
        }
        if (event.getPacketType() == PacketType.Play.Server.SPAWN_ENTITY) {
            handleSpawn(event);
        } else if (event.getPacketType() == PacketType.Play.Server.ENTITY_METADATA) {
            handleMetadata(event);
        } else if (event.getPacketType() == PacketType.Play.Server.ENTITY_RELATIVE_MOVE) {
            handleRelativeMove(event);
        } else if (event.getPacketType() == PacketType.Play.Server.ENTITY_RELATIVE_MOVE_AND_ROTATION) {
            handleRelativeMoveAndRotation(event);
        } else if (event.getPacketType() == PacketType.Play.Server.ENTITY_TELEPORT) {
            handleTeleport(event);
        } else if (event.getPacketType() == PacketType.Play.Server.ENTITY_POSITION_SYNC) {
            handlePositionSync(event);
        } else if (event.getPacketType() == PacketType.Play.Server.DESTROY_ENTITIES) {
            handleDestroy(event);
        }
    }

    void clear(UUID playerId) {
        views.remove(playerId);
    }

    void clear() {
        views.clear();
    }

    private void handleSpawn(PacketSendEvent event) {
        UUID viewerId = event.getUser().getUUID();
        WrapperPlayServerSpawnEntity packet = new WrapperPlayServerSpawnEntity(event);
        Map<Integer, DropView> playerViews = views.get(viewerId);
        if (playerViews != null) {
            playerViews.remove(packet.getEntityId());
            if (playerViews.isEmpty()) {
                views.remove(viewerId, playerViews);
            }
        }
        if (!tweak.isEnabled()) {
            return;
        }
        Player player = event.getPlayer();
        if (player == null || !tweak.isWorldEnabled(player.getWorld())) {
            return;
        }
        if (packet.getEntityType() != EntityTypes.ITEM) {
            return;
        }

        float yaw = tweak.getConfig().isRandomYaw()
                ? stableYaw(packet.getUUID().orElse(null), packet.getEntityId()) : 0.0F;
        boolean initiallyStationary = packet.getVelocity().isPresent()
                && packet.getVelocity().get().lengthSquared() < 0.000001D;
        views.computeIfAbsent(viewerId, ignored -> new ConcurrentHashMap<>())
                .put(packet.getEntityId(), new DropView(
                        packet.getEntityId(), packet.getUUID().orElse(null), packet.getPosition(),
                        yaw, initiallyStationary));
    }

    private void handleMetadata(PacketSendEvent event) {
        UUID viewerId = event.getUser().getUUID();
        Map<Integer, DropView> playerViews = views.get(viewerId);
        if (playerViews == null) {
            return;
        }

        WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(event);
        DropView view = playerViews.get(packet.getEntityId());
        if (view == null) {
            return;
        }

        ItemStack itemStack = findItemStack(packet);
        if (itemStack != null) {
            view.itemStack = itemStack.copy();
        }
        if (!view.displayed) {
            if (itemStack != null && view.grounded && !view.displaySwitchScheduled) {
                scheduleDisplayCountdown(event, view);
            }
            return;
        }
        if (itemStack == null) {
            event.setCancelled(true);
            return;
        }

        ItemDisplayMeta meta = DropDisplayMetadataFactory.create(
                packet.getEntityId(), itemStack, view.yawRadians,
                view.groundTranslationY, tweak.getConfig());
        packet.setEntityMetadata(meta.entityData(event.getClientVersion()));
        event.markForReEncode(true);
    }

    private void handleRelativeMove(PacketSendEvent event) {
        WrapperPlayServerEntityRelativeMove packet = new WrapperPlayServerEntityRelativeMove(event);
        DropView view = getView(event, packet.getEntityId());
        if (view == null) {
            return;
        }
        view.position = view.position.add(packet.getDeltaX(), packet.getDeltaY(), packet.getDeltaZ());
        handleGroundState(event, view, packet.isOnGround());
    }

    private void handleRelativeMoveAndRotation(PacketSendEvent event) {
        WrapperPlayServerEntityRelativeMoveAndRotation packet =
                new WrapperPlayServerEntityRelativeMoveAndRotation(event);
        DropView view = getView(event, packet.getEntityId());
        if (view == null) {
            return;
        }
        view.position = view.position.add(packet.getDeltaX(), packet.getDeltaY(), packet.getDeltaZ());
        handleGroundState(event, view, packet.isOnGround());
    }

    private void handleTeleport(PacketSendEvent event) {
        WrapperPlayServerEntityTeleport packet = new WrapperPlayServerEntityTeleport(event);
        DropView view = getView(event, packet.getEntityId());
        if (view == null) {
            return;
        }
        view.position = packet.getPosition();
        handleGroundState(event, view, packet.isOnGround());
    }

    private void handlePositionSync(PacketSendEvent event) {
        WrapperPlayServerEntityPositionSync packet = new WrapperPlayServerEntityPositionSync(event);
        DropView view = getView(event, packet.getId());
        if (view == null) {
            return;
        }
        view.position = packet.getValues().getPosition();
        handleGroundState(event, view, packet.isOnGround());
    }

    private void handleGroundState(PacketSendEvent event, DropView view, boolean onGround) {
        view.grounded = onGround;
        if (!onGround) {
            if (view.displayed) {
                if (++view.offGroundUpdates >= 2 && !view.itemSwitchScheduled) {
                    scheduleItemSwitch(event, view);
                }
            } else {
                cancelDisplayCountdown(view);
            }
            return;
        }
        view.offGroundUpdates = 0;
        if (view.displayed) {
            if (hasMovedSignificantly(view) && !view.itemSwitchScheduled) {
                scheduleItemSwitch(event, view);
            }
            return;
        }
        if (hasMovedSignificantly(view)) {
            cancelDisplayCountdown(view);
            view.settleAnchor = view.position;
        }
        if (view.itemStack != null && !view.displaySwitchScheduled) {
            scheduleDisplayCountdown(event, view);
        }
    }

    private void scheduleDisplayCountdown(PacketSendEvent event, DropView view) {
        Player player = event.getPlayer();
        ClientVersion clientVersion = event.getClientVersion();
        event.getTasksAfterSend().add(() -> startDisplayCountdown(player, clientVersion, view));
    }

    private void startDisplayCountdown(Player player, ClientVersion clientVersion, DropView view) {
        if (view.displaySwitchScheduled || view.displayed || !view.grounded || view.itemStack == null) {
            return;
        }
        view.displaySwitchScheduled = true;
        view.settleAnchor = view.position;
        long revision = ++view.displayRevision;
        SchedulerUtil.runTaskLater(
                () -> switchToDisplay(player, clientVersion, view, revision),
                tweak.getConfig().getSettleDelayTicks());
    }

    private void scheduleItemSwitch(PacketSendEvent event, DropView view) {
        if (view.itemSwitchScheduled) {
            return;
        }
        view.itemSwitchScheduled = true;
        Player player = event.getPlayer();
        ClientVersion clientVersion = event.getClientVersion();
        event.getTasksAfterSend().add(() -> switchToItem(player, clientVersion, view));
    }

    private void switchToDisplay(Player player,
                                 ClientVersion clientVersion,
                                 DropView view,
                                 long revision) {
        if (revision != view.displayRevision) {
            return;
        }
        if (player == null || !player.isOnline() || view.displayed || !view.grounded
                || view.itemStack == null || !tweak.isEnabled() || hasMovedSignificantly(view)) {
            view.displaySwitchScheduled = false;
            return;
        }
        tweak.resolveGroundTranslation(
                view.entityUuid,
                view.position.y,
                tweak.getConfig().getTranslationY(),
                translationY -> completeDisplaySwitch(
                        player, clientVersion, view, translationY, revision));
    }

    private void completeDisplaySwitch(Player player,
                                       ClientVersion clientVersion,
                                       DropView view,
                                       float translationY,
                                       long revision) {
        if (revision != view.displayRevision) {
            return;
        }
        Map<Integer, DropView> playerViews = views.get(player.getUniqueId());
        if (playerViews == null || playerViews.get(view.entityId) != view
                || !player.isOnline() || view.displayed || !view.grounded
                || view.itemStack == null || !tweak.isEnabled() || hasMovedSignificantly(view)) {
            view.displaySwitchScheduled = false;
            return;
        }
        view.groundTranslationY = translationY;
        view.displayed = true;
        view.displaySwitchScheduled = false;
        sendRespawn(player, view, EntityTypes.ITEM_DISPLAY);
        ItemDisplayMeta meta = DropDisplayMetadataFactory.create(
                view.entityId, view.itemStack, view.yawRadians,
                view.groundTranslationY, tweak.getConfig());
        PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player,
                new WrapperPlayServerEntityMetadata(view.entityId, meta.entityData(clientVersion)));
    }

    private void switchToItem(Player player, ClientVersion clientVersion, DropView view) {
        if (player == null || !player.isOnline() || !view.displayed || view.itemStack == null) {
            view.itemSwitchScheduled = false;
            return;
        }
        view.displayed = false;
        view.itemSwitchScheduled = false;
        sendRespawn(player, view, EntityTypes.ITEM);
        ItemEntityMeta meta = DropDisplayMetadataFactory.createItem(view.entityId, view.itemStack);
        PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player,
                new WrapperPlayServerEntityMetadata(view.entityId, meta.entityData(clientVersion)));
        if (view.grounded) {
            cancelDisplayCountdown(view);
            startDisplayCountdown(player, clientVersion, view);
        }
    }

    private void cancelDisplayCountdown(DropView view) {
        if (view.displaySwitchScheduled) {
            view.displaySwitchScheduled = false;
            view.displayRevision++;
        }
        view.settleAnchor = null;
    }

    private boolean hasMovedSignificantly(DropView view) {
        Vector3d anchor = view.settleAnchor;
        if (anchor == null) {
            return false;
        }
        Vector3d position = view.position;
        double x = position.x - anchor.x;
        double y = position.y - anchor.y;
        double z = position.z - anchor.z;
        double threshold = tweak.getConfig().getSettleMovementThreshold();
        return x * x + y * y + z * z > threshold * threshold;
    }

    private void sendRespawn(Player player,
                             DropView view,
                             com.github.retrooper.packetevents.protocol.entity.type.EntityType type) {
        PacketEvents.getAPI().getPlayerManager().sendPacketSilently(
                player, new WrapperPlayServerDestroyEntities(view.entityId));
        WrapperPlayServerSpawnEntity spawn = new WrapperPlayServerSpawnEntity(
                view.entityId,
                Optional.ofNullable(view.entityUuid),
                type,
                view.position,
                0.0F,
                0.0F,
                0.0F,
                0,
                Optional.empty());
        PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, spawn);
    }

    private DropView getView(PacketSendEvent event, int entityId) {
        Map<Integer, DropView> playerViews = views.get(event.getUser().getUUID());
        return playerViews == null ? null : playerViews.get(entityId);
    }

    private void handleDestroy(PacketSendEvent event) {
        UUID viewerId = event.getUser().getUUID();
        Map<Integer, DropView> playerViews = views.get(viewerId);
        if (playerViews == null) {
            return;
        }
        WrapperPlayServerDestroyEntities packet = new WrapperPlayServerDestroyEntities(event);
        for (int entityId : packet.getEntityIds()) {
            playerViews.remove(entityId);
        }
        if (playerViews.isEmpty()) {
            views.remove(viewerId, playerViews);
        }
    }

    private ItemStack findItemStack(WrapperPlayServerEntityMetadata packet) {
        for (EntityData<?> data : packet.getEntityMetadata()) {
            if (data.getType() == EntityDataTypes.ITEMSTACK && data.getValue() instanceof ItemStack) {
                return (ItemStack) data.getValue();
            }
        }
        return null;
    }

    private float stableYaw(UUID entityUuid, int entityId) {
        long seed = entityUuid == null
                ? Integer.toUnsignedLong(entityId)
                : entityUuid.getMostSignificantBits() ^ entityUuid.getLeastSignificantBits();
        long positive = seed & Long.MAX_VALUE;
        return (float) ((positive % 3600L) * (Math.PI * 2.0 / 3600.0));
    }

    private static final class DropView {

        private final int entityId;

        private final UUID entityUuid;

        private final float yawRadians;

        private volatile Vector3d position;

        private volatile ItemStack itemStack;

        private volatile boolean displayed;

        private volatile boolean displaySwitchScheduled;

        private volatile boolean itemSwitchScheduled;

        private volatile long displayRevision;

        private volatile Vector3d settleAnchor;

        private volatile int offGroundUpdates;

        private volatile float groundTranslationY;

        private volatile boolean grounded;

        private DropView(int entityId,
                         UUID entityUuid,
                         Vector3d position,
                         float yawRadians,
                         boolean initiallyStationary) {
            this.entityId = entityId;
            this.entityUuid = entityUuid;
            this.position = position;
            this.yawRadians = yawRadians;
            this.grounded = initiallyStationary;
            this.settleAnchor = position;
        }
    }
}
