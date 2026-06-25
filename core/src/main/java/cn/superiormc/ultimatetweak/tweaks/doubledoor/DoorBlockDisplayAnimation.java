package cn.superiormc.ultimatetweak.tweaks.doubledoor;

import cn.superiormc.ultimatetweak.tweaks.display.AbstractBlockDisplayEffect;
import cn.superiormc.ultimatetweak.utils.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Player;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class DoorBlockDisplayAnimation {

    private static final float MAX_DEGREES = 90.0F;

    private DoorBlockDisplayAnimation() {
    }

    static AnimationSession create(World world,
                                   List<AnimationDoor> doors,
                                   int durationTicks,
                                   int intervalTicks,
                                   int viewDistance) {
        return new AnimationSession(world, doors,
                Math.max(1, durationTicks), Math.max(1, intervalTicks), Math.max(1, viewDistance));
    }

    static final class AnimationSession extends AbstractBlockDisplayEffect {

        private final List<AnimationDoor> doors;

        private final List<Player> viewers;

        private final int durationTicks;

        private final int intervalTicks;

        private SchedulerUtil task;

        private AnimationSession(World world,
                                 List<AnimationDoor> doors,
                                 int durationTicks,
                                 int intervalTicks,
                                 int viewDistance) {
            super(world, getCenter(world, doors), toDisplayBlocks(doors), viewDistance,
                    DisplayOptions.exactAnimated(false, WHITE_GLOW, intervalTicks));
            this.doors = doors == null ? Collections.emptyList() : doors;
            this.viewers = getViewers(world, center(), viewDistance);
            this.durationTicks = durationTicks;
            this.intervalTicks = intervalTicks;
        }

        void play(Runnable onComplete) {
            if (!hasBlocks() || viewers.isEmpty()) {
                complete(onComplete);
                return;
            }

            hideDoorBlocks();
            if (!spawnDisplays()) {
                complete(onComplete);
                return;
            }

            List<AnimationFrame> frames = calculateFrames();
            startAnimation(frames, onComplete);
        }

        void stop() {
            if (task != null) {
                task.cancel();
                task = null;
            }
            restoreDoorBlocks();
            destroyDisplays();
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
            double eased = progress * progress * (3.0 - 2.0 * progress);
            List<Quaternionf> rotations = new ArrayList<>(blocks().size());
            List<org.joml.Vector3f> translations = new ArrayList<>(blocks().size());
            for (AnimationDoor door : doors) {
                float angle = door.startAngle() * (1.0F - (float) eased);
                Quaternionf rotation = new Quaternionf().rotateY((float) Math.toRadians(angle));
                for (DisplayBlock block : door.blocks()) {
                    rotations.add(rotation);
                    translations.add(getDisplayTranslation(block, door.transform(), angle, rotation));
                }
            }
            return new AnimationFrame(elapsedTicks, rotations, translations);
        }

        private void startAnimation(List<AnimationFrame> frames, Runnable onComplete) {
            if (frames.isEmpty()) {
                complete(onComplete);
                return;
            }

            final int[] frameIndex = {0};
            task = SchedulerUtil.runTaskTimer(() -> {
                AnimationFrame frame = frames.get(frameIndex[0]++);
                hideDoorBlocks();
                updateDoorDisplays(frame);

                if (frameIndex[0] >= frames.size()) {
                    if (task != null) {
                        task.cancel();
                        task = null;
                    }
                    SchedulerUtil.runTaskLater(() -> complete(onComplete), intervalTicks);
                }
            }, 1L, intervalTicks);
        }

        private void updateDoorDisplays(AnimationFrame frame) {
            int size = Math.min(entities().size(), frame.translations().size());
            for (int index = 0; index < size; index++) {
                updateDisplay(entities().get(index), frame.rotations().get(index), frame.translations().get(index));
            }
        }

        private org.joml.Vector3f getDisplayTranslation(DisplayBlock block,
                                                        DoorTransform transform,
                                                        float angle,
                                                        Quaternionf rotation) {
            org.joml.Vector3f pivot = transform.pivot();
            org.joml.Vector3f shift = transform.getShift(angle);
            org.joml.Vector3f rotatedPivot = new org.joml.Vector3f(pivot);
            rotation.transform(rotatedPivot);
            return new org.joml.Vector3f(pivot).sub(rotatedPivot).add(shift);
        }

        private void hideDoorBlocks() {
            BlockData air = Material.AIR.createBlockData();
            for (Player viewer : viewers) {
                for (AnimationDoor door : doors) {
                    viewer.sendBlockChange(door.bottomBlock().getLocation(), air);
                    viewer.sendBlockChange(door.topBlock().getLocation(), air);
                }
            }
        }

        private void restoreDoorBlocks() {
            for (Player viewer : viewers) {
                if (!viewer.isOnline()) {
                    continue;
                }
                for (AnimationDoor door : doors) {
                    viewer.sendBlockChange(door.bottomBlock().getLocation(), door.bottomBlock().getBlockData());
                    viewer.sendBlockChange(door.topBlock().getLocation(), door.topBlock().getBlockData());
                }
            }
        }

        private void complete(Runnable onComplete) {
            if (onComplete != null) {
                onComplete.run();
            }
            restoreDoorBlocks();
            SchedulerUtil.runTaskLater(() -> {
                destroyDisplays();
            }, 1L);
        }

        private static Location getCenter(World world, List<AnimationDoor> doors) {
            if (world == null || doors == null || doors.isEmpty()) {
                return null;
            }
            Block block = doors.get(0).bottomBlock();
            return block.getLocation().add(0.5, 0.5, 0.5);
        }

        private static List<Player> getViewers(World world, Location center, int viewDistance) {
            if (world == null || center == null) {
                return Collections.emptyList();
            }
            double maxDistanceSquared = (double) viewDistance * viewDistance;
            List<Player> result = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isOnline()
                        && player.getWorld().equals(world)
                        && player.getLocation().distanceSquared(center) <= maxDistanceSquared) {
                    result.add(player);
                }
            }
            return result;
        }

        private static List<DisplayBlock> toDisplayBlocks(List<AnimationDoor> doors) {
            if (doors == null || doors.isEmpty()) {
                return Collections.emptyList();
            }
            List<DisplayBlock> result = new ArrayList<>(doors.size() * 2);
            for (AnimationDoor door : doors) {
                result.addAll(door.blocks());
            }
            return result;
        }
    }

    static final class AnimationDoor {

        private final Block bottomBlock;

        private final Block topBlock;

        private final List<AbstractBlockDisplayEffect.DisplayBlock> blocks;

        private final DoorTransform transform;

        private final float startAngle;

        AnimationDoor(Block bottomBlock, boolean targetOpen) {
            this.bottomBlock = bottomBlock;
            this.topBlock = bottomBlock.getRelative(BlockFace.UP);
            Door door = (Door) bottomBlock.getBlockData();
            BlockData bottomData = bottomBlock.getBlockData().clone();
            BlockData topData = topBlock.getBlockData().clone();
            setOpen(bottomData, targetOpen);
            setOpen(topData, targetOpen);
            this.blocks = List.of(
                    new AbstractBlockDisplayEffect.DisplayBlock(bottomBlock, bottomData),
                    new AbstractBlockDisplayEffect.DisplayBlock(topBlock, topData));
            this.transform = DoorTransform.of(door);
            this.startAngle = getStartAngle(door, targetOpen);
        }

        private static void setOpen(BlockData blockData, boolean open) {
            if (blockData instanceof Door door) {
                door.setOpen(open);
            }
        }

        private static float getStartAngle(Door door, boolean targetOpen) {
            float openAngle = door.getHinge() == Door.Hinge.RIGHT ? -MAX_DEGREES : MAX_DEGREES;
            return targetOpen ? -openAngle : openAngle;
        }

        Block bottomBlock() {
            return bottomBlock;
        }

        Block topBlock() {
            return topBlock;
        }

        List<AbstractBlockDisplayEffect.DisplayBlock> blocks() {
            return blocks;
        }

        DoorTransform transform() {
            return transform;
        }

        float startAngle() {
            return startAngle;
        }
    }

    private record DoorTransform(org.joml.Vector3f pivot, float offsetX, float offsetZ) {

        private static final float THICKNESS = 3.0F / 16.0F;

        private static DoorTransform of(Door door) {
            Bounds bounds = Bounds.of(door);
            BlockFace hingeSide = getHingeSide(door);
            float pivotX = bounds.centerX();
            float pivotZ = bounds.centerZ();
            switch (hingeSide) {
                case EAST -> pivotX = bounds.maxX();
                case WEST -> pivotX = bounds.minX();
                case SOUTH -> pivotZ = bounds.maxZ();
                case NORTH -> pivotZ = bounds.minZ();
                default -> {
                }
            }

            float halfThickness = bounds.thickness(door.getFacing()) * 0.5F;
            float offsetX = 0.0F;
            float offsetZ = 0.0F;
            switch (door.getFacing()) {
                case NORTH -> offsetZ = halfThickness;
                case SOUTH -> offsetZ = -halfThickness;
                case WEST -> offsetX = halfThickness;
                case EAST -> offsetX = -halfThickness;
                default -> {
                }
            }
            return new DoorTransform(new org.joml.Vector3f(pivotX, 0.0F, pivotZ), offsetX, offsetZ);
        }

        private org.joml.Vector3f getShift(float angle) {
            double radians = Math.toRadians(angle);
            float cos = (float) Math.cos(radians);
            float sin = (float) Math.sin(radians);
            float rotatedX = offsetX * cos - offsetZ * sin;
            float rotatedZ = offsetX * sin + offsetZ * cos;
            return new org.joml.Vector3f(offsetX - rotatedX, 0.0F, offsetZ - rotatedZ);
        }

        private static BlockFace getHingeSide(Door door) {
            return door.getHinge() == Door.Hinge.RIGHT
                    ? rotateYClockwise(door.getFacing())
                    : rotateYCounterClockwise(door.getFacing());
        }

        private static BlockFace rotateYClockwise(BlockFace face) {
            return switch (face) {
                case NORTH -> BlockFace.EAST;
                case EAST -> BlockFace.SOUTH;
                case SOUTH -> BlockFace.WEST;
                case WEST -> BlockFace.NORTH;
                default -> face;
            };
        }

        private static BlockFace rotateYCounterClockwise(BlockFace face) {
            return switch (face) {
                case NORTH -> BlockFace.WEST;
                case WEST -> BlockFace.SOUTH;
                case SOUTH -> BlockFace.EAST;
                case EAST -> BlockFace.NORTH;
                default -> face;
            };
        }

        private record Bounds(float minX, float maxX, float minZ, float maxZ) {

            private static Bounds of(Door door) {
                return switch (door.getFacing()) {
                    case EAST -> new Bounds(0.0F, THICKNESS, 0.0F, 1.0F);
                    case SOUTH -> new Bounds(0.0F, 1.0F, 0.0F, THICKNESS);
                    case WEST -> new Bounds(1.0F - THICKNESS, 1.0F, 0.0F, 1.0F);
                    case NORTH -> new Bounds(0.0F, 1.0F, 1.0F - THICKNESS, 1.0F);
                    default -> new Bounds(0.0F, 1.0F, 0.0F, 1.0F);
                };
            }

            private float centerX() {
                return (minX + maxX) * 0.5F;
            }

            private float centerZ() {
                return (minZ + maxZ) * 0.5F;
            }

            private float thickness(BlockFace facing) {
                return facing == BlockFace.NORTH || facing == BlockFace.SOUTH
                        ? maxZ - minZ
                        : maxX - minX;
            }
        }
    }

    private record AnimationFrame(int elapsedTicks,
                                  List<Quaternionf> rotations,
                                  List<org.joml.Vector3f> translations) {
    }
}
