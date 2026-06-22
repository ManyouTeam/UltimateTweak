package cn.superiormc.ultimatetweak.hooks.protection;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.lists.Flags;
import world.bentobox.bentobox.managers.IslandsManager;

public class ProtectionBentoBoxHook extends AbstractProtectionHook {

    public ProtectionBentoBoxHook() {
        super("BentoBox");
    }

    @Override
    public boolean canUse(Player player, Location location) {
        Island island = BentoBox.getInstance().getIslandsManager().getIslandAt(location).orElse(null);
        if (island != null) {
            return island.isAllowed(User.getInstance(player), Flags.BREAK_BLOCKS);
        }
        return true;
    }

    @Override
    public ProtectionRegionResult createRegion(ProtectionRegion region) {
        BentoBox bentoBox = BentoBox.getInstance();
        if (!bentoBox.getIWM().inWorld(region.world())) {
            return ProtectionRegionResult.failed("World is not a BentoBox island world: " + region.world().getName());
        }
        IslandsManager islandsManager = bentoBox.getIslandsManager();
        if (islandsManager.getProtectedIslandAt(region.centerLocation()).isPresent()) {
            return ProtectionRegionResult.alreadyExistsResult();
        }
        Island island = islandsManager.createIsland(region.centerLocation());
        if (island == null) {
            return ProtectionRegionResult.failed("BentoBox refused to create island: " + region.id());
        }
        island.setName(region.id());
        island.setProtectionRange(region.maxHorizontalRadius());
        island.setRange(region.maxHorizontalRadius());
        IslandsManager.saveIsland(island);
        return ProtectionRegionResult.created();
    }

}
