package cn.superiormc.ultimatetweak.hooks.protection;

import cn.lunadeer.dominion.api.DominionAPI;
import cn.lunadeer.dominion.api.dtos.CuboidDTO;
import cn.lunadeer.dominion.api.dtos.DominionDTO;
import cn.lunadeer.dominion.api.dtos.flag.Flags;
import cn.lunadeer.dominion.providers.DominionProvider;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ProtectionDominionHook extends AbstractProtectionHook {

    public ProtectionDominionHook() {
        super("Dominion");
    }

    @Override
    public boolean canBreak(Player player, Location location) {
        try {
            DominionAPI dominionAPI = DominionAPI.getInstance();
            DominionDTO dominionDTO = dominionAPI.getDominion(location);
            if (dominionDTO != null) {
                return dominionAPI.checkPrivilegeFlag(dominionDTO, Flags.BREAK_BLOCK, player);
            }
            return true;
        } catch (Throwable throwable) {
            return true;
        }
    }

    @Override
    public ProtectionRegionResult createRegion(ProtectionRegion region) {
        try {
            DominionAPI dominionAPI = DominionAPI.getInstance();
            if (dominionAPI.getDominion(region.id()) != null) {
                return ProtectionRegionResult.alreadyExistsResult();
            }
            UUID worldUid = region.world().getUID();
            CuboidDTO cuboid = new CuboidDTO(region.minLocation(), region.maxLocation());
            for (DominionDTO dominion : dominionAPI.getAllDominions()) {
                if (worldUid.equals(dominion.getWorldUid()) && dominion.getCuboid().intersectWith(cuboid)) {
                    return ProtectionRegionResult.failed("Dominion region overlaps an existing dominion: " + dominion.getName());
                }
            }

            DominionProvider.getInstance().createDominion(
                    Bukkit.getConsoleSender(),
                    region.id(),
                    UUID.randomUUID(),
                    region.world(),
                    cuboid,
                    null,
                    true);
            return ProtectionRegionResult.created();
        } catch (Exception exception) {
            exception.printStackTrace();
            return ProtectionRegionResult.failed(exception);
        }
    }

}
