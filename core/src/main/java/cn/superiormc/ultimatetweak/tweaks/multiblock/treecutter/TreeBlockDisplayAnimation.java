package cn.superiormc.ultimatetweak.tweaks.multiblock.treecutter;

import cn.superiormc.ultimatetweak.utils.SchedulerUtil;
import cn.superiormc.ultimatetweak.tweaks.display.AbstractBlockDisplayEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class TreeBlockDisplayAnimation {

    private static final float MAX_DEGREES = 92.0F;

    private static final DamageSource FALLING_TREE_DAMAGE_SOURCE =
            DamageSource.builder(DamageType.FALLING_BLOCK).build();

    private TreeBlockDisplayAnimation() {
    }

    public static AnimationSession create(World world,
                                          Block pivotBlock,
                                          List<AnimationBlock> blocks,
                                          int durationTicks,
                                          int intervalTicks,
                                          boolean glow,
                                          int glowColor,
                                          int viewDistance,
                                          String directionMode,
                                          Vector playerDirection,
                                          FallDamageOptions fallDamageOptions) {
        if (world == null || pivotBlock == null || blocks == null || blocks.isEmpty()) {
            return new AnimationSession(world, pivotBlock, Collections.emptyList(),
                    new Vector(0.0, 0.0, -1.0), new org.joml.Vector3f(), Math.max(1, durationTicks),
                    Math.max(1, intervalTicks), glow, glowColor, Math.max(1, viewDistance),
                    FallDamageOptions.disabled());
        }

        Vector direction = getDirection(directionMode, playerDirection);
        Vector pivotVector = pivotBlock.getLocation().toVector().add(getPivotOffset(direction));
        org.joml.Vector3f pivot = new org.joml.Vector3f(
                (float) pivotVector.getX(),
                (float) pivotVector.getY(),
                (float) pivotVector.getZ());
        Quaternionf finalRotation = getRotation(direction, (float) Math.toRadians(MAX_DEGREES));

        for (AnimationBlock block : blocks) {
            block.setDropLocation(getCalculatedDropLocation(world, block, pivot, finalRotation));
        }

        return new AnimationSession(world, pivotBlock, blocks, direction, pivot,
                Math.max(1, durationTicks), Math.max(1, intervalTicks), glow, glowColor,
                Math.max(1, viewDistance), fallDamageOptions);
    }

    private static Location getCalculatedDropLocation(World world,
                                                      AnimationBlock block,
                                                      org.joml.Vector3f pivot,
                                                      Quaternionf rotation) {
        org.joml.Vector3f originalCorner = new org.joml.Vector3f(block.getX(), block.getY(), block.getZ());
        org.joml.Vector3f localCorner = new org.joml.Vector3f(originalCorner).sub(pivot);
        rotation.transform(localCorner);

        org.joml.Vector3f rotatedCorner = new org.joml.Vector3f(localCorner).add(pivot);
        org.joml.Vector3f centerOffset = new org.joml.Vector3f(0.5F, 0.5F, 0.5F);
        rotation.transform(centerOffset);

        org.joml.Vector3f rotatedCenter = rotatedCorner.add(centerOffset);
        return new Location(world, rotatedCenter.x, rotatedCenter.y, rotatedCenter.z);
    }

    private static org.joml.Vector3f getRotatedPosition(AbstractBlockDisplayEffect.DisplayBlock block,
                                                        org.joml.Vector3f pivot,
                                                        Quaternionf rotation) {
        org.joml.Vector3f originalCorner = new org.joml.Vector3f(block.x(), block.y(), block.z());
        org.joml.Vector3f localCorner = new org.joml.Vector3f(originalCorner).sub(pivot);
        rotation.transform(localCorner);
        return new org.joml.Vector3f(localCorner).add(pivot);
    }

    private static org.joml.Vector3f getRotatedCenter(AbstractBlockDisplayEffect.DisplayBlock block,
                                                      org.joml.Vector3f pivot,
                                                      Quaternionf rotation) {
        org.joml.Vector3f center = getRotatedPosition(block, pivot, rotation);
        org.joml.Vector3f centerOffset = new org.joml.Vector3f(0.5F, 0.5F, 0.5F);
        rotation.transform(centerOffset);
        return center.add(centerOffset);
    }

    private static Quaternionf getRotation(Vector direction, float radians) {
        return new Quaternionf().rotateAxis(radians,
                (float) direction.getZ(), 0.0F, (float) -direction.getX());
    }

    private static Vector getPivotOffset(Vector direction) {
        return new Vector(0.5 + direction.getX() * 0.5, 0.0, 0.5 + direction.getZ() * 0.5);
    }

    private static Vector getDirection(String mode, Vector playerDirection) {
        Vector horizontalPlayerDirection = getHorizontalDirection(playerDirection);
        return switch (mode == null ? "random" : mode) {
            case "same-as-player", "same", "player", "player-direction" -> horizontalPlayerDirection.multiply(-1.0);
            case "away-from-player", "away", "opposite", "opposite-player-direction" -> horizontalPlayerDirection;
            default -> randomDirection();
        };
    }

    private static Vector getHorizontalDirection(Vector direction) {
        if (direction == null) {
            return randomDirection();
        }
        Vector horizontal = new Vector(direction.getX(), 0.0, direction.getZ());
        if (horizontal.lengthSquared() == 0.0) {
            return randomDirection();
        }
        return horizontal.normalize();
    }

    private static Vector randomDirection() {
        double angle = ThreadLocalRandom.current().nextDouble(Math.PI * 2.0);
        return new Vector(Math.cos(angle), 0.0, Math.sin(angle));
    }

    public static final class AnimationSession extends AbstractBlockDisplayEffect {

        private final Block pivotBlock;

        private final List<AnimationBlock> animationBlocks;

        private final Vector direction;

        private final org.joml.Vector3f pivot;

        private final int durationTicks;

        private final int intervalTicks;

        private final FallDamageOptions fallDamageOptions;

        private final double searchRadius;

        private final Set<UUID> damagedEntities = new HashSet<>();

        private int lastDamageCheckTicks = -1_000_000;

        private AnimationSession(World world,
                                 Block pivotBlock,
                                 List<AnimationBlock> blocks,
                                 Vector direction,
                                 org.joml.Vector3f pivot,
                                 int durationTicks,
                                 int intervalTicks,
                                 boolean glow,
                                 int glowColor,
                                 int viewDistance,
                                 FallDamageOptions fallDamageOptions) {
            super(world,
                    pivotBlock == null ? getFallbackCenter(world, blocks) : pivotBlock.getLocation(),
                    toDisplayBlocks(blocks),
                    viewDistance,
                    DisplayOptions.animated(glow, glowColor, intervalTicks));
            this.pivotBlock = pivotBlock;
            this.animationBlocks = blocks;
            this.direction = direction;
            this.pivot = pivot;
            this.durationTicks = durationTicks;
            this.intervalTicks = intervalTicks;
            this.fallDamageOptions = fallDamageOptions == null ? FallDamageOptions.disabled() : fallDamageOptions;
            this.searchRadius = calculateSearchRadius();
        }

        public void play(Runnable onComplete) {
            if (!hasBlocks()) {
                if (onComplete != null) {
                    onComplete.run();
                }
                return;
            }

            if (!spawnDisplays()) {
                if (onComplete != null) {
                    onComplete.run();
                }
                return;
            }
            SchedulerUtil.runTaskAsynchronously(() -> {
                List<AnimationFrame> frames = calculateFrames();
                SchedulerUtil.runSync(center(), () -> startAnimation(frames, onComplete));
            });
        }

        private List<AnimationFrame> calculateFrames() {
            List<AnimationFrame> frames = new ArrayList<>();
            for (int elapsedTicks = intervalTicks; elapsedTicks <= durationTicks; elapsedTicks += intervalTicks) {
                frames.add(calculateFrame(Math.min(elapsedTicks, durationTicks)));
            }
            if (frames.isEmpty() || frames.get(frames.size() - 1).elapsedTicks() < durationTicks) {
                frames.add(calculateFrame(durationTicks));
            }
            return frames;
        }

        private AnimationFrame calculateFrame(int elapsedTicks) {
            double progress = Math.min((double) elapsedTicks / durationTicks, 1.0);
            double eased = progress * progress * progress
                    * (progress * (progress * 6.0 - 15.0) + 10.0);
            float angle = (float) (MAX_DEGREES * eased);
            Quaternionf rotation = getRotation(direction, (float) Math.toRadians(angle));
            List<org.joml.Vector3f> translations = new ArrayList<>(blocks().size());
            List<org.joml.Vector3f> centers = new ArrayList<>(blocks().size());
            for (DisplayBlock block : blocks()) {
                translations.add(getDisplayTranslation(block, rotation));
                centers.add(getRotatedCenter(block, pivot, rotation));
            }
            return new AnimationFrame(elapsedTicks, angle, rotation, translations, centers);
        }

        private void startAnimation(List<AnimationFrame> frames, Runnable onComplete) {
            if (frames.isEmpty()) {
                complete(onComplete);
                return;
            }

            final int[] frameIndex = {0};
            final SchedulerUtil[] task = new SchedulerUtil[1];
            task[0] = SchedulerUtil.runTaskTimer(() -> {
                AnimationFrame frame = frames.get(frameIndex[0]++);
                updateDisplays(frame.rotation(), frame.translations());
                applyFallDamage(frame);

                if (frameIndex[0] >= frames.size()) {
                    if (task[0] != null) {
                        task[0].cancel();
                    }
                    SchedulerUtil.runTaskLater(() -> complete(onComplete), intervalTicks);
                }
            }, 1L, intervalTicks);
        }

        private void complete(Runnable onComplete) {
            destroyDisplays();
            playLandingEffects();
            if (onComplete != null) {
                onComplete.run();
            }
        }

        private org.joml.Vector3f getDisplayTranslation(DisplayBlock block, Quaternionf rotation) {
            org.joml.Vector3f rotatedPosition = getRotatedPosition(block, pivot, rotation);
            return rotatedPosition
                    .sub(block.x(), block.y(), block.z())
                    .add(getRotatedBaseTranslation(rotation));
        }

        private void applyFallDamage(AnimationFrame frame) {
            if (!fallDamageOptions.enabled()
                    || fallDamageOptions.damage() <= 0.0
                    || frame.angle() < fallDamageOptions.minAngle()
                    || frame.elapsedTicks() - lastDamageCheckTicks < fallDamageOptions.checkIntervalTicks()) {
                return;
            }
            lastDamageCheckTicks = frame.elapsedTicks();

            for (LivingEntity entity : world().getNearbyLivingEntities(
                    new Location(world(), pivot.x, pivot.y, pivot.z),
                    searchRadius, searchRadius, searchRadius)) {
                if (damagedEntities.contains(entity.getUniqueId())
                        || entity.isDead()
                        || entity instanceof Player && !fallDamageOptions.damagePlayers()
                        || !(entity instanceof Player) && !fallDamageOptions.damageEntities()
                        || !isHitByTree(entity, frame.centers())) {
                    continue;
                }
                damagedEntities.add(entity.getUniqueId());
                entity.damage(fallDamageOptions.damage(), FALLING_TREE_DAMAGE_SOURCE);
            }
        }

        private boolean isHitByTree(LivingEntity entity, List<org.joml.Vector3f> centers) {
            BoundingBox hitBox = entity.getBoundingBox().expand(fallDamageOptions.hitRadius());
            for (org.joml.Vector3f position : centers) {
                if (hitBox.contains(position.x, position.y, position.z)) {
                    return true;
                }
            }
            return false;
        }

        private double calculateSearchRadius() {
            double radiusSquared = 1.0;
            for (DisplayBlock block : blocks()) {
                double dx = block.x() + 0.5 - pivot.x;
                double dy = block.y() + 0.5 - pivot.y;
                double dz = block.z() + 0.5 - pivot.z;
                radiusSquared = Math.max(radiusSquared, dx * dx + dy * dy + dz * dz);
            }
            return Math.sqrt(radiusSquared) + fallDamageOptions.hitRadius() + 2.0;
        }

        private void playLandingEffects() {
            for (AnimationBlock block : animationBlocks) {
                Location location = block.getDropLocation();
                if (location == null || location.getWorld() == null) {
                    continue;
                }
                location.getWorld().spawnParticle(Particle.BLOCK, location, 5,
                        0.35, 0.35, 0.35, block.getBlockData());
            }
            if (pivotBlock != null) {
                pivotBlock.getWorld().playSound(pivotBlock.getLocation(),
                        "block.wood.break", 1.0F, 0.7F);
            }
        }

        private static Location getFallbackCenter(World world, List<AnimationBlock> blocks) {
            if (world == null || blocks == null || blocks.isEmpty()) {
                return null;
            }
            AnimationBlock block = blocks.get(0);
            return new Location(world, block.getX(), block.getY(), block.getZ());
        }

        private static List<AbstractBlockDisplayEffect.DisplayBlock> toDisplayBlocks(List<AnimationBlock> blocks) {
            if (blocks == null || blocks.isEmpty()) {
                return Collections.emptyList();
            }
            List<AbstractBlockDisplayEffect.DisplayBlock> result = new ArrayList<>(blocks.size());
            for (AnimationBlock block : blocks) {
                result.add(block.toDisplayBlock());
            }
            return result;
        }
    }

    private record AnimationFrame(int elapsedTicks,
                                  float angle,
                                  Quaternionf rotation,
                                  List<org.joml.Vector3f> translations,
                                  List<org.joml.Vector3f> centers) {
    }

    public static final class AnimationBlock {

        private final AbstractBlockDisplayEffect.DisplayBlock displayBlock;

        private Location dropLocation;

        public AnimationBlock(Block block) {
            this.displayBlock = new AbstractBlockDisplayEffect.DisplayBlock(block);
        }

        public int getX() {
            return displayBlock.x();
        }

        public int getY() {
            return displayBlock.y();
        }

        public int getZ() {
            return displayBlock.z();
        }

        public BlockData getBlockData() {
            return displayBlock.blockData();
        }

        public Location getDropLocation() {
            return dropLocation;
        }

        private void setDropLocation(Location dropLocation) {
            this.dropLocation = dropLocation;
        }

        private AbstractBlockDisplayEffect.DisplayBlock toDisplayBlock() {
            return displayBlock;
        }
    }

    public record FallDamageOptions(boolean enabled,
                                    boolean damagePlayers,
                                    boolean damageEntities,
                                    double damage,
                                    double minAngle,
                                    double hitRadius,
                                    int checkIntervalTicks) {

        public static FallDamageOptions disabled() {
            return new FallDamageOptions(false, false, false, 0.0, 0.0, 0.0, 1);
        }
    }
}
