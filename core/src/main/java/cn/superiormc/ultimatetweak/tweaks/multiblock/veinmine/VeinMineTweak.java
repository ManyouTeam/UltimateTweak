package cn.superiormc.ultimatetweak.tweaks.multiblock.veinmine;

import cn.superiormc.ultimatetweak.managers.HookManager;
import cn.superiormc.ultimatetweak.managers.MatchItemManager;
import cn.superiormc.ultimatetweak.tweaks.config.VeinMineConfig;
import cn.superiormc.ultimatetweak.tweaks.config.VeinMineConfig.VeinRule;
import cn.superiormc.ultimatetweak.tweaks.multiblock.AbstractMultiBlockTweak;
import cn.superiormc.ultimatetweak.utils.CommonUtil;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

public class VeinMineTweak extends AbstractMultiBlockTweak<VeinMineConfig, VeinMineTweak.VeinData> {

    public VeinMineTweak(VeinMineConfig config) {
        super("VeinMine", config);
    }

    @Override
    protected Detection<VeinData> detect(Player player, Block origin) {
        String blockId = getMatchingBlockId(player, origin);
        if (blockId == null) {
            return null;
        }
        List<Block> blocks = findVein(player, origin, blockId);
        if (blocks.isEmpty() || !CommonUtil.hasEnoughDurability(
                player.getInventory().getItemInMainHand(), blocks.size())) {
            return null;
        }
        VeinData data = new VeinData(blockId, blocks.stream().map(LocationKey::of).toList());
        return detection(VeinKey.of(blocks), data, blocks, blocks.size());
    }

    private String getMatchingBlockId(Player player, Block origin) {
        for (VeinRule rule : getConfig().getRules()) {
            if (!MatchItemManager.matchItemManager.getMatch(
                    rule.matchItem(), player.getInventory().getItemInMainHand())) {
                continue;
            }
            String blockId = HookManager.hookManager.getMatchingBlockId(origin, rule.matchBlocks());
            if (blockId != null) {
                return blockId;
            }
        }
        return null;
    }

    @Override
    protected List<Block> getCurrentBlocks(VeinData data) {
        List<Block> blocks = new ArrayList<>(data.blocks.size());
        for (LocationKey key : data.blocks) {
            Block block = key.getBlock();
            if (data.blockId.equals(HookManager.hookManager.getBlockId(block))) {
                blocks.add(block);
            }
        }
        return blocks;
    }

    @Override
    protected boolean isStillValid(VeinData data, List<Block> currentBlocks) {
        return currentBlocks.size() == data.blocks.size();
    }

    @Override
    protected void breakBlocks(Player player, MultiBlockSession session, List<Block> blocks) {
        if (!player.isOnline()) {
            removeSession(session);
            return;
        }
        Set<LocationKey> dropKeys = new HashSet<>();
        prepareDefaultDrops(session, blocks, dropKeys);
        getConfig().getBreakActions().runAllActions(player,
                getActionArgs(session.data(), blocks, blocks.size()));
        breakBlocksInBatches(player, session, blocks, block -> false,
                block -> dropKeys.remove(LocationKey.of(block)),
                () -> finish(session, player, dropKeys));
    }

    private List<Block> findVein(Player player, Block origin, String blockId) {
        Queue<Block> pending = new ArrayDeque<>();
        Set<LocationKey> visited = new HashSet<>();
        List<Block> result = new ArrayList<>();
        pending.add(origin);
        visited.add(LocationKey.of(origin));

        while (!pending.isEmpty() && result.size() < getConfig().getMaxBlocks()) {
            Block block = pending.remove();
            if (!blockId.equals(HookManager.hookManager.getBlockId(block))) {
                continue;
            }
            result.add(block);
            addNeighbours(block, pending, visited);
        }
        return result;
    }

    private void addNeighbours(Block block, Queue<Block> pending, Set<LocationKey> visited) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) {
                        continue;
                    }
                    if (!getConfig().shouldSearchDiagonally() && Math.abs(x) + Math.abs(y) + Math.abs(z) != 1) {
                        continue;
                    }
                    Block next = block.getRelative(x, y, z);
                    if (visited.add(LocationKey.of(next))) {
                        pending.add(next);
                    }
                }
            }
        }
    }

    protected record VeinData(String blockId, List<LocationKey> blocks) {
    }

    @Override
    protected String[] getActionArgs(VeinData data, List<Block> blocks, int miningBlockCount) {
        return new String[] {
                "block", data.blockId(),
                "block-amount", String.valueOf(blocks.size()),
                "mining-block-amount", String.valueOf(miningBlockCount)
        };
    }

    private record VeinKey(UUID worldId, int x, int y, int z) {

        private static VeinKey of(List<Block> blocks) {
            Block first = Collections.min(blocks, (left, right) -> {
                int x = Integer.compare(left.getX(), right.getX());
                if (x != 0) {
                    return x;
                }
                int y = Integer.compare(left.getY(), right.getY());
                return y != 0 ? y : Integer.compare(left.getZ(), right.getZ());
            });
            return new VeinKey(first.getWorld().getUID(), first.getX(), first.getY(), first.getZ());
        }
    }
}
