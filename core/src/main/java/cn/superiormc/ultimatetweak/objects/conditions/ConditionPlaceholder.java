package cn.superiormc.ultimatetweak.objects.conditions;


import cn.superiormc.ultimatetweak.managers.ErrorManager;
import cn.superiormc.ultimatetweak.utils.MathUtil;
import org.bukkit.entity.Player;

public class ConditionPlaceholder extends AbstractCheckCondition {

    public ConditionPlaceholder() {
        super("placeholder");
        setRequiredArgs("placeholder", "rule", "value");
    }

    @Override
    protected boolean onCheckCondition(ObjectSingleCondition singleCondition, Player player) {
        String placeholder = singleCondition.getString("placeholder", player);
        if (placeholder.isEmpty()) {
            return true;
        }
        String value = singleCondition.getString("value", player);
        try {
            switch (singleCondition.getString("rule")) {
                case ">=":
                    return MathUtil.doCalculate(placeholder) >= MathUtil.doCalculate(value);
                case ">":
                    return MathUtil.doCalculate(placeholder) > MathUtil.doCalculate(value);
                case "=":
                    return MathUtil.doCalculate(placeholder) == MathUtil.doCalculate(value);
                case "<":
                    return MathUtil.doCalculate(placeholder) < MathUtil.doCalculate(value);
                case "<=":
                    return MathUtil.doCalculate(placeholder) <= MathUtil.doCalculate(value);
                case "==":
                    return placeholder.equals(value);
                case "!=":
                    return !placeholder.equals(value);
                case "*=":
                    return placeholder.contains(value);
                case "=*":
                    return value.contains(placeholder);
                case "!*=":
                    return !placeholder.contains(value);
                case "!=*":
                    return !value.contains(placeholder);
                default:
                    ErrorManager.errorManager.sendErrorMessage("§cError: Your placeholder condition can not being correctly load.");
                    return true;
            }
        } catch (Throwable throwable) {
            ErrorManager.errorManager.sendErrorMessage("§cError: Your placeholder condition can not being correctly load.");
            return true;
        }
    }
}
