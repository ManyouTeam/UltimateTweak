package cn.superiormc.ultimatetweak.hooks.hitbox;

import kr.toxicity.model.api.BetterModel;
import kr.toxicity.model.api.nms.HitBox;
import kr.toxicity.model.api.tracker.EntityTrackerRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;

public final class HitboxBetterModelHook extends AbstractHitboxHook {

    public HitboxBetterModelHook() {
        super("BetterModel");
    }

    @Override
    public HitboxResult resolve(Entity entity) {
        Entity owner = entity;
        if (entity instanceof HitBox hitbox) {
            owner = Bukkit.getEntity(hitbox.source().uuid());
            if (owner == null) {
                return null;
            }
        }
        EntityTrackerRegistry registry = BetterModel.registryOrNull(owner.getUniqueId());
        if (registry == null || registry.isClosed()) {
            return null;
        }
        BoundingBox bounds = null;
        for (HitBox hitbox : registry.hitBoxes()) {
            Entity collisionEntity = Bukkit.getEntity(hitbox.uuid());
            if (collisionEntity == null || !collisionEntity.isValid()
                    || !collisionEntity.getWorld().equals(owner.getWorld())) {
                continue;
            }
            if (bounds == null) {
                bounds = collisionEntity.getBoundingBox().clone();
            } else {
                bounds.union(collisionEntity.getBoundingBox());
            }
        }
        return new HitboxResult(owner, bounds == null ? owner.getBoundingBox() : bounds);
    }
}
