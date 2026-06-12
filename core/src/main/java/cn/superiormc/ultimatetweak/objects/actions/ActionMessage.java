package cn.superiormc.ultimatetweak.objects.actions;

import cn.superiormc.ultimatetweak.utils.TextUtil;
import org.bukkit.entity.Player;

import java.util.List;

public class ActionMessage extends AbstractRunAction {

    public ActionMessage() {
        super("message");
    }

    @Override
    protected void onDoAction(ObjectSingleAction singleAction, Player player) {
        String message = singleAction.getString("message", player);
        if (message != null && !message.isEmpty()) {
            TextUtil.sendMessage(player, message);
        }
    }
}