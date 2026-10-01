package cn.superiormc.ultimatetweak.hooks.hitbox;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;

import java.util.Objects;

public final class HitboxResult {

    private final Entity owner;

    private final BoundingBox bounds;

    public HitboxResult(Entity owner, BoundingBox bounds) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.bounds = Objects.requireNonNull(bounds, "bounds").clone();
    }

    public Entity getOwner() {
        return owner;
    }

    public BoundingBox getBounds() {
        return bounds.clone();
    }

    public Location getTopCenter() {
        return new Location(owner.getWorld(), bounds.getCenterX(), bounds.getMaxY(), bounds.getCenterZ());
    }
}
