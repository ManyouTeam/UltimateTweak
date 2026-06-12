package cn.superiormc.ultimatetweak.managers;

import cn.gtemc.itembridge.api.ItemBridge;
import cn.gtemc.itembridge.api.util.Pair;
import cn.gtemc.itembridge.core.BukkitItemBridge;
import cn.superiormc.ultimatetweak.hooks.blocks.*;
import cn.superiormc.ultimatetweak.hooks.items.*;
import cn.superiormc.ultimatetweak.hooks.protection.*;
import cn.superiormc.ultimatetweak.utils.CommonUtil;
import cn.superiormc.ultimatetweak.utils.TextUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HookManager {

    public static HookManager hookManager;

    private Map<String, AbstractItemHook> itemHooks;

    private Map<String, AbstractProtectionHook> protectionHooks;

    private Map<String, AbstractBlockHook> blockHooks;

    private ItemBridge<ItemStack, Player> itemBridgeHook = null;

    public HookManager() {
        hookManager = this;
        initBlockHook();
        initProtectionHook();
        initItemHook();
        if (ConfigManager.configManager.getString("hook-item-method").equalsIgnoreCase("ITEMBRIDGE")) {
            itemBridgeHook = BukkitItemBridge.builder()
                    .onHookSuccess(p -> TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fUSItemBridge successfully hook into " + p + "."))
                    .detectSupportedPlugins()
                    .build();
        }
    }

    private void initItemHook() {
        itemHooks = new HashMap<>();
        if (CommonUtil.checkPluginLoad("ItemsAdder")) {
            registerNewItemHook("ItemsAdder", new ItemItemsAdderHook());
        }
        if (CommonUtil.checkPluginLoad("Oraxen")) {
            registerNewItemHook("Oraxen", new ItemOraxenHook());
        }
        if (CommonUtil.checkPluginLoad("MMOItems")) {
            registerNewItemHook("MMOItems", new ItemMMOItemsHook());
        }
        if (CommonUtil.checkPluginLoad("EcoItems")) {
            registerNewItemHook("EcoItems", new ItemEcoItemsHook());
        }
        if (CommonUtil.checkPluginLoad("EcoArmor")) {
            registerNewItemHook("EcoArmor", new ItemEcoArmorHook());
        }
        if (CommonUtil.checkPluginLoad("MythicMobs")) {
            registerNewItemHook("MythicMobs", new ItemMythicMobsHook());
        }
        if (CommonUtil.checkPluginLoad("eco")) {
            registerNewItemHook("eco", new ItemecoHook());
        }
        if (CommonUtil.checkPluginLoad("NeigeItems")) {
            registerNewItemHook("NeigeItems", new ItemNeigeItemsHook());
        }
        if (CommonUtil.checkPluginLoad("ExecutableItems")) {
            registerNewItemHook("ExecutableItems", new ItemExecutableItemsHook());
        }
        if (CommonUtil.checkPluginLoad("Nexo")) {
            registerNewItemHook("Nexo", new ItemNexoHook());
        }
        if (CommonUtil.checkPluginLoad("CraftEngine")) {
            registerNewItemHook("CraftEngine", new ItemCraftEngineHook());
        }
    }

    private void initProtectionHook() {
        protectionHooks = new HashMap<>();
        if (CommonUtil.checkPluginLoad("WorldGuard")) {
            registerNewProtectionHook("WorldGuard", new ProtectionWorldGuardHook());
        }
        if (CommonUtil.checkPluginLoad("Residence")) {
            registerNewProtectionHook("Residence", new ProtectionResidenceHook());
        }
        if (CommonUtil.checkPluginLoad("GriefPrevention")) {
            registerNewProtectionHook("GriefPrevention", new ProtectionGriefPreventionHook());
        }
        if (CommonUtil.checkPluginLoad("Lands")) {
            registerNewProtectionHook("Lands", new ProtectionLandsHook());
        }
        if (CommonUtil.checkPluginLoad("HuskTowns")) {
            registerNewProtectionHook("HuskTowns", new ProtectionHuskTownsHook());
        }
        if (CommonUtil.checkPluginLoad("HuskClaims")) {
            registerNewProtectionHook("HuskClaims", new ProtectionHuskClaimsHook());
        }
        if (CommonUtil.checkPluginLoad("PlotSquared")) {
            registerNewProtectionHook("PlotSquared", new ProtectionPlotSquaredHook());
        }
        if (CommonUtil.checkPluginLoad("Towny")) {
            registerNewProtectionHook("Towny", new ProtectionTownyHook());
        }
        if (CommonUtil.checkPluginLoad("BentoBox")) {
            registerNewProtectionHook("BentoBox", new ProtectionBentoBoxHook());
        }
        if (CommonUtil.checkPluginLoad("Dominion")) {
            registerNewProtectionHook("Dominion", new ProtectionDominionHook());
        }
        if (CommonUtil.checkPluginLoad("SuperiorSkyblock2")) {
            registerNewProtectionHook("SuperiorSkyblock2", new ProtectionSuperiorSkyblock2Hook());
        }
    }

    private void initBlockHook() {
        blockHooks = new HashMap<>();
        registerNewBlockHook("Vanilla", new MinecraftBlockChecker());
        if (CommonUtil.checkPluginLoad("CraftEngine")) {
            registerNewBlockHook("CraftEngine", new CraftEngineBlockChecker());
        }
        if (CommonUtil.checkPluginLoad("Oraxen")) {
            registerNewBlockHook("Oraxen", new OraxenBlockChecker());
        }
        if (CommonUtil.checkPluginLoad("Nexo")) {
            registerNewBlockHook("Nexo", new NexoBlockChecker());
        }
        if (CommonUtil.checkPluginLoad("ItemsAdder")) {
            registerNewBlockHook("ItemsAdder", new ItemsAdderBlockChecker());
        }
    }

    public void registerNewItemHook(String pluginName,
                                    AbstractItemHook itemHook) {
        if (!itemHooks.containsKey(pluginName)) {
            TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fHooking into " + pluginName + "...");
            itemHooks.put(pluginName, itemHook);
        }
    }

    public void registerNewProtectionHook(String pluginName,
                                          AbstractProtectionHook protectionHook) {
        if (!protectionHooks.containsKey(pluginName)) {
            TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fHooking into " + pluginName + "...");
            protectionHooks.put(pluginName, protectionHook);
        }
    }

    public void registerNewBlockHook(String pluginName, AbstractBlockHook checker) {
        if (!blockHooks.containsKey(pluginName)) {
            TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fHooking into: " + pluginName + "...");
            blockHooks.put(pluginName, checker);
        }
    }

    public ItemStack getHookItem(Player player, String pluginName, String itemID) {
        if (itemBridgeHook != null) {
            Optional<ItemStack> tempVal1 = itemBridgeHook.build(pluginName, player, itemID);
            if (tempVal1.isPresent()) {
                return tempVal1.get();
            }
        }
        if (!itemHooks.containsKey(pluginName)) {
            ErrorManager.errorManager.sendErrorMessage("§cError: Can not hook into "
                    + pluginName + " plugin, maybe we do not support this plugin, or your server didn't correctly load " +
                    "this plugin!");
            return null;
        }
        AbstractItemHook itemHook = itemHooks.get(pluginName);
        return itemHook.getHookItemByID(player, itemID);
    }


    public String[] getHookItemPluginAndID(ItemStack hookItem) {
        if (itemBridgeHook != null) {
            Pair<String, String> tempVal1 = itemBridgeHook.getFirstId(hookItem);
            if (tempVal1 != null) {
                return new String[]{tempVal1.left, tempVal1.right};
            }
        }
        for (AbstractItemHook itemHook : itemHooks.values()) {
            String itemID = itemHook.getIDByItemStack(hookItem);
            if (itemID != null) {
                return new String[]{itemHook.getPluginName(), itemHook.getIDByItemStack(hookItem)};
            }
        }
        return null;
    }

    public String parseItemID(ItemStack hookItem, boolean useTier) {
        if (!hookItem.hasItemMeta()) {
            return hookItem.getType().name().toLowerCase();
        }
        for (AbstractItemHook itemHook : itemHooks.values()) {
            String tempVal1 = itemHook.getSimplyIDByItemStack(hookItem, useTier);
            if (tempVal1 != null) {
                return tempVal1;
            }
        }
        return hookItem.getType().name().toLowerCase();
    }

    public boolean getProtectionCanUse(Player player, Location location) {
        if (player == null || player.isOp() || player.hasPermission("ultimatetweak.bypass.protection")) {
            return true;
        }
        for (AbstractProtectionHook protectionHook : protectionHooks.values()) {
            if (!protectionHook.canUse(player, location)) {
                return false;
            }
        }
        return true;
    }

    public AbstractBlockHook getSuitableChecker(String materialString) {
        for (AbstractBlockHook checker : blockHooks.values()) {
            if (checker.canCheck(materialString)) {
                return checker;
            }
        }
        return null;
    }

    @Nullable
    public String getBlockId(Block block) {
        AbstractBlockHook minecraftChecker = null;

        for (AbstractBlockHook checker : blockHooks.values()) {
            if (checker instanceof MinecraftBlockChecker) {
                minecraftChecker = checker;
                continue;
            }

            String blockId = checker.getBlockId(block);
            if (blockId != null) {
                return blockId;
            }
        }

        return minecraftChecker == null ? null : minecraftChecker.getBlockId(block);
    }

    @Nullable
    public String getMatchingBlockId(Block block, Collection<String> availableIds) {
        AbstractBlockHook minecraftChecker = null;

        for (AbstractBlockHook checker : blockHooks.values()) {
            if (checker instanceof MinecraftBlockChecker) {
                minecraftChecker = checker;
                continue;
            }

            String blockId = checker.getBlockId(block);
            if (blockId != null && availableIds.contains(blockId)) {
                return blockId;
            }
        }

        if (minecraftChecker == null) {
            return null;
        }

        String vanillaBlockId = minecraftChecker.getBlockId(block);
        if (vanillaBlockId != null && availableIds.contains(vanillaBlockId)) {
            return vanillaBlockId;
        }
        return null;
    }
}