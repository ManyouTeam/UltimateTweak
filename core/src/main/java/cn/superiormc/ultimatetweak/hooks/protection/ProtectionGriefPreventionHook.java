package cn.superiormc.ultimatetweak.hooks.protection;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.CreateClaimResult;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ProtectionGriefPreventionHook extends AbstractProtectionHook {

    public ProtectionGriefPreventionHook() {
        super("GriefPrevention");
    }

    @Override
    public boolean canUse(Player player, Location location) {
        PlayerData playerData = GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId());
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, false, null);
        if (claim == null || playerData.ignoreClaims) {
            return true;
        }
        return claim.checkPermission(player, ClaimPermission.Build, null) == null;
    }

    @Override
    public ProtectionRegionResult createRegion(ProtectionRegion region) {
        if (GriefPrevention.instance.dataStore.getClaimAt(region.centerLocation(), false, null) != null
                || GriefPrevention.instance.dataStore.getClaimAt(region.minLocation(), false, null) != null
                || GriefPrevention.instance.dataStore.getClaimAt(region.maxLocation(), false, null) != null) {
            return ProtectionRegionResult.failed("GriefPrevention claim overlaps an existing claim: " + region.id());
        }
        CreateClaimResult result = GriefPrevention.instance.dataStore.createClaim(region.world(),
                region.minX(), region.maxX(),
                region.minY(), region.maxY(),
                region.minZ(), region.maxZ(),
                null, null, null, null);
        return result.succeeded
                ? ProtectionRegionResult.created()
                : ProtectionRegionResult.failed("GriefPrevention refused to create admin claim: " + region.id());
    }

}
