package cn.superiormc.ultimatetweak.tweaks.config;

import cn.superiormc.ultimatetweak.objects.ObjectCondition;

import java.io.File;

public class DoubleDoorConfig extends AbstractTweakConfig {

    private ObjectCondition conditions;

    public DoubleDoorConfig(File file) {
        super("DoubleDoor", file);
    }

    @Override
    public void reload() {
        super.reload();
        conditions = new ObjectCondition(getSection("conditions"));
    }

    public ObjectCondition getConditions() {
        return conditions;
    }

    public boolean isAnimationEnabled() {
        return getBoolean("animation.enabled", true);
    }

    public boolean shouldAnimateSingleDoor() {
        return getBoolean("animation.single-door", false);
    }

    public int getAnimationDurationTicks() {
        return Math.max(1, getInt("animation.duration-ticks", 16));
    }

    public int getAnimationIntervalTicks() {
        return Math.max(1, getInt("animation.interval-ticks", 1));
    }

    public int getAnimationViewDistance() {
        return Math.max(1, getInt("animation.view-distance", 24));
    }
}
