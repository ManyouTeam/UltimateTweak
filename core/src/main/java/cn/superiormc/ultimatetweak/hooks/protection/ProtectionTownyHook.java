package cn.superiormc.ultimatetweak.hooks.protection;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class ProtectionTownyHook extends AbstractProtectionHook {

    public ProtectionTownyHook() {
        super("Towny");
    }

    @Override
    public boolean canBreak(Player player, Location location) {
        Block block = location.getBlock();
        return PlayerCacheUtil.getCachePermission(player, block.getLocation(), block.getType(), TownyPermission.ActionType.DESTROY);
    }

    @Override
    public ProtectionRegionResult createRegion(ProtectionRegion region) {
        TownyWorld townyWorld = TownyAPI.getInstance().getTownyWorld(region.world());
        if (townyWorld == null) {
            return ProtectionRegionResult.failed("World is not a Towny world: " + region.world().getName());
        }
        TownyUniverse universe = TownyUniverse.getInstance();
        if (universe.hasTown(region.id())) {
            return ProtectionRegionResult.alreadyExistsResult();
        }
        for (int chunkX = region.minChunkX(); chunkX <= region.maxChunkX(); chunkX++) {
            for (int chunkZ = region.minChunkZ(); chunkZ <= region.maxChunkZ(); chunkZ++) {
                WorldCoord worldCoord = new WorldCoord(region.world(), chunkX, chunkZ);
                if (universe.hasTownBlock(worldCoord)) {
                    return ProtectionRegionResult.failed("Towny chunk overlaps an existing town block: "
                            + region.world().getName() + " " + chunkX + "," + chunkZ);
                }
            }
        }
        try {
            universe.newTown(region.id());
            Town town = universe.getTown(region.id());
            town.setWorld(townyWorld);
            town.setHasUpkeep(false);
            town.setBonusBlocks((region.maxChunkX() - region.minChunkX() + 1)
                    * (region.maxChunkZ() - region.minChunkZ() + 1));
            town.setSpawn(region.centerLocation());
            for (int chunkX = region.minChunkX(); chunkX <= region.maxChunkX(); chunkX++) {
                for (int chunkZ = region.minChunkZ(); chunkZ <= region.maxChunkZ(); chunkZ++) {
                    WorldCoord worldCoord = new WorldCoord(region.world(), chunkX, chunkZ);
                    TownBlock townBlock = new TownBlock(worldCoord);
                    townBlock.setName(region.id());
                    townBlock.setTown(town, false);
                    universe.addTownBlock(townBlock);
                    town.addTownBlock(townBlock);
                    townBlock.save();
                }
            }
            town.save();
            townyWorld.save();
            return ProtectionRegionResult.created();
        } catch (Exception exception) {
            return ProtectionRegionResult.failed(exception);
        }
    }

}
