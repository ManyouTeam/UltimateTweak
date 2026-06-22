package cn.superiormc.ultimatetweak.objects.actions;


import cn.superiormc.ultimatetweak.managers.ActionManager;
import cn.superiormc.ultimatetweak.objects.AbstractSingleRun;
import cn.superiormc.ultimatetweak.objects.ObjectAction;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class ObjectSingleAction extends AbstractSingleRun {

    private static final ThreadLocal<String[]> CURRENT_ARGS = ThreadLocal.withInitial(() -> new String[0]);

    private final ObjectAction action;

    private String[] args = new String[0];

    public ObjectSingleAction(ObjectAction action, ConfigurationSection actionSection) {
        super(actionSection);
        this.action = action;
    }

    public void doAction(Player player) {
        ActionManager.actionManager.doAction(this, player);
    }

    public void doAction(Player player, String... args) {
        this.args = args == null ? new String[0] : args;
        String[] previousArgs = CURRENT_ARGS.get();
        CURRENT_ARGS.set(this.args);
        try {
            ActionManager.actionManager.doAction(this, player);
        } finally {
            this.args = new String[0];
            CURRENT_ARGS.set(previousArgs);
        }
    }

    @Override
    protected String[] getExtraArgs() {
        return args.length == 0 ? CURRENT_ARGS.get() : args;
    }

    public String[] getActiveArgs() {
        return getExtraArgs();
    }

    public ObjectAction getAction() {
        return action;
    }

}
