package cn.superiormc.ultimatetweak.hooks.protection;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.protection.ResidenceManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ProtectionResidenceHook extends AbstractProtectionHook {

    public ProtectionResidenceHook() {
        super("Residence");
    }

    @Override
    public boolean canBreak(Player player, Location location) {
        ResidencePlayer rPlayer = Residence.getInstance().getPlayerManager().getResidencePlayer(player);
        return rPlayer.canBreakBlock(location.getBlock(), true);
    }

    @Override
    public ProtectionRegionResult createRegion(ProtectionRegion region) {
        ResidenceManager residenceManager = Residence.getInstance().getResidenceManager();
        if (residenceManager.getByName(region.id()) != null) {
            return ProtectionRegionResult.alreadyExistsResult();
        }
        if (residenceManager.collidesWithResidence(
                new com.bekvon.bukkit.residence.protection.CuboidArea(region.minLocation(), region.maxLocation())) != null) {
            return ProtectionRegionResult.failed("Residence region overlaps an existing residence: " + region.id());
        }
        return residenceManager.addResidence(region.id(), region.minLocation(), region.maxLocation())
                ? ProtectionRegionResult.created()
                : ProtectionRegionResult.failed("Residence refused to create region: " + region.id());
    }

}
