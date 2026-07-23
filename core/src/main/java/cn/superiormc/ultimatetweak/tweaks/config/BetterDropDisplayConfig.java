package cn.superiormc.ultimatetweak.tweaks.config;

import me.tofaa.entitylib.meta.display.ItemDisplayMeta;

import java.io.File;
import java.util.Locale;

public class BetterDropDisplayConfig extends AbstractTweakConfig {

    private volatile ItemDisplayMeta.DisplayType displayType;

    private volatile float scaleX;

    private volatile float scaleY;

    private volatile float scaleZ;

    private volatile float translationX;

    private volatile float translationY;

    private volatile float translationZ;

    private volatile float rotationX;

    private volatile float rotationY;

    private volatile float rotationZ;

    private volatile boolean randomYaw;

    private volatile boolean fullBright;

    private volatile float shadowRadius;

    private volatile float shadowStrength;

    private volatile float viewRange;

    private volatile int interpolationDuration;

    private volatile int settleDelayTicks;

    private volatile float settleMovementThreshold;

    private volatile boolean labelEnabled;

    private volatile boolean labelShowAmount;

    private volatile String labelFormat;

    private volatile float labelHeight;

    public BetterDropDisplayConfig(File file) {
        super("BetterDropDisplay", file);
    }

    @Override
    public void reload() {
        super.reload();
        displayType = parseDisplayType(getString("render.transform", "fixed"));
        scaleX = positiveFloat("render.scale.x", 0.65F);
        scaleY = positiveFloat("render.scale.y", 0.65F);
        scaleZ = positiveFloat("render.scale.z", 0.65F);
        translationX = finiteFloat("render.translation.x", 0.0F);
        // This is clearance above the collision surface; the surface correction is calculated at runtime.
        translationY = nonNegativeFloat("render.translation.y", 0.16F);
        translationZ = finiteFloat("render.translation.z", 0.0F);
        rotationX = finiteFloat("render.rotation.x", -90.0F);
        rotationY = finiteFloat("render.rotation.y", 0.0F);
        rotationZ = finiteFloat("render.rotation.z", 0.0F);
        randomYaw = getBoolean("render.random-yaw", true);
        fullBright = getBoolean("render.full-bright", false);
        shadowRadius = nonNegativeFloat("render.shadow.radius", 0.15F);
        shadowStrength = nonNegativeFloat("render.shadow.strength", 0.35F);
        viewRange = positiveFloat("render.view-range", 1.0F);
        interpolationDuration = Math.max(0, getInt("render.interpolation-duration", 0));
        settleDelayTicks = Math.max(1, getInt("settling.delay-ticks", 10));
        settleMovementThreshold = nonNegativeFloat("settling.movement-threshold", 0.03F);
        labelEnabled = getBoolean("label.enabled", true);
        labelShowAmount = getBoolean("label.show-amount", true);
        labelFormat = getString("label.format", "{name} ×{amount}");
        labelHeight = positiveFloat("label.height", 0.55F);
    }

    public ItemDisplayMeta.DisplayType getDisplayType() {
        return displayType;
    }

    public float getScaleX() {
        return scaleX;
    }

    public float getScaleY() {
        return scaleY;
    }

    public float getScaleZ() {
        return scaleZ;
    }

    public float getTranslationX() {
        return translationX;
    }

    public float getTranslationY() {
        return translationY;
    }

    public float getTranslationZ() {
        return translationZ;
    }

    public float getRotationX() {
        return rotationX;
    }

    public float getRotationY() {
        return rotationY;
    }

    public float getRotationZ() {
        return rotationZ;
    }

    public boolean isRandomYaw() {
        return randomYaw;
    }

    public boolean isFullBright() {
        return fullBright;
    }

    public float getShadowRadius() {
        return shadowRadius;
    }

    public float getShadowStrength() {
        return shadowStrength;
    }

    public float getViewRange() {
        return viewRange;
    }

    public int getInterpolationDuration() {
        return interpolationDuration;
    }

    public int getSettleDelayTicks() {
        return settleDelayTicks;
    }

    public float getSettleMovementThreshold() {
        return settleMovementThreshold;
    }

    public boolean isLabelEnabled() {
        return labelEnabled;
    }

    public boolean isLabelShowAmount() {
        return labelShowAmount;
    }

    public String getLabelFormat() {
        return labelFormat;
    }

    public float getLabelHeight() {
        return labelHeight;
    }

    private ItemDisplayMeta.DisplayType parseDisplayType(String value) {
        try {
            return ItemDisplayMeta.DisplayType.valueOf(value.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException exception) {
            return ItemDisplayMeta.DisplayType.GROUND;
        }
    }

    private float positiveFloat(String path, float defaultValue) {
        float value = finiteFloat(path, defaultValue);
        return value > 0.0F ? value : defaultValue;
    }

    private float nonNegativeFloat(String path, float defaultValue) {
        return Math.max(0.0F, finiteFloat(path, defaultValue));
    }

    private float finiteFloat(String path, float defaultValue) {
        double value = getDouble(path, defaultValue);
        return Double.isFinite(value) ? (float) value : defaultValue;
    }
}
