package cn.superiormc.ultimatetweak.objects.actions;

import cn.superiormc.ultimatetweak.UltimateTweak;

import org.bukkit.entity.Player;

public class ActionActionBar extends AbstractRunAction {

    public ActionActionBar() {
        super("action_bar");
        setRequiredArgs("message");
    }

    @Override
    protected void onDoAction(ObjectSingleAction singleAction, Player player) {
        String msg = singleAction.getString("message", player);
        UltimateTweak.methodUtil.sendActionBar(player, msg);
    }
}
