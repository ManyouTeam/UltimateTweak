package cn.superiormc.ultimatetweak.tweaks.betterdropdisplay;

import cn.superiormc.ultimatetweak.tweaks.config.BetterDropDisplayConfig;
import cn.superiormc.ultimatetweak.tweaks.config.BetterDropDisplayConfig.ModelProfile;
import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.Vector3f;
import me.tofaa.entitylib.meta.EntityMeta;
import me.tofaa.entitylib.meta.display.AbstractDisplayMeta;
import me.tofaa.entitylib.meta.display.ItemDisplayMeta;
import me.tofaa.entitylib.meta.projectile.ItemEntityMeta;
import net.kyori.adventure.text.Component;

final class DropDisplayMetadataFactory {

    private static final int FULL_BRIGHTNESS = 0xF000F0;

    private DropDisplayMetadataFactory() {
    }

    static ItemDisplayMeta create(int entityId,
                                  ItemStack itemStack,
                                  float yawRadians,
                                  float groundTranslationY,
                                  BetterDropDisplayConfig config,
                                  ModelProfile profile,
                                  boolean landingStart,
                                  int interpolationDuration) {
        ItemDisplayMeta meta = (ItemDisplayMeta) EntityMeta.createMeta(entityId, EntityTypes.ITEM_DISPLAY);
        meta.setItem(itemStack);
        meta.setDisplayType(profile.getDisplayType());
        meta.setBillboardConstraints(AbstractDisplayMeta.BillboardConstraints.FIXED);
        meta.setScale(new Vector3f(profile.getScaleX(), profile.getScaleY(), profile.getScaleZ()));
        meta.setTranslation(new Vector3f(
                profile.getTranslationX(),
                groundTranslationY + (landingStart ? profile.getStartOffsetY() : 0.0F),
                profile.getTranslationZ()));
        meta.setLeftRotation(DropDisplayPose.packetRotation(landingStart
                ? DropDisplayPose.startRotation(profile, yawRadians)
                : DropDisplayPose.targetRotation(profile, yawRadians)));
        meta.setBrightnessOverride(profile.isFullBright() ? FULL_BRIGHTNESS : -1);
        meta.setShadowRadius(profile.getShadowRadius());
        meta.setShadowStrength(profile.getShadowStrength());
        meta.setViewRange(profile.getViewRange());
        meta.setInterpolationDelay(0);
        meta.setTransformationInterpolationDuration(interpolationDuration);
        meta.setPositionRotationInterpolationDuration(interpolationDuration);
        if (config.isLabelEnabled()) {
            meta.setCustomName(createLabel(itemStack, config));
            meta.setCustomNameVisible(true);
            meta.setHeight(config.getLabelHeight());
            meta.setWidth(Math.max(profile.getScaleX(), profile.getScaleZ()));
        }
        return meta;
    }

    static ItemEntityMeta createItem(int entityId, ItemStack itemStack) {
        ItemEntityMeta meta = (ItemEntityMeta) EntityMeta.createMeta(entityId, EntityTypes.ITEM);
        meta.setItem(itemStack);
        return meta;
    }

    private static Component createLabel(ItemStack itemStack, BetterDropDisplayConfig config) {
        Component itemName = itemStack.getComponent(ComponentTypes.CUSTOM_NAME)
                .orElseGet(() -> itemStack.getComponent(ComponentTypes.ITEM_NAME)
                        .orElseGet(() -> defaultItemName(itemStack)));
        if (!config.isLabelShowAmount()) {
            return itemName;
        }

        String format = config.getLabelFormat().replace("{amount}", Integer.toString(itemStack.getAmount()));
        Component result = Component.empty();
        int from = 0;
        int marker;
        while ((marker = format.indexOf("{name}", from)) >= 0) {
            result = result.append(Component.text(format.substring(from, marker))).append(itemName);
            from = marker + "{name}".length();
        }
        return result.append(Component.text(format.substring(from)));
    }

    private static Component defaultItemName(ItemStack itemStack) {
        ResourceLocation key = itemStack.getType().getName();
        StateType placedType = itemStack.getType().getPlacedType();
        String category = placedType != null && !placedType.isAir() ? "block." : "item.";
        return Component.translatable(category + key.getNamespace() + "." + key.getKey());
    }
}
