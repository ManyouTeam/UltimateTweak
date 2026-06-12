package cn.superiormc.ultimatetweak.objects.actions;

import cn.superiormc.ultimatetweak.UltimateTweak;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ActionTeleport extends AbstractRunAction {

    public ActionTeleport() {
        super("teleport");
        setRequiredArgs("world", "x", "y", "z");
    }

    @Override
    protected void onDoAction(ObjectSingleAction singleAction, Player player) {
        Location loc = new Location(Bukkit.getWorld(singleAction.getString("world")),
                    singleAction.getDouble("x", player),
                    singleAction.getDouble("y", player),
                    singleAction.getDouble("z", player),
                    singleAction.getInt("yaw", (int) player.getLocation().getYaw()),
                    singleAction.getInt("pitch", (int) player.getLocation().getPitch()));
        UltimateTweak.methodUtil.playerTeleport(player, loc);
    }
}
