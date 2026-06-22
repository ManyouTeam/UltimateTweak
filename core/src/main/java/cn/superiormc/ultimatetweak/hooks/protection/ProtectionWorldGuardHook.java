package cn.superiormc.ultimatetweak.hooks.protection;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ProtectionWorldGuardHook extends AbstractProtectionHook {

    public ProtectionWorldGuardHook() {
        super("WorldGuard");
    }

    @Override
    public boolean canUse(Player player, Location location) {
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        return WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().testBuild(BukkitAdapter.adapt(location), localPlayer, Flags.BLOCK_BREAK)
                || WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer, BukkitAdapter.adapt(player.getWorld()));
    }

    @Override
    public ProtectionRegionResult createRegion(ProtectionRegion region) {
        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer()
                .get(BukkitAdapter.adapt(region.world()));
        if (regionManager == null) {
            return ProtectionRegionResult.failed("WorldGuard region manager is not available for world " + region.world().getName());
        }
        if (regionManager.hasRegion(region.id())) {
            return ProtectionRegionResult.alreadyExistsResult();
        }
        ProtectedCuboidRegion worldGuardRegion = new ProtectedCuboidRegion(region.id(),
                BlockVector3.at(region.minX(), region.minY(), region.minZ()),
                BlockVector3.at(region.maxX(), region.maxY(), region.maxZ()));
        regionManager.addRegion(worldGuardRegion);
        try {
            regionManager.saveChanges();
        } catch (StorageException exception) {
            return ProtectionRegionResult.failed(exception.getMessage());
        }
        return ProtectionRegionResult.created();
    }
}
