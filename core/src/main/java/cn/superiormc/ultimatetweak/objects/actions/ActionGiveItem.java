package cn.superiormc.ultimatetweak.objects.actions;

import cn.superiormc.ultimatetweak.UltimateTweak;

import cn.superiormc.ultimatetweak.managers.ConfigManager;
import cn.superiormc.ultimatetweak.methods.BuildItem;
import cn.superiormc.ultimatetweak.utils.CommonUtil;
import cn.superiormc.ultimatetweak.utils.TextUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ActionGiveItem extends AbstractRunAction {

    public ActionGiveItem() {
        super("give_item");
        setRequiredArgs("item");
    }

    @Override
    protected void onDoAction(ObjectSingleAction singleAction, Player player) {
        ItemStack item = BuildItem.buildItemStack(player, singleAction.getSection().getConfigurationSection("item"));
        CommonUtil.giveOrDrop(player, item);
    }
}
