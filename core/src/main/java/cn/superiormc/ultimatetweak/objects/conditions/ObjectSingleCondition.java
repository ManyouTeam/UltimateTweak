package cn.superiormc.ultimatetweak.objects.conditions;

import cn.superiormc.ultimatetweak.managers.ConditionManager;
import cn.superiormc.ultimatetweak.objects.AbstractSingleRun;
import cn.superiormc.ultimatetweak.objects.ObjectCondition;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class ObjectSingleCondition extends AbstractSingleRun {

    private final ObjectCondition condition;

    public ObjectSingleCondition(ObjectCondition condition, ConfigurationSection conditionSection) {
        super(conditionSection);
        this.condition = condition;
    }

    public boolean checkBoolean(Player player) {
        return ConditionManager.conditionManager.checkBoolean(this, player);
    }

    public ObjectCondition getCondition() {
        return condition;
    }

}
