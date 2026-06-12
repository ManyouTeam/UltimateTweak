package cn.superiormc.ultimatetweak.tweaks.config;

import cn.superiormc.ultimatetweak.objects.ObjectAction;
import cn.superiormc.ultimatetweak.objects.ObjectCondition;

import java.io.File;

public class BestToolConfig extends AbstractTweakConfig {

    private ObjectCondition conditions;

    private ObjectAction switchActions;

    public BestToolConfig(File file) {
        super("BestTool", file);
    }

    @Override
    public void reload() {
        super.reload();
        conditions = new ObjectCondition(getSection("conditions"));
        switchActions = new ObjectAction(getSection("switch-actions"));
    }

    public ObjectCondition getConditions() {
        return conditions;
    }

    public ObjectAction getSwitchActions() {
        return switchActions;
    }

    public double getFortuneMultiplierPerLevel() {
        return Math.max(1.0, getDouble("fortune-multiplier-per-level", 1.5));
    }

    public boolean shouldSearchEntireInventory() {
        return "inventory".equalsIgnoreCase(getString("search-scope", "hotbar"));
    }

    public int getCooldownTicks() {
        return Math.max(0, getInt("cooldown-tick", 5));
    }
}
