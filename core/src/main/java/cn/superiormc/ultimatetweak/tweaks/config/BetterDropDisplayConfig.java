package cn.superiormc.ultimatetweak.tweaks.config;

import me.tofaa.entitylib.meta.display.ItemDisplayMeta;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class BetterDropDisplayConfig extends AbstractTweakConfig {

    private volatile List<ModelProfile> modelProfiles;

    private volatile ModelProfile defaultProfile;

    private volatile int settleDelayTicks;

    private volatile float settleEnterMovementThreshold;

    private volatile float settleExitMovementThreshold;

    private volatile int landingDurationTicks;

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
        loadProfiles();
        settleDelayTicks = Math.max(1, getInt("settling.delay-ticks", 10));
        float legacyThreshold = nonNegativeFloat("settling.movement-threshold", 0.03F);
        settleEnterMovementThreshold = nonNegativeFloat(
                "settling.enter-movement-threshold", legacyThreshold);
        settleExitMovementThreshold = nonNegativeFloat(
                "settling.exit-movement-threshold", Math.max(legacyThreshold, 0.05F));
        landingDurationTicks = Math.max(0, getInt("render.landing-duration-ticks",
                Math.max(0, getInt("render.interpolation-duration", 0))));
        labelEnabled = getBoolean("label.enabled", true);
        labelShowAmount = getBoolean("label.show-amount", true);
        labelFormat = getString("label.format", "{name} ×{amount}");
        labelHeight = positiveFloat("label.height", 0.55F);
    }

    private void loadProfiles() {
        ConfigurationSection profilesSection = getConfig().getConfigurationSection("render.models");
        if (profilesSection == null || profilesSection.getKeys(false).isEmpty()) {
            ModelProfile legacy = loadProfile("default", getConfig().getConfigurationSection("render"),
                    null, -1, 0);
            defaultProfile = legacy;
            modelProfiles = Collections.emptyList();
            return;
        }

        List<ModelProfile> loaded = new ArrayList<>();
        ModelProfile fallback = null;
        int order = 0;
        for (String id : profilesSection.getKeys(false)) {
            ConfigurationSection section = profilesSection.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            ConfigurationSection matcher = section.getConfigurationSection("match-item");
            ModelProfile profile = loadProfile(id, section, matcher,
                    section.getInt("priority", 0), order++);
            if ("default".equalsIgnoreCase(id)) {
                fallback = profile;
            } else if (matcher != null) {
                loaded.add(profile);
            }
        }
        if (fallback == null) {
            fallback = loadProfile("default", getConfig().getConfigurationSection("render"),
                    null, -1, order);
        }
        loaded.sort(Comparator.comparingInt(ModelProfile::getPriority).reversed()
                .thenComparingInt(ModelProfile::getOrder));
        defaultProfile = fallback;
        modelProfiles = Collections.unmodifiableList(loaded);
    }

    private ModelProfile loadProfile(String id,
                                     ConfigurationSection section,
                                     ConfigurationSection matcher,
                                     int priority,
                                     int order) {
        String prefix = section == null ? "render" : section.getCurrentPath();
        if (prefix == null || prefix.isEmpty()) {
            prefix = "render";
        }
        ItemDisplayMeta.DisplayType displayType = parseDisplayType(
                getString(prefix + ".transform", getString("render.transform", "fixed")));
        float scaleX = positiveFloat(prefix + ".scale.x", positiveFloat("render.scale.x", 0.65F));
        float scaleY = positiveFloat(prefix + ".scale.y", positiveFloat("render.scale.y", 0.65F));
        float scaleZ = positiveFloat(prefix + ".scale.z", positiveFloat("render.scale.z", 0.65F));
        float translationX = finiteFloat(prefix + ".translation.x", finiteFloat("render.translation.x", 0.0F));
        float translationZ = finiteFloat(prefix + ".translation.z", finiteFloat("render.translation.z", 0.0F));
        float legacyClearance = nonNegativeFloat("render.translation.y", 0.02F);
        float clearance = nonNegativeFloat(prefix + ".clearance",
                nonNegativeFloat(prefix + ".translation.y", legacyClearance));
        float rotationX = finiteFloat(prefix + ".rotation.x", finiteFloat("render.rotation.x", -90.0F));
        float rotationY = finiteFloat(prefix + ".rotation.y", finiteFloat("render.rotation.y", 0.0F));
        float rotationZ = finiteFloat(prefix + ".rotation.z", finiteFloat("render.rotation.z", 0.0F));
        float startRotationX = finiteFloat(prefix + ".landing-start.rotation.x", rotationX);
        float startRotationY = finiteFloat(prefix + ".landing-start.rotation.y", rotationY);
        float startRotationZ = finiteFloat(prefix + ".landing-start.rotation.z", rotationZ);
        float startOffsetY = finiteFloat(prefix + ".landing-start.offset-y", 0.0F);
        float dimensionX = positiveFloat(prefix + ".dimensions.x", 1.0F);
        float dimensionY = positiveFloat(prefix + ".dimensions.y", 0.08F);
        float dimensionZ = positiveFloat(prefix + ".dimensions.z", 1.0F);
        boolean randomYaw = getConfig().getBoolean(prefix + ".random-yaw",
                getBoolean("render.random-yaw", true));
        boolean fullBright = getConfig().getBoolean(prefix + ".full-bright",
                getBoolean("render.full-bright", false));
        float shadowRadius = nonNegativeFloat(prefix + ".shadow.radius",
                nonNegativeFloat("render.shadow.radius", 0.15F));
        float shadowStrength = nonNegativeFloat(prefix + ".shadow.strength",
                nonNegativeFloat("render.shadow.strength", 0.35F));
        float viewRange = positiveFloat(prefix + ".view-range",
                positiveFloat("render.view-range", 1.0F));
        return new ModelProfile(id, priority, order, matcher, displayType,
                scaleX, scaleY, scaleZ, translationX, translationZ, clearance,
                rotationX, rotationY, rotationZ,
                startRotationX, startRotationY, startRotationZ, startOffsetY,
                dimensionX, dimensionY, dimensionZ,
                prefix.startsWith("render.models."), randomYaw, fullBright,
                shadowRadius, shadowStrength, viewRange);
    }

    public List<ModelProfile> getModelProfiles() {
        return modelProfiles;
    }

    public ModelProfile getDefaultProfile() {
        return defaultProfile;
    }

    public int getSettleDelayTicks() {
        return settleDelayTicks;
    }

    public float getSettleEnterMovementThreshold() {
        return settleEnterMovementThreshold;
    }

    public float getSettleExitMovementThreshold() {
        return settleExitMovementThreshold;
    }

    public int getLandingDurationTicks() {
        return landingDurationTicks;
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

    public static final class ModelProfile {

        private final String id;

        private final int priority;

        private final int order;

        private final ConfigurationSection matcher;

        private final ItemDisplayMeta.DisplayType displayType;

        private final float scaleX;

        private final float scaleY;

        private final float scaleZ;

        private final float translationX;

        private final float translationZ;

        private final float clearance;

        private final float rotationX;

        private final float rotationY;

        private final float rotationZ;

        private final float startRotationX;

        private final float startRotationY;

        private final float startRotationZ;

        private final float startOffsetY;

        private final float dimensionX;

        private final float dimensionY;

        private final float dimensionZ;

        private final boolean modelBoundsEnabled;

        private final boolean randomYaw;

        private final boolean fullBright;

        private final float shadowRadius;

        private final float shadowStrength;

        private final float viewRange;

        private ModelProfile(String id, int priority, int order, ConfigurationSection matcher,
                             ItemDisplayMeta.DisplayType displayType,
                             float scaleX, float scaleY, float scaleZ,
                             float translationX, float translationZ, float clearance,
                             float rotationX, float rotationY, float rotationZ,
                             float startRotationX, float startRotationY, float startRotationZ,
                             float startOffsetY,
                             float dimensionX, float dimensionY, float dimensionZ,
                             boolean modelBoundsEnabled,
                             boolean randomYaw, boolean fullBright,
                             float shadowRadius, float shadowStrength, float viewRange) {
            this.id = id;
            this.priority = priority;
            this.order = order;
            this.matcher = matcher;
            this.displayType = displayType;
            this.scaleX = scaleX;
            this.scaleY = scaleY;
            this.scaleZ = scaleZ;
            this.translationX = translationX;
            this.translationZ = translationZ;
            this.clearance = clearance;
            this.rotationX = rotationX;
            this.rotationY = rotationY;
            this.rotationZ = rotationZ;
            this.startRotationX = startRotationX;
            this.startRotationY = startRotationY;
            this.startRotationZ = startRotationZ;
            this.startOffsetY = startOffsetY;
            this.dimensionX = dimensionX;
            this.dimensionY = dimensionY;
            this.dimensionZ = dimensionZ;
            this.modelBoundsEnabled = modelBoundsEnabled;
            this.randomYaw = randomYaw;
            this.fullBright = fullBright;
            this.shadowRadius = shadowRadius;
            this.shadowStrength = shadowStrength;
            this.viewRange = viewRange;
        }

        public String getId() {
            return id;
        }

        public int getPriority() {
            return priority;
        }

        private int getOrder() {
            return order;
        }

        public ConfigurationSection getMatcher() {
            return matcher;
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

        public float getTranslationZ() {
            return translationZ;
        }

        public float getClearance() {
            return clearance;
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

        public float getStartRotationX() {
            return startRotationX;
        }

        public float getStartRotationY() {
            return startRotationY;
        }

        public float getStartRotationZ() {
            return startRotationZ;
        }

        public float getStartOffsetY() {
            return startOffsetY;
        }

        public float getDimensionX() {
            return dimensionX;
        }

        public float getDimensionY() {
            return dimensionY;
        }

        public float getDimensionZ() {
            return dimensionZ;
        }

        public boolean isModelBoundsEnabled() {
            return modelBoundsEnabled;
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
    }
}
