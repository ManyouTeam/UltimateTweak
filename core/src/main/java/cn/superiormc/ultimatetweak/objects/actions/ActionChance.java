package cn.superiormc.ultimatetweak.objects.actions;


import cn.superiormc.ultimatetweak.objects.ObjectAction;
import org.apache.commons.lang3.RandomUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class ActionChance extends AbstractRunAction {

    public ActionChance() {
        super("chance");
        setRequiredArgs("rate", "actions");
    }

    @Override
    protected void onDoAction(ObjectSingleAction singleAction, Player player) {
        ConfigurationSection chanceSection = singleAction.getSection().getConfigurationSection("actions");
        if (chanceSection == null) {
            return;
        }
        double rate = singleAction.getDouble("rate", player);
        if (RandomUtils.nextDouble(0, 100) <= rate) {
            ObjectAction action = new ObjectAction(chanceSection);
            action.runAllActions(player);
        }
    }
}
