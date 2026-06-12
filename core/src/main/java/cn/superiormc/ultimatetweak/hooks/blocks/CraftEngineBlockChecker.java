package cn.superiormc.ultimatetweak.hooks.blocks;

import cn.superiormc.ultimatetweak.managers.ConfigManager;
import cn.superiormc.ultimatetweak.managers.ErrorManager;
import cn.superiormc.ultimatetweak.utils.CommonUtil;
import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import net.momirealms.craftengine.core.block.BlockDefinition;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class CraftEngineBlockChecker extends AbstractBlockHook {

    @Override
    public String getBlockId(Block block) {
        if (!CommonUtil.checkPluginLoad("CraftEngine")) {
            return null;
        }

        try {
            ImmutableBlockState craftEngineBlock = CraftEngineBlocks.getCustomBlockState(block);
            if (craftEngineBlock == null || craftEngineBlock.owner() == null || craftEngineBlock.owner().value() == null) {
                return null;
            }
            return "craftengine:" + craftEngineBlock.owner().value().id();
        } catch (Exception e) {
            if (ConfigManager.configManager.getBoolean("debug", false)) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public boolean check(Block block, String materialString, Location location) {
        if (!CommonUtil.checkPluginLoad("CraftEngine")) {
            ErrorManager.errorManager.sendErrorMessage("§cError: CraftEngine is not loaded but you are using block from it!");
            return false;
        }
        String[] parts = materialString.split(":");
        if (!isValidMaterialFormat(parts, 3)) {
            return false;
        }
        try {
            ImmutableBlockState craftEngineBlock = CraftEngineBlocks.getCustomBlockState(block);
            if (craftEngineBlock == null) {
                return false;
            }
            BlockDefinition customBlock = craftEngineBlock.owner().value();
            String expectedId = parts[1] + ":" + parts[2];
            return customBlock != null && expectedId.equals(customBlock.id().toString());
        } catch (Exception e) {
            if (ConfigManager.configManager.getBoolean("debug", false)) {
                e.printStackTrace();
            }
            return false;
        }
    }

    @Override
    public void placeBlock(String materialString, Location location) {
        if (!CommonUtil.checkPluginLoad("CraftEngine")) {
            ErrorManager.errorManager.sendErrorMessage("§cError: CraftEngine is not loaded but you are using block from it!");
            return;
        }
        String[] parts = materialString.split(":");
        if (!isValidMaterialFormat(parts, 3)) {
            return;
        }
        try {
            CraftEngineBlocks.place(location, Key.of(parts[1], parts[2]), true);
        } catch (Exception e) {
            if (ConfigManager.configManager.getBoolean("debug", false)) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected String getCheckerName() {
        return "craftengine";
    }
}
