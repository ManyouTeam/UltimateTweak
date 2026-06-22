package cn.superiormc.ultimatetweak.hooks.protection;

import net.william278.huskclaims.api.BukkitHuskClaimsAPI;
import net.william278.huskclaims.claim.ClaimWorld;
import net.william278.huskclaims.claim.Region;
import net.william278.huskclaims.libraries.cloplib.operation.OperationType;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ProtectionHuskClaimsHook extends AbstractProtectionHook {

    public BukkitHuskClaimsAPI api = BukkitHuskClaimsAPI.getInstance();

    public ProtectionHuskClaimsHook() {
        super("HuskClaims");
    }

    @Override
    public boolean canUse(Player player, Location location) {
        return api.isOperationAllowed(api.getOnlineUser(player.getUniqueId()), OperationType.BLOCK_BREAK, api.getPosition(location));
    }

    @Override
    public ProtectionRegionResult createRegion(ProtectionRegion region) {
        Optional<ClaimWorld> claimWorld = api.getClaimWorld(api.getWorld(region.world()));
        if (claimWorld.isEmpty()) {
            return ProtectionRegionResult.failed("World is not claimable by HuskClaims: " + region.world().getName());
        }
        Region huskRegion = Region.from(
                api.getBlockPosition(region.minX(), region.minZ()),
                api.getBlockPosition(region.maxX(), region.maxZ()));
        if (api.isRegionClaimed(claimWorld.get(), huskRegion)) {
            return ProtectionRegionResult.failed("HuskClaims region overlaps an existing claim: " + region.id());
        }
        try {
            api.createAdminClaim(claimWorld.get(), huskRegion).get(5, TimeUnit.SECONDS);
            return ProtectionRegionResult.created();
        } catch (Exception exception) {
            return ProtectionRegionResult.failed(exception);
        }
    }

}
