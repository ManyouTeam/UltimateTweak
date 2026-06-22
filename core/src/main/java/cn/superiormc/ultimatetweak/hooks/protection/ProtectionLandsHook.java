package cn.superiormc.ultimatetweak.hooks.protection;

import cn.superiormc.ultimatetweak.UltimateTweak;
import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.flags.type.Flags;
import me.angeschossen.lands.api.land.Land;
import me.angeschossen.lands.api.land.LandWorld;
import me.angeschossen.lands.api.land.enums.LandType;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class ProtectionLandsHook extends AbstractProtectionHook {

    public LandsIntegration api = LandsIntegration.of(UltimateTweak.instance);

    public ProtectionLandsHook() {
        super("Lands");
    }

    @Override
    public boolean canUse(Player player, Location location) {
        LandWorld world = api.getWorld(location.getWorld());
        if (world != null) {
            return world.hasRoleFlag(api.getLandPlayer(player.getUniqueId()),
                    location,
                    Flags.BLOCK_BREAK, location.getBlock().getType(),
                    false);
        }
        return true;
    }

    @Override
    public ProtectionRegionResult createRegion(ProtectionRegion region) {
        Land existing = api.getLandByName(region.id());
        if (existing != null) {
            return ProtectionRegionResult.alreadyExistsResult();
        }
        for (int chunkX = region.minChunkX(); chunkX <= region.maxChunkX(); chunkX++) {
            for (int chunkZ = region.minChunkZ(); chunkZ <= region.maxChunkZ(); chunkZ++) {
                if (api.getLandByChunk(region.world(), chunkX, chunkZ) != null) {
                    return ProtectionRegionResult.failed("Lands chunk overlaps an existing land: "
                            + region.world().getName() + " " + chunkX + "," + chunkZ);
                }
            }
        }
        try {
            Land land = Land.of(region.id(), LandType.ADMIN, region.centerLocation(), null, false, false)
                    .get(5, TimeUnit.SECONDS);
            if (land == null) {
                return ProtectionRegionResult.failed("Lands refused to create admin land: " + region.id());
            }
            for (int chunkX = region.minChunkX(); chunkX <= region.maxChunkX(); chunkX++) {
                for (int chunkZ = region.minChunkZ(); chunkZ <= region.maxChunkZ(); chunkZ++) {
                    Boolean claimed = land.claimChunk(null, region.world(), chunkX, chunkZ).get(5, TimeUnit.SECONDS);
                    if (!Boolean.TRUE.equals(claimed)) {
                        return ProtectionRegionResult.failed("Lands refused to claim chunk: "
                                + region.world().getName() + " " + chunkX + "," + chunkZ);
                    }
                }
            }
            return ProtectionRegionResult.created();
        } catch (Exception exception) {
            return ProtectionRegionResult.failed(exception);
        }
    }

}
