package cn.superiormc.ultimatetweak.objects.actions;


import cn.superiormc.ultimatetweak.managers.ErrorManager;
import org.bukkit.entity.Player;

public abstract class AbstractRunAction {

    private final String type;

    private String[] requiredArgs;

    public AbstractRunAction(String type) {
        this.type = type;
    }

    protected void setRequiredArgs(String... requiredArgs) {
        this.requiredArgs = requiredArgs;
    }

    public void runAction(ObjectSingleAction singleAction, Player player) {
        if (requiredArgs != null) {
            for (String arg : requiredArgs) {
                if (!singleAction.getSection().contains(arg)) {
                    ErrorManager.errorManager.sendErrorMessage("§cError: Your action missing required arg: " + arg + ".");
                    return;
                }
            }
        }
        onDoAction(singleAction, player);
    }

    protected abstract void onDoAction(ObjectSingleAction singleAction, Player player);

    public String getType() {
        return type;
    }

    protected int parseMinPlayers(String msg) {
        if (msg == null || !msg.startsWith("@")) {
            return 0;
        }
        int idx = msg.indexOf(" ");
        if (idx == -1) {
            return 0;
        }
        try {
            return Integer.parseInt(msg.substring(1, idx));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
