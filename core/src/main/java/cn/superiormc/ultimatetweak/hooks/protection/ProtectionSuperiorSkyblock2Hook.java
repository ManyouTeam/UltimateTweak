package cn.superiormc.ultimatetweak.hooks.protection;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ProtectionSuperiorSkyblock2Hook extends AbstractProtectionHook {

    public ProtectionSuperiorSkyblock2Hook() {
        super("SuperiorSkyblock2");
    }

    @Override
    public boolean canBreak(Player player, Location location) {
        Island island = SuperiorSkyblockAPI.getGrid().getIslandAt(location);
        if (island == null) {
            return true;
        }
        return island.hasPermission(player, IslandPrivilege.getByName("BREAK"));
    }

    @Override
    public ProtectionRegionResult createRegion(ProtectionRegion region) {
        return ProtectionRegionResult.unsupported(getPluginName());
    }

}
