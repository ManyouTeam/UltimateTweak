package cn.superiormc.ultimatetweak.tweaks.betterdropdisplay;

import cn.superiormc.ultimatetweak.tweaks.config.BetterDropDisplayConfig.ModelProfile;
import com.github.retrooper.packetevents.util.Quaternion4f;
import org.joml.Quaternionf;

final class DropDisplayPose {

    private DropDisplayPose() {
    }

    static Quaternionf targetRotation(ModelProfile profile, float yawRadians) {
        return rotation(profile.getRotationX(), profile.getRotationY(), profile.getRotationZ(), yawRadians);
    }

    static Quaternionf startRotation(ModelProfile profile, float yawRadians) {
        return rotation(profile.getStartRotationX(), profile.getStartRotationY(),
                profile.getStartRotationZ(), yawRadians);
    }

    static Quaternion4f packetRotation(Quaternionf rotation) {
        return new Quaternion4f(rotation.x, rotation.y, rotation.z, rotation.w);
    }

    static float verticalHalfExtent(ModelProfile profile, Quaternionf rotation) {
        float x = rotation.x();
        float y = rotation.y();
        float z = rotation.z();
        float w = rotation.w();
        float rowX = 2.0F * (x * y + z * w);
        float rowY = 1.0F - 2.0F * (x * x + z * z);
        float rowZ = 2.0F * (y * z - x * w);
        return Math.abs(rowX) * profile.getDimensionX() * profile.getScaleX() * 0.5F
                + Math.abs(rowY) * profile.getDimensionY() * profile.getScaleY() * 0.5F
                + Math.abs(rowZ) * profile.getDimensionZ() * profile.getScaleZ() * 0.5F;
    }

    private static Quaternionf rotation(float xDegrees,
                                        float yDegrees,
                                        float zDegrees,
                                        float yawRadians) {
        return new Quaternionf()
                .rotateY((float) Math.toRadians(yDegrees) + yawRadians)
                .rotateX((float) Math.toRadians(xDegrees))
                .rotateZ((float) Math.toRadians(zDegrees));
    }
}
