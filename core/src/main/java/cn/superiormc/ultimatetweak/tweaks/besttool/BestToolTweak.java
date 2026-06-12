package cn.superiormc.ultimatetweak.tweaks.besttool;

import cn.superiormc.ultimatetweak.UltimateTweak;
import cn.superiormc.ultimatetweak.managers.HookManager;
import cn.superiormc.ultimatetweak.tweaks.AbstractTweak;
import cn.superiormc.ultimatetweak.tweaks.TweakEventType;
import cn.superiormc.ultimatetweak.tweaks.config.BestToolConfig;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BestToolTweak extends AbstractTweak<BestToolConfig> {

    private static final int HOTBAR_SIZE = 9;

    private static final double SCORE_EPSILON = 0.000001;

    private static final long NANOS_PER_TICK = 50_000_000L;

    private final Map<UUID, PlayerCache> playerCaches = new ConcurrentHashMap<>();

    private final Map<UUID, Long> lastDetectionNanos = new ConcurrentHashMap<>();

    public BestToolTweak(BestToolConfig config) {
        super("BestTool", config);
    }

    @Override
    public Set<TweakEventType> getEventTypes() {
        return EnumSet.of(TweakEventType.BLOCK_DAMAGE, TweakEventType.PLAYER_QUIT);
    }

    @Override
    public void onReload() {
        playerCaches.clear();
        lastDetectionNanos.clear();
        super.onReload();
    }

    @Override
    public void onDisable() {
        playerCaches.clear();
        lastDetectionNanos.clear();
    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        playerCaches.remove(playerId);
        lastDetectionNanos.remove(playerId);
    }

    @Override
    public void onBlockDamage(BlockDamageEvent event) {
        Player player = event.getPlayer();
        if (!getConfig().getConditions().getAllBoolean(player)) {
            return;
        }
        if (!canDetect(player.getUniqueId())) {
            return;
        }
        PlayerInventory inventory = player.getInventory();
        int currentSlot = inventory.getHeldItemSlot();
        int bestSlot = getBestSlot(player, event.getBlock());
        if (bestSlot == currentSlot) {
            return;
        }

        if (bestSlot < HOTBAR_SIZE) {
            inventory.setHeldItemSlot(bestSlot);
        } else {
            ItemStack mainHand = inventory.getItem(currentSlot);
            inventory.setItem(currentSlot, inventory.getItem(bestSlot));
            inventory.setItem(bestSlot, mainHand);
            playerCaches.remove(player.getUniqueId());
        }
        getConfig().getSwitchActions().runAllActions(player);
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

    private int getBestSlot(Player player, Block block) {
        PlayerInventory inventory = player.getInventory();
        int signature = getInventorySignature(inventory);
        PlayerCache cache = playerCaches.compute(player.getUniqueId(), (ignored, existing) ->
                existing != null && existing.inventorySignature == signature
                        ? existing : new PlayerCache(signature, new HashMap<>()));
        String blockId = HookManager.hookManager.getBlockId(block);
        if (blockId != null) {
            Integer cachedSlot = cache.bestSlots.get(blockId);
            if (cachedSlot != null) {
                return cachedSlot;
            }
        }

        int currentSlot = inventory.getHeldItemSlot();
        int bestSlot = currentSlot;
        double bestScore = getScore(block, inventory.getItemInMainHand());
        int slotCount = getConfig().shouldSearchEntireInventory()
                ? inventory.getStorageContents().length : HOTBAR_SIZE;
        for (int slot = 0; slot < slotCount; slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item == null || item.getType().isAir()) {
                continue;
            }
            double score = getScore(block, item);
            if (score > bestScore + SCORE_EPSILON) {
                bestScore = score;
                bestSlot = slot;
            }
        }
        if (blockId != null) {
            cache.bestSlots.put(blockId, bestSlot);
        }
        return bestSlot;
    }

    private int getInventorySignature(PlayerInventory inventory) {
        int result = 1;
        int slotCount = getConfig().shouldSearchEntireInventory()
                ? inventory.getStorageContents().length : HOTBAR_SIZE;
        for (int slot = 0; slot < slotCount; slot++) {
            result = 31 * result + getToolSignature(inventory.getItem(slot));
        }
        return result;
    }

    private int getToolSignature(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return 0;
        }
        return item.hashCode();
    }

    private double getScore(Block block, ItemStack item) {
        ItemStack tool = item == null ? new ItemStack(Material.AIR) : item;
        double score = UltimateTweak.methodUtil.getDestroySpeed(block, tool);
        int fortuneLevel = tool.getEnchantmentLevel(Enchantment.FORTUNE);
        if (fortuneLevel > 0 && block.isPreferredTool(tool)) {
            score *= Math.pow(getConfig().getFortuneMultiplierPerLevel(), fortuneLevel);
        }
        return score;
    }

    private record PlayerCache(int inventorySignature, Map<String, Integer> bestSlots) {
    }
}
