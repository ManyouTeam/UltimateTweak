package cn.superiormc.ultimatetweak.objects.actions;

import cn.superiormc.ultimatetweak.UltimateTweak;

import org.bukkit.entity.Player;

public class ActionOPCommand extends AbstractRunAction {

    public ActionOPCommand() {
        super("op_command");
        setRequiredArgs("command");
    }

    @Override
    protected void onDoAction(ObjectSingleAction singleAction, Player player) {
        UltimateTweak.methodUtil.dispatchOpCommand(player, singleAction.getString("command", player));
    }
}
