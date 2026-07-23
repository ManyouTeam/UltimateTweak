package cn.superiormc.ultimatetweak.tweaks.treereplant;

import cn.superiormc.ultimatetweak.hooks.blocks.AbstractBlockHook;
import cn.superiormc.ultimatetweak.managers.HookManager;
import cn.superiormc.ultimatetweak.managers.MatchItemManager;
import cn.superiormc.ultimatetweak.tweaks.AbstractTweak;
import cn.superiormc.ultimatetweak.tweaks.TweakEventType;
import cn.superiormc.ultimatetweak.tweaks.config.TreeReplantConfig;
import cn.superiormc.ultimatetweak.tweaks.config.TreeReplantConfig.ReplantRule;
import cn.superiormc.ultimatetweak.tweaks.config.TreeReplantConfig.SaplingDefinition;
import cn.superiormc.ultimatetweak.utils.SchedulerUtil;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.Set;

public class TreeReplantTweak extends AbstractTweak<TreeReplantConfig> {

    public TreeReplantTweak(TreeReplantConfig config) {
        super("TreeReplant", config);
    }

    @Override
    public Set<TweakEventType> getEventTypes() {
        return EnumSet.of(TweakEventType.ITEM_SPAWN);
    }

    @Override
    public void onItemSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();
        MatchedSapling matched = getSapling(item.getItemStack());
        if (matched == null) {
            return;
        }

        int intervalTicks = getConfig().getCheckIntervalTicks();
        int maxChecks = Math.max(1, getConfig().getMaxWaitTicks() / intervalTicks);
        int[] checks = {0};
        SchedulerUtil[] task = new SchedulerUtil[1];
        task[0] = SchedulerUtil.runTaskTimer(item, () -> {
            checks[0]++;
            if (!item.isValid() || item.isDead() || checks[0] >= maxChecks) {
                task[0].cancel();
                return;
            }
            if (!isWorldEnabled(item.getWorld())) {
                task[0].cancel();
                return;
            }
            if (!item.isOnGround()) {
                return;
            }
            tryPlant(item, matched);
            task[0].cancel();
        }, getConfig().getCooldownTicks(), intervalTicks);
    }

    private MatchedSapling getSapling(ItemStack itemStack) {
        for (ReplantRule rule : getConfig().getRules()) {
            for (SaplingDefinition sapling : rule.saplings()) {
                if (MatchItemManager.matchItemManager.getMatch(sapling.item(), itemStack)) {
                    return new MatchedSapling(sapling, rule.validSoils());
                }
            }
        }
        return null;
    }

    private void tryPlant(Item item, MatchedSapling matched) {
        Block target = item.getLocation().getBlock();
        SaplingDefinition sapling = matched.sapling();
        Set<String> validSoils = matched.validSoils();
        if (HookManager.hookManager.getMatchingBlockId(target, validSoils) != null) {
            target = target.getRelative(BlockFace.UP);
        }
        AbstractBlockHook blockHook = HookManager.hookManager.getSuitableChecker(sapling.block());
        if (!target.isEmpty()
                || blockHook == null
                || HookManager.hookManager.getMatchingBlockId(target.getRelative(BlockFace.DOWN), validSoils) == null) {
            return;
        }

        blockHook.placeBlock(sapling.block(), target.getLocation());
        if (!blockHook.check(target, sapling.block(), target.getLocation())) {
            return;
        }
        ItemStack itemStack = item.getItemStack();
        if (itemStack.getAmount() <= 1) {
            item.remove();
        } else {
            itemStack.setAmount(itemStack.getAmount() - 1);
            item.setItemStack(itemStack);
        }
    }

    private record MatchedSapling(SaplingDefinition sapling, Set<String> validSoils) {
    }
}
