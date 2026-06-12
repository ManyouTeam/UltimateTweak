package cn.superiormc.ultimatetweak.objects.actions;

import cn.superiormc.ultimatetweak.UltimateTweak;

import org.bukkit.entity.Player;

public class ActionTitle extends AbstractRunAction {

    public ActionTitle() {
        super("title");
        setRequiredArgs("main-title", "sub-title", "fade-in", "stay", "fade-out");
    }

    @Override
    protected void onDoAction(ObjectSingleAction singleAction, Player player) {
        UltimateTweak.methodUtil.sendTitle(player,
                singleAction.getString("main-title", player),
                singleAction.getString("sub-title", player),
                singleAction.getInt("fade-in"),
                singleAction.getInt("stay"),
                singleAction.getInt("fade-out"));
    }
}
