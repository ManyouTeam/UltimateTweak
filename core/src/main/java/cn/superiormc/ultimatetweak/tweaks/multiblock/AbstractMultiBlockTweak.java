package cn.superiormc.ultimatetweak.tweaks.multiblock;

import cn.superiormc.ultimatetweak.managers.AttributeModifyManager;
import cn.superiormc.ultimatetweak.managers.HookManager;
import cn.superiormc.ultimatetweak.managers.MatchItemManager;
import cn.superiormc.ultimatetweak.tweaks.AbstractTweak;
import cn.superiormc.ultimatetweak.tweaks.TweakEventType;
import cn.superiormc.ultimatetweak.tweaks.config.AbstractMultiBlockConfig;
import cn.superiormc.ultimatetweak.utils.CommonUtil;
import cn.superiormc.ultimatetweak.utils.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractMultiBlockTweak<C extends AbstractMultiBlockConfig, D>
        extends AbstractTweak<C> {

    private final Map<Object, MultiBlockSession> sessions = new ConcurrentHashMap<>();

    protected AbstractMultiBlockTweak(String id, C config) {
        super(id, config);
    }

    @Override
    public final Set<TweakEventType> getEventTypes() {
        return EnumSet.of(TweakEventType.BLOCK_DAMAGE, TweakEventType.BLOCK_DAMAGE_ABORT, TweakEventType.NO_SWING_ITEM,
                TweakEventType.BLOCK_BREAK, TweakEventType.BLOCK_DROP_ITEM, TweakEventType.PLAYER_QUIT);
    }

    @Override
    public void onReload() {
        sessions.values().forEach(MultiBlockSession::close);
        sessions.clear();
        super.onReload();
    }

    @Override
    public final void onBlockDamage(BlockDamageEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (!shouldTrigger(player, block.getLocation())) {
            return;
        }
        Detection<D> detection = detect(player, block);
        if (detection == null || detection.blocks().isEmpty()) {
            return;
        }

        UUID playerId = player.getUniqueId();
        String pendingKey = getPendingKey(playerId, block);
        MultiBlockSession currentSession = sessions.get(detection.key());
        if (currentSession != null && !currentSession.playerId.equals(playerId)) {
            return;
        }
        removePreviousPlayerSession(playerId, pendingKey);
        MultiBlockSession newSession = new MultiBlockSession(playerId, pendingKey, detection, LocationKey.of(block));
        MultiBlockSession lockOwner = sessions.putIfAbsent(detection.key(), newSession);
        if (lockOwner != null && !lockOwner.playerId.equals(playerId)) {
            return;
        }
        MultiBlockSession session = lockOwner == null ? newSession : lockOwner;
        session.applyMiningSlowdown();
        session.applyGlow();
        getConfig().getDamageActions().runAllActions(player);
    }

    @Override
    public final void onBlockBreak(BlockBreakEvent event) {
        LocationKey locationKey = LocationKey.of(event.getBlock());
        if (consumeInternalBreak(locationKey) || !shouldTrigger(event.getPlayer(), event.getBlock().getLocation())) {
            return;
        }
        MultiBlockSession session = findSessionByPendingKey(
                getPendingKey(event.getPlayer().getUniqueId(), event.getBlock()));
        if (session == null) {
            return;
        }
        session.clearMiningSlowdown();
        session.clearGlow();
        List<Block> blocks = getCurrentBlocks(session.data());
        if (!isStillValid(session.data(), blocks)) {
            removeSession(session);
            return;
        }

        event.setCancelled(true);
        try {
            breakBlocks(event.getPlayer(), session, blocks);
        } catch (Throwable throwable) {
            removeSession(session);
            throw throwable;
        }
    }

    @Override
    public final void onBlockDropItem(BlockDropItemEvent event) {
        BlockOperation operation = findBlockOperation(LocationKey.of(event.getBlock()));
        if (operation == null) {
            return;
        }
        event.getItems().forEach(item -> {
            operation.addDrop(item.getItemStack());
            item.remove();
        });
        event.getItems().clear();
    }

    @Override
    public void onNoSwingItem(UUID playerId) {
        clearPlayerSessions(playerId);
    }

    @Override
    public final void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        clearPlayerSessions(playerId);
    }

    protected abstract Detection<D> detect(Player player, Block origin);

    protected abstract List<Block> getCurrentBlocks(D data);

    protected abstract boolean isStillValid(D data, List<Block> currentBlocks);

    protected abstract void breakBlocks(Player player, MultiBlockSession session, List<Block> blocks);

    protected Location getGlowCenter(D data, Block breakingBlock) {
        return breakingBlock.getLocation().add(0.5, 0.5, 0.5);
    }

    protected final Detection<D> detection(Object key, D data, List<Block> blocks, int miningBlockCount) {
        return new Detection<>(key, data, blocks, miningBlockCount);
    }

    protected final LocationKey locationKey(UUID worldId, int x, int y, int z) {
        return new LocationKey(worldId, x, y, z);
    }

    protected boolean shouldTrigger(Player player, Location location) {
        if (player == null || getConfig().isRequireSneaking() && !player.isSneaking()) {
            return false;
        }
        if (!getConfig().getConditions().getAllBoolean(player)) {
            return false;
        }
        if (!HookManager.hookManager.getProtectionCanUse(player, location)) {
            return false;
        }
        return !getConfig().hasTriggerItem() || MatchItemManager.matchItemManager.getMatch(
                getConfig().getTriggerItem(), player.getInventory().getItemInMainHand());
    }

    protected final void prepareDefaultDrops(MultiBlockSession session, List<Block> blocks, Set<LocationKey> dropKeys) {
        for (Block block : blocks) {
            LocationKey key = LocationKey.of(block);
            session.prepareDrop(key, block.getLocation().add(0.5, 0.5, 0.5));
            dropKeys.add(key);
        }
    }

    protected final boolean breakBlock(Player player, MultiBlockSession session, Block block, boolean naturally) {
        LocationKey key = LocationKey.of(block);
        session.markInternalBreak(key);
        boolean broken = naturally ? block.breakNaturally() : player.breakBlock(block);
        if (naturally || !broken) {
            session.consumeInternalBreak(key);
        }
        if (!broken) {
            session.removeBlockOperation(key);
        }
        return broken;
    }

    protected final void finish(MultiBlockSession session, Player player, Set<LocationKey> dropKeys) {
        SchedulerUtil.runSync(player, () -> {
            for (LocationKey key : dropKeys) {
                BlockOperation operation = session.removeBlockOperation(key);
                if (operation == null || operation.isEmpty()) {
                    continue;
                }
                Location location = operation.dropLocation;
                if (location.getWorld() == null) {
                    continue;
                }
                for (ItemStack itemStack : operation.drops) {
                    location.getWorld().dropItemNaturally(location, itemStack);
                }
            }
            removeSession(session);
        });
    }

    protected final void removeSession(MultiBlockSession session) {
        if (session != null && sessions.remove(session.detection.key(), session)) {
            session.close();
        }
    }

    private void removePreviousPlayerSession(UUID playerId, String pendingKey) {
        for (MultiBlockSession session : new ArrayList<>(sessions.values())) {
            if (session.playerId.equals(playerId) && !session.pendingKey.equals(pendingKey)) {
                removeSession(session);
            }
        }
    }

    public final void clearPlayerSessions(UUID playerId) {
        sessions.values().stream()
                .filter(session -> session.playerId.equals(playerId))
                .toList()
                .forEach(this::removeSession);
    }

    private MultiBlockSession findSessionByPendingKey(String pendingKey) {
        for (MultiBlockSession session : sessions.values()) {
            if (session.pendingKey.equals(pendingKey)) {
                return session;
            }
        }
        return null;
    }

    private BlockOperation findBlockOperation(LocationKey key) {
        for (MultiBlockSession session : sessions.values()) {
            BlockOperation operation = session.blockOperations.get(key);
            if (operation != null) {
                return operation;
            }
        }
        return null;
    }

    private boolean consumeInternalBreak(LocationKey key) {
        BlockOperation operation = findBlockOperation(key);
        return operation != null && operation.consumeInternalBreak();
    }

    private String getPendingKey(UUID playerId, Block block) {
        return playerId + ":" + LocationKey.of(block);
    }

    protected final class MultiBlockSession {

        private final UUID playerId;

        private final String pendingKey;

        private final Detection<D> detection;

        private final LocationKey breakingBlockKey;

        private final Map<LocationKey, BlockOperation> blockOperations = new ConcurrentHashMap<>();

        private MultiBlockDisplayGlow.GlowSession glowSession;

        private MultiBlockSession(UUID playerId,
                                  String pendingKey,
                                  Detection<D> detection,
                                  LocationKey breakingBlockKey) {
            this.playerId = playerId;
            this.pendingKey = pendingKey;
            this.detection = detection;
            this.breakingBlockKey = breakingBlockKey;
        }

        public D data() {
            return detection.data();
        }

        public void prepareDrop(LocationKey key, Location dropLocation) {
            blockOperations.put(key, new BlockOperation(dropLocation));
        }

        public void markInternalBreak(LocationKey key) {
            BlockOperation operation = blockOperations.get(key);
            if (operation != null) {
                operation.markInternalBreak();
            }
        }

        public boolean consumeInternalBreak(LocationKey key) {
            BlockOperation operation = blockOperations.get(key);
            return operation != null && operation.consumeInternalBreak();
        }

        public BlockOperation removeBlockOperation(LocationKey key) {
            return blockOperations.remove(key);
        }

        private void applyGlow() {
            if (!getConfig().isDamageGlowEnabled()) {
                return;
            }
            List<Block> blocks = new ArrayList<>(getCurrentBlocks(data()));
            if (getConfig().shouldHideBreakingBlockFromDamageGlow()) {
                blocks.removeIf(block -> LocationKey.of(block).equals(breakingBlockKey));
            }
            if (blocks.isEmpty()) {
                return;
            }
            clearGlow();
            Block origin = breakingBlockKey.getBlock();
            glowSession = MultiBlockDisplayGlow.show(origin.getWorld(), getGlowCenter(data(), origin),
                    blocks, getConfig().getDamageGlowDurationTicks(), getConfig().getDamageGlowViewDistance(),
                    getConfig().getDamageGlowColor());
        }

        private void applyMiningSlowdown() {
            if (!getConfig().isMiningTimeEnabled() || !CommonUtil.getMinorVersion(20, 5)) {
                return;
            }
            Player player = Bukkit.getPlayer(playerId);
            if (player == null) {
                return;
            }
            double totalPercent = Math.min((detection.miningBlockCount() - 1) * getConfig().getMiningTimePercentPerBlock(), getConfig().getMaxMiningTimePercent());
            if (totalPercent <= 0.0) {
                return;
            }
            double speedMultiplier = 1.0 / (1.0 + totalPercent / 100.0);
            AttributeModifyManager.attributeModifyManager.start(player, pendingKey, Attribute.BLOCK_BREAK_SPEED,
                    speedMultiplier - 1.0, AttributeModifier.Operation.MULTIPLY_SCALAR_1);
        }

        private void clearMiningSlowdown() {
            AttributeModifyManager.attributeModifyManager.cancel(playerId, pendingKey);
        }

        private void clearGlow() {
            if (glowSession != null) {
                glowSession.destroy();
                glowSession = null;
            }
        }

        private void close() {
            clearMiningSlowdown();
            clearGlow();
            blockOperations.clear();
        }
    }

    protected record Detection<D>(Object key, D data, List<Block> blocks, int miningBlockCount) {
    }

    protected record LocationKey(UUID worldId, int x, int y, int z) {

        public static LocationKey of(Block block) {
            return new LocationKey(block.getWorld().getUID(), block.getX(), block.getY(), block.getZ());
        }

        public Block getBlock() {
            World world = Bukkit.getWorld(worldId);
            if (world == null) {
                throw new IllegalStateException("World " + worldId + " is no longer loaded");
            }
            return world.getBlockAt(x, y, z);
        }

        @Override
        public @NonNull String toString() {
            return worldId + ":" + x + ":" + y + ":" + z;
        }
    }

    protected static final class BlockOperation {

        private final Location dropLocation;

        private final List<ItemStack> drops = Collections.synchronizedList(new ArrayList<>());

        private final AtomicBoolean internalBreaking = new AtomicBoolean();

        private BlockOperation(Location dropLocation) {
            this.dropLocation = dropLocation;
        }

        private void markInternalBreak() {
            internalBreaking.set(true);
        }

        private boolean consumeInternalBreak() {
            return internalBreaking.compareAndSet(true, false);
        }

        private void addDrop(ItemStack itemStack) {
            if (itemStack != null && !itemStack.getType().isAir()) {
                drops.add(itemStack.clone());
            }
        }

        private boolean isEmpty() {
            return drops.isEmpty();
        }

    }
}
