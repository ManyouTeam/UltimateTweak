package cn.superiormc.ultimatetweak.hooks.hitbox;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.Hitbox;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.nms.entity.HitboxEntity;
import com.ticxo.modelengine.api.model.bone.ModelBone;
import com.ticxo.modelengine.api.model.bone.type.SubHitbox;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;
import org.joml.Vector3fc;

public final class HitboxModelEngineHook extends AbstractHitboxHook {

    public HitboxModelEngineHook() {
        super("ModelEngine");
    }

    @Override
    public HitboxResult resolve(Entity entity) {
        ModeledEntity modeled = ModelEngineAPI.getModeledEntity(entity);
        if (modeled == null) {
            HitboxEntity hitbox = ModelEngineAPI.getInteractionTracker().getHitbox(entity.getUniqueId());
            if (hitbox != null) {
                modeled = hitbox.getBone().getActiveModel().getModeledEntity();
            } else {
                ActiveModel relay = ModelEngineAPI.getInteractionTracker().getModelRelay(entity.getEntityId());
                if (relay != null) {
                    modeled = relay.getModeledEntity();
                }
            }
        }
        if (modeled == null || modeled.isDestroyed()
                || !(modeled.getBase().getOriginal() instanceof Entity owner)) {
            return null;
        }
        BoundingBox bounds = modeled.getBase().getBoundingBox().clone();
        Location location = owner.getLocation();
        for (ActiveModel model : modeled.getModels().values()) {
            for (ModelBone bone : model.getBones().values()) {
                bone.getImmutableBoneBehaviors().values().forEach(behavior -> {
                    if (behavior instanceof SubHitbox sub && sub.getHitboxEntity() != null
                            && sub.getHitboxEntity().isValid()) {
                        Entity part = Bukkit.getEntity(sub.getHitboxEntity().getUniqueId());
                        if (part != null && part.getWorld().equals(owner.getWorld())) {
                            bounds.union(part.getBoundingBox());
                        }
                    }
                });
            }
            Hitbox hitbox = model.getBlueprint().getMainHitbox();
            if (hitbox == null) {
                continue;
            }
            Vector3fc scale = model.getHitboxScale();
            double halfWidth = Math.abs(hitbox.getWidth() * scale.x()) / 2.0D;
            double halfDepth = Math.abs(hitbox.getDepth() * scale.z()) / 2.0D;
            double height = Math.abs(hitbox.getHeight() * scale.y());
            bounds.union(new BoundingBox(location.getX() - halfWidth, location.getY(),
                    location.getZ() - halfDepth, location.getX() + halfWidth,
                    location.getY() + height, location.getZ() + halfDepth));
        }
        return new HitboxResult(owner, bounds);
    }
}
