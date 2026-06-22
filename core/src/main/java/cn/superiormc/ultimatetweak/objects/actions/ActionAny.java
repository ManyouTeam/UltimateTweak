package cn.superiormc.ultimatetweak.objects.actions;


import cn.superiormc.ultimatetweak.objects.ObjectAction;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class ActionAny extends AbstractRunAction {

    public ActionAny() {
        super("any");
        setRequiredArgs("actions");
    }

    @Override
    protected void onDoAction(ObjectSingleAction singleAction, Player player) {
        ConfigurationSection chanceSection = singleAction.getSection().getConfigurationSection("actions");
        if (chanceSection == null) {
            return;
        }
        ObjectAction action = new ObjectAction(chanceSection);
        action.runRandomEveryActions(player, singleAction.getInt("amount", 1), singleAction.getActiveArgs());
    }
}
