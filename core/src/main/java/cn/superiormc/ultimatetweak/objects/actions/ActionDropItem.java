package cn.superiormc.ultimatetweak.objects.actions;

import cn.superiormc.ultimatetweak.UltimateTweak;

import cn.superiormc.ultimatetweak.managers.ConfigManager;
import cn.superiormc.ultimatetweak.methods.BuildItem;
import cn.superiormc.ultimatetweak.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ActionDropItem extends AbstractRunAction {

    public ActionDropItem() {
        super("drop_item");
        setRequiredArgs("item");
    }

    @Override
    protected void onDoAction(ObjectSingleAction singleAction, Player player) {
        String worldName = singleAction.getString("world");
        Location location;
        if (worldName == null) {
            location = player.getLocation();
        } else {
            World world = Bukkit.getWorld(worldName);
            location = new Location(world,
                    singleAction.getDouble("x", player),
                    singleAction.getDouble("y", player),
                    singleAction.getDouble("z", player));

        }
        ItemStack item = BuildItem.buildItemStack(player, singleAction.getSection().getConfigurationSection("item"));
        UltimateTweak.methodUtil.dropPrivateItem(player, item, location);
    }
}
