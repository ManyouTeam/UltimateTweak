package cn.superiormc.ultimatetweak.tweaks.display;

import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Quaternion4f;
import com.github.retrooper.packetevents.util.Vector3f;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import me.tofaa.entitylib.meta.display.BlockDisplayMeta;
import me.tofaa.entitylib.wrapper.WrapperEntity;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public abstract class AbstractBlockDisplayEffect {

    protected static final int FULL_BRIGHTNESS = 0xF000F0;

    protected static final int WHITE_GLOW = 0xFFFFFF;

    private final World world;

    private final org.bukkit.Location center;

    private final List<DisplayBlock> blocks;

    private final int viewDistance;

    private final DisplayOptions options;

    private final List<DisplayEntity> entities = new ArrayList<>();

    protected AbstractBlockDisplayEffect(World world,
                                         org.bukkit.Location center,
                                         List<DisplayBlock> blocks,
                                         int viewDistance,
                                         DisplayOptions options) {
        this.world = world;
        this.center = center;
        this.blocks = blocks == null ? Collections.emptyList() : blocks;
        this.viewDistance = Math.max(1, viewDistance);
        this.options = options == null ? DisplayOptions.normal() : options;
    }

    protected World world() {
        return world;
    }

    protected org.bukkit.Location center() {
        return center;
    }

    protected List<DisplayBlock> blocks() {
        return blocks;
    }

    protected int viewDistance() {
        return viewDistance;
    }

    protected boolean hasBlocks() {
        return world != null && center != null && !blocks.isEmpty();
    }

    protected boolean spawnDisplays() {
        if (!hasBlocks()) {
            return false;
        }

        List<Player> viewers = getViewers();
        if (viewers.isEmpty()) {
            return false;
        }

        for (DisplayBlock block : blocks) {
            WrappedBlockState blockState = SpigotConversionUtil.fromBukkitBlockData(block.blockData());
            WrapperEntity entity = spawnBlockDisplay(viewers, block, blockState, null);
            if (entity != null) {
                entities.add(new DisplayEntity(block, blockState, entity));
            }
        }
        return !entities.isEmpty();
    }

    protected void updateDisplays(Quaternionf rotation) {
        updateDisplays(rotation, (Function<DisplayBlock, org.joml.Vector3f>) null);
    }

    protected void updateDisplays(Quaternionf rotation,
                                  Function<DisplayBlock, org.joml.Vector3f> translationProvider) {
        for (DisplayEntity displayEntity : entities) {
            org.joml.Vector3f translation = translationProvider == null
                    ? null
                    : translationProvider.apply(displayEntity.block());
            updateMetadata(displayEntity.entity(), displayEntity.blockState(), rotation, translation);
        }
    }

    protected void updateDisplays(Quaternionf rotation, List<org.joml.Vector3f> translations) {
        int size = Math.min(entities.size(), translations.size());
        for (int index = 0; index < size; index++) {
            DisplayEntity displayEntity = entities.get(index);
            updateMetadata(displayEntity.entity(), displayEntity.blockState(), rotation, translations.get(index));
        }
    }

    protected void teleport(DisplayEntity displayEntity, org.joml.Vector3f position) {
        if (displayEntity == null || displayEntity.entity() == null || position == null) {
            return;
        }

        displayEntity.entity().teleport(new Location(position.x, position.y, position.z, 0.0F, 0.0F));
    }

    protected void destroyDisplays() {
        for (DisplayEntity displayEntity : entities) {
            if (displayEntity.entity() != null) {
                displayEntity.entity().remove();
            }
        }
        entities.clear();
    }

    protected List<DisplayEntity> entities() {
        return entities;
    }

    private List<Player> getViewers() {
        if (world == null || center == null) {
            return Collections.emptyList();
        }

        List<Player> viewers = new ArrayList<>();
        double maxDistanceSquared = (double) viewDistance * viewDistance;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.isOnline() || !player.getWorld().equals(world)) {
                continue;
            }
            if (player.getLocation().distanceSquared(center) <= maxDistanceSquared) {
                viewers.add(player);
            }
        }
        return viewers;
    }

    private WrapperEntity spawnBlockDisplay(List<Player> viewers,
                                            DisplayBlock block,
                                            WrappedBlockState blockState,
                                            Quaternionf rotation) {
        if (blockState == null) {
            return null;
        }

        WrapperEntity entity = new WrapperEntity(EntityTypes.BLOCK_DISPLAY);
        applyMetadata(entity, blockState, rotation);
        for (Player viewer : viewers) {
            entity.addViewer(viewer.getUniqueId());
        }
        entity.spawn(new Location(block.x(), block.y(), block.z(), 0.0F, 0.0F));
        return entity;
    }

    private void updateMetadata(WrapperEntity entity,
                                WrappedBlockState blockState,
                                Quaternionf rotation,
                                org.joml.Vector3f translation) {
        if (entity == null || blockState == null) {
            return;
        }

        applyMetadata(entity, blockState, rotation, translation);
        entity.refresh();
    }

    private void applyMetadata(WrapperEntity entity, WrappedBlockState blockState, Quaternionf rotation) {
        applyMetadata(entity, blockState, rotation, null);
    }

    private void applyMetadata(WrapperEntity entity,
                               WrappedBlockState blockState,
                               Quaternionf rotation,
                               org.joml.Vector3f translation) {
        BlockDisplayMeta meta = entity.getEntityMeta(BlockDisplayMeta.class);
        meta.setViewRange((float) viewDistance);
        meta.setGlowing(options.glow());
        meta.setGlowColorOverride(options.glowColor());
        meta.setInterpolationDelay(0);
        meta.setTransformationInterpolationDuration(options.interpolationDuration());
        meta.setPositionRotationInterpolationDuration(options.interpolationDuration());
        meta.setBrightnessOverride(options.fullBright() ? FULL_BRIGHTNESS : -1);
        meta.setShadowRadius(0.0F);
        meta.setShadowStrength(0.0F);
        meta.setScale(new Vector3f(options.scale(), options.scale(), options.scale()));
        org.joml.Vector3f appliedTranslation = translation == null
                ? getRotatedBaseTranslation(rotation)
                : translation;
        meta.setTranslation(new Vector3f(appliedTranslation.x, appliedTranslation.y, appliedTranslation.z));
        meta.setBlockState(blockState);
        if (rotation != null) {
            meta.setLeftRotation(new Quaternion4f(rotation.x, rotation.y, rotation.z, rotation.w));
        }
    }

    protected org.joml.Vector3f getRotatedBaseTranslation(Quaternionf rotation) {
        org.joml.Vector3f translation = new org.joml.Vector3f(
                options.translation(), options.translation(), options.translation());
        if (rotation != null) {
            rotation.transform(translation);
        }
        return translation;
    }

    public record DisplayBlock(int x, int y, int z, BlockData blockData) {

        public DisplayBlock(Block block) {
            this(block.getX(), block.getY(), block.getZ(), block.getBlockData().clone());
        }

        public DisplayBlock(Block block, BlockData blockData) {
            this(block.getX(), block.getY(), block.getZ(), blockData);
        }
    }

    protected record DisplayEntity(DisplayBlock block, WrappedBlockState blockState, WrapperEntity entity) {

    }

    protected record DisplayOptions(boolean glow,
                                    boolean fullBright,
                                    int glowColor,
                                    float scale,
                                    float translation,
                                    int interpolationDuration) {

        public static DisplayOptions normal() {
            return new DisplayOptions(false, true, WHITE_GLOW, 1.0F, 0.0F, 0);
        }

        public static DisplayOptions animated(boolean glow, int glowColor, int interpolationDuration) {
            // Slight overlap prevents visible seams between independently rotated block displays.
            return new DisplayOptions(glow, true, glowColor, 1.02F, -0.01F,
                    Math.max(1, interpolationDuration));
        }

        public static DisplayOptions outlineOnly(int glowColor) {
            return new DisplayOptions(true, true, glowColor, 1.01F, -0.005F, 0);
        }
    }
}
