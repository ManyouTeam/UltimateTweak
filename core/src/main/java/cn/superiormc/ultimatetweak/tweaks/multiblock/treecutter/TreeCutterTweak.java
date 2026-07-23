package cn.superiormc.ultimatetweak.tweaks.multiblock.treecutter;

import cn.superiormc.ultimatetweak.UltimateTweak;
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

public class TreeCutterTweak extends AbstractMultiBlockTweak<TreeCutterConfig, TreeDetectionResult> {

    public TreeCutterTweak(TreeCutterConfig config) {
        super("TreeCutter", config);
    }

    @Override
    protected Detection<TreeDetectionResult> detect(Player player, Block origin) {
        TreeDetectionResult result = TreeDetermineManager.treeDetermineManager.determineTree(player, origin);
        if (result == null || !CommonUtil.hasEnoughDurability(
                player.getInventory().getItemInMainHand(), result.logAmount())) {
            return null;
        }
        return detection(TreeKey.of(result), result,
                TreeDetermineManager.treeDetermineManager.getValidBlocks(result), result.logAmount());
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
        boolean playAnimation = getConfig().isAnimationEnabled() && UltimateTweak.isEntityLibAvailable();
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
                            getConfig().getFallDamageHitRadius(),
                            getConfig().getFallDamageCheckIntervalTicks()));
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

        getConfig().getBreakActions().runAllActions(player,
                getActionArgs(result, blocks, result.logAmount()));
        TreeBlockDisplayAnimation.AnimationSession finalAnimationSession = animationSession;
        Runnable afterBreak = () -> {
            Runnable finish = () -> finish(session, player, dropKeys);
            if (playAnimation && finalAnimationSession != null) {
                finalAnimationSession.play(finish);
            } else {
                finish.run();
            }
        };
        breakBlocksInBatches(player, session, blocks,
                block -> TreeDetermineManager.treeDetermineManager.isLeafBlock(block, result.treeDefinition())
                        && !breakLeaves,
                block -> dropKeys.remove(LocationKey.of(block)),
                afterBreak);
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

    @Override
    protected String[] getActionArgs(TreeDetectionResult result, List<Block> blocks, int miningBlockCount) {
        return new String[] {
                "block-amount", String.valueOf(blocks.size()),
                "mining-block-amount", String.valueOf(miningBlockCount),
                "tree-amount", String.valueOf(result.treeBlocks().size()),
                "log-amount", String.valueOf(result.logAmount()),
                "leaf-amount", String.valueOf(result.leafAmount()),
                "tree-id", result.treeDefinition().getId()
        };
    }
}
