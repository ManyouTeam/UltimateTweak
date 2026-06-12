package cn.superiormc.ultimatetweak.tweaks.multiblock.treecutter;

import cn.superiormc.ultimatetweak.managers.TreeDetermineManager;
import cn.superiormc.ultimatetweak.managers.TreeDetermineManager.SnapshotBlock;
import cn.superiormc.ultimatetweak.managers.TreeDetermineManager.TreeDetectionResult;
import cn.superiormc.ultimatetweak.tweaks.config.TreeCutterConfig;
import cn.superiormc.ultimatetweak.tweaks.multiblock.AbstractMultiBlockTweak;
import cn.superiormc.ultimatetweak.utils.CommonUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TreeCutterTweak extends AbstractMultiBlockTweak<TreeCutterConfig, TreeDetectionResult> {

    private static final long NANOS_PER_TICK = 50_000_000L;

    private final ConcurrentHashMap<UUID, Long> lastDetectionNanos = new ConcurrentHashMap<>();

    public TreeCutterTweak(TreeCutterConfig config) {
        super("TreeCutter", config);
    }

    @Override
    protected Detection<TreeDetectionResult> detect(Player player, Block origin) {
        if (!canDetect(player.getUniqueId())) {
            return null;
        }
        TreeDetectionResult result = TreeDetermineManager.treeDetermineManager.determineTree(player, origin);
        if (result == null || !CommonUtil.hasEnoughDurability(
                player.getInventory().getItemInMainHand(), result.logAmount())) {
            return null;
        }
        return detection(TreeKey.of(result), result,
                TreeDetermineManager.treeDetermineManager.getValidBlocks(result), result.logAmount());
    }

    @Override
    public void onReload() {
        lastDetectionNanos.clear();
        super.onReload();
    }

    @Override
    public void onDisable() {
        lastDetectionNanos.clear();
        super.onDisable();
    }

    private boolean canDetect(UUID playerId) {
        int cooldownTicks = getConfig().getCooldownTicks();
        if (cooldownTicks <= 0) {
            return true;
        }
        long now = System.nanoTime();
        long cooldownNanos = cooldownTicks * NANOS_PER_TICK;
        Long previous = lastDetectionNanos.get(playerId);
        if (previous != null && now - previous < cooldownNanos) {
            return false;
        }
        lastDetectionNanos.put(playerId, now);
        return true;
    }

    @Override
    protected List<Block> getCurrentBlocks(TreeDetectionResult result) {
        return TreeDetermineManager.treeDetermineManager.getValidBlocks(result);
    }

    @Override
    protected boolean isStillValid(TreeDetectionResult result, List<Block> currentBlocks) {
        return currentBlocks.size() == result.treeBlocks().size();
    }

    @Override
    protected Location getGlowCenter(TreeDetectionResult result, Block breakingBlock) {
        return new Location(result.snapshot().world(), result.snapshot().originX() + 0.5,
                result.snapshot().originY() + 0.5, result.snapshot().originZ() + 0.5);
    }

    @Override
    protected void breakBlocks(Player player, MultiBlockSession session, List<Block> blocks) {
        if (!player.isOnline()) {
            removeSession(session);
            return;
        }
        TreeDetectionResult result = session.data();
        World world = result.snapshot().world();
        boolean breakLeaves = getConfig().shouldBreakLeaves(player);
        boolean playAnimation = getConfig().isAnimationEnabled();
        List<TreeBlockDisplayAnimation.AnimationBlock> animationBlocks = new ArrayList<>();
        TreeBlockDisplayAnimation.AnimationSession animationSession = null;

        if (playAnimation) {
            for (Block block : blocks) {
                animationBlocks.add(new TreeBlockDisplayAnimation.AnimationBlock(block));
            }
            animationSession = TreeBlockDisplayAnimation.create(
                    world,
                    result.getLowestLogBlock(),
                    animationBlocks,
                    getConfig().getAnimationDurationTicks(),
                    getConfig().getAnimationIntervalTicks(),
                    getConfig().isAnimationGlowEnabled(),
                    getConfig().getAnimationGlowColor(),
                    getConfig().getAnimationViewDistance(),
                    getConfig().getAnimationDirection(),
                    player.getLocation().getDirection(),
                    new TreeBlockDisplayAnimation.FallDamageOptions(
                            getConfig().isFallDamageEnabled(),
                            getConfig().shouldFallDamagePlayers(),
                            getConfig().shouldFallDamageEntities(),
                            getConfig().getFallDamageAmount(),
                            getConfig().getFallDamageMinAngle(),
                            getConfig().getFallDamageHitRadius()));
        }

        Set<LocationKey> dropKeys = new HashSet<>();
        prepareDefaultDrops(session, blocks, dropKeys);
        if (playAnimation) {
            for (TreeBlockDisplayAnimation.AnimationBlock animationBlock : animationBlocks) {
                if (animationBlock.getDropLocation() == null) {
                    continue;
                }
                LocationKey key = locationKey(world.getUID(),
                        animationBlock.getX(), animationBlock.getY(), animationBlock.getZ());
                session.prepareDrop(key, animationBlock.getDropLocation());
                dropKeys.add(key);
            }
        }

        getConfig().getBreakActions().runAllActions(player);
        for (Block block : blocks) {
            boolean naturally = TreeDetermineManager.treeDetermineManager.isLeafBlock(block, result.treeDefinition())
                    && !breakLeaves;
            if (!breakBlock(player, session, block, naturally)) {
                dropKeys.remove(LocationKey.of(block));
            }
        }

        Runnable finish = () -> finish(session, player, dropKeys);
        if (playAnimation && animationSession != null) {
            animationSession.play(finish);
        } else {
            finish.run();
        }
    }

    private record TreeKey(UUID worldId, int x, int y, int z) {

        private static TreeKey of(TreeDetectionResult result) {
            SnapshotBlock lowestLog = result.lowestLog();
            return new TreeKey(result.snapshot().world().getUID(),
                    lowestLog.getWorldX(result.snapshot()),
                    lowestLog.getWorldY(result.snapshot()),
                    lowestLog.getWorldZ(result.snapshot()));
        }
    }
}
