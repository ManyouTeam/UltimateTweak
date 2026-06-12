package cn.superiormc.ultimatetweak.objects.actions;


import cn.superiormc.ultimatetweak.objects.ObjectAction;
import cn.superiormc.ultimatetweak.utils.SchedulerUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class ActionDelay extends AbstractRunAction {

    public ActionDelay() {
        super("delay");
        setRequiredArgs("time", "actions");
    }

    @Override
    protected void onDoAction(ObjectSingleAction singleAction, Player player) {
        ConfigurationSection chanceSection = singleAction.getSection().getConfigurationSection("actions");
        if (chanceSection == null) {
            return;
        }
        long time = singleAction.getSection().getLong("time");
        ObjectAction action = new ObjectAction(chanceSection);
        SchedulerUtil.runTaskLater(() -> action.runAllActions(player), time);
    }
}
