package cn.superiormc.ultimatetweak.packets.entity.meta.display;

import cn.superiormc.ultimatetweak.packets.entity.meta.EntityMeta;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.util.Quaternion4f;
import com.github.retrooper.packetevents.util.Vector3f;

public class AbstractDisplayMeta extends EntityMeta {

    public static final int OFFSET = 8;

    public static final int MAX_OFFSET = 23;

    public AbstractDisplayMeta(int entityId) {
        super(entityId);
    }

    public void setInterpolationDelay(int value) {
        set(OFFSET, EntityDataTypes.INT, value);
    }

    public void setTransformationInterpolationDuration(int value) {
        set(OFFSET + 1, EntityDataTypes.INT, value);
    }

    public void setPositionRotationInterpolationDuration(int value) {
        set(OFFSET + 2, EntityDataTypes.INT, value);
    }

    public void setTranslation(Vector3f value) {
        set(OFFSET + 3, EntityDataTypes.VECTOR3F, value);
    }

    public void setScale(Vector3f value) {
        set(OFFSET + 4, EntityDataTypes.VECTOR3F, value);
    }

    public void setLeftRotation(Quaternion4f value) {
        set(OFFSET + 5, EntityDataTypes.QUATERNION, value);
    }

    public void setRightRotation(Quaternion4f value) {
        set(OFFSET + 6, EntityDataTypes.QUATERNION, value);
    }

    public void setBillboardConstraints(BillboardConstraints value) {
        set(OFFSET + 7, EntityDataTypes.BYTE, (byte) value.ordinal());
    }

    public void setBrightnessOverride(int value) {
        set(OFFSET + 8, EntityDataTypes.INT, value);
    }

    public void setViewRange(float value) {
        set(OFFSET + 9, EntityDataTypes.FLOAT, value);
    }

    public void setShadowRadius(float value) {
        set(OFFSET + 10, EntityDataTypes.FLOAT, value);
    }

    public void setShadowStrength(float value) {
        set(OFFSET + 11, EntityDataTypes.FLOAT, value);
    }

    public void setWidth(float value) {
        set(OFFSET + 12, EntityDataTypes.FLOAT, value);
    }

    public void setHeight(float value) {
        set(OFFSET + 13, EntityDataTypes.FLOAT, value);
    }

    public void setGlowColorOverride(int value) {
        set(OFFSET + 14, EntityDataTypes.INT, value);
    }

    public enum BillboardConstraints {
        FIXED,
        VERTICAL,
        HORIZONTAL,
        CENTER
    }
}
