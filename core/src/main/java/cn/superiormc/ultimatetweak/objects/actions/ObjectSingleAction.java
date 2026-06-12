package cn.superiormc.ultimatetweak.objects.actions;


import cn.superiormc.ultimatetweak.managers.ActionManager;
import cn.superiormc.ultimatetweak.objects.AbstractSingleRun;
import cn.superiormc.ultimatetweak.objects.ObjectAction;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class ObjectSingleAction extends AbstractSingleRun {

    private final ObjectAction action;

    public ObjectSingleAction(ObjectAction action, ConfigurationSection actionSection) {
        super(actionSection);
        this.action = action;
    }

    public void doAction(Player player) {
        ActionManager.actionManager.doAction(this, player);
    }

    public ObjectAction getAction() {
        return action;
    }

}
