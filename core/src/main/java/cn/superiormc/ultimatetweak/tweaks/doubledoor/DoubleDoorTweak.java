package cn.superiormc.ultimatetweak.tweaks.doubledoor;

import cn.superiormc.ultimatetweak.UltimateTweak;
import cn.superiormc.ultimatetweak.managers.HookManager;
import cn.superiormc.ultimatetweak.tweaks.AbstractTweak;
import cn.superiormc.ultimatetweak.tweaks.TweakEventType;
import cn.superiormc.ultimatetweak.tweaks.config.DoubleDoorConfig;
import cn.superiormc.ultimatetweak.utils.SchedulerUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DoubleDoorTweak extends AbstractTweak<DoubleDoorConfig> {

    private static final BlockFace[] HORIZONTAL_FACES = {
            BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.SOUTH,
            BlockFace.WEST
    };

    private final Map<DoorKey, DoorBlockDisplayAnimation.AnimationSession> activeAnimations =
            new ConcurrentHashMap<>();

    public DoubleDoorTweak(DoubleDoorConfig config) {
        super("DoubleDoor", config);
    }

    @Override
    public Set<TweakEventType> getEventTypes() {
        return EnumSet.of(TweakEventType.PLAYER_INTERACT);
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK
                || event.getHand() != EquipmentSlot.HAND
                || event.getClickedBlock() == null || event.useInteractedBlock() == Event.Result.DENY) {
            return;
        }

        Player player = event.getPlayer();
        if (!getConfig().getConditions().getAllBoolean(player)) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (!HookManager.hookManager.getProtectionCanUse(player, clickedBlock.getLocation())) {
            return;
        }
        BlockData clickedData = clickedBlock.getBlockData();
        if (!(clickedData instanceof Door clickedDoor) || clickedBlock.getType() == Material.IRON_DOOR) {
            return;
        }

        Block clickedBottom = getBottomBlock(clickedBlock, clickedDoor);
        Block pairedBottom = getPairedBottomBlock(clickedBottom, clickedDoor);
        if (isAnimating(clickedBottom) || pairedBottom != null && isAnimating(pairedBottom)) {
            event.setCancelled(true);
            return;
        }
        if (pairedBottom == null) {
            if (isAnimationAvailable() && getConfig().shouldAnimateSingleDoor()) {
                event.setCancelled(true);
                playAnimation(clickedBottom, null, !clickedDoor.isOpen());
            }
            return;
        }

        boolean targetOpen = !clickedDoor.isOpen();
        if (isAnimationAvailable()) {
            event.setCancelled(true);
            playAnimation(clickedBottom, pairedBottom, targetOpen);
            return;
        }

        event.setCancelled(true);
        SchedulerUtil.runTaskLater(() -> {
            setDoorOpen(clickedBottom, targetOpen);
            setDoorOpen(pairedBottom, targetOpen);
        }, 1L);
    }

    private boolean isAnimating(Block bottomBlock) {
        return activeAnimations.containsKey(DoorKey.of(bottomBlock));
    }

    private boolean isAnimationAvailable() {
        return getConfig().isAnimationEnabled() && UltimateTweak.isEntityLibAvailable();
    }

    @Override
    public void onReload() {
        stopAnimations();
        super.onReload();
    }

    @Override
    public void onDisable() {
        stopAnimations();
    }

    private void playAnimation(Block clickedBottom, Block pairedBottom, boolean targetOpen) {
        DoorKey clickedKey = DoorKey.of(clickedBottom);
        DoorKey pairedKey = pairedBottom == null ? null : DoorKey.of(pairedBottom);
        if (activeAnimations.containsKey(clickedKey)
                || pairedKey != null && activeAnimations.containsKey(pairedKey)) {
            return;
        }

        List<DoorBlockDisplayAnimation.AnimationDoor> doors = pairedBottom == null
                ? List.of(new DoorBlockDisplayAnimation.AnimationDoor(clickedBottom, targetOpen))
                : List.of(
                new DoorBlockDisplayAnimation.AnimationDoor(clickedBottom, targetOpen),
                new DoorBlockDisplayAnimation.AnimationDoor(pairedBottom, targetOpen));
        DoorBlockDisplayAnimation.AnimationSession session = DoorBlockDisplayAnimation.create(
                clickedBottom.getWorld(),
                doors,
                getConfig().getAnimationDurationTicks(),
                getConfig().getAnimationIntervalTicks(),
                getConfig().getAnimationViewDistance());
        activeAnimations.put(clickedKey, session);
        if (pairedKey != null) {
            activeAnimations.put(pairedKey, session);
        }

        session.play(() -> {
            setDoorOpen(clickedBottom, targetOpen);
            if (pairedBottom != null) {
                setDoorOpen(pairedBottom, targetOpen);
            }
            activeAnimations.remove(clickedKey);
            if (pairedKey != null) {
                activeAnimations.remove(pairedKey);
            }
        });
    }

    private void stopAnimations() {
        Set<DoorBlockDisplayAnimation.AnimationSession> sessions =
                java.util.Set.copyOf(activeAnimations.values());
        activeAnimations.clear();
        for (DoorBlockDisplayAnimation.AnimationSession session : sessions) {
            session.stop();
        }
    }

    private Block getBottomBlock(Block block, Door door) {
        return door.getHalf() == Bisected.Half.TOP ? block.getRelative(BlockFace.DOWN) : block;
    }

    private Block getPairedBottomBlock(Block clickedBottom, Door clickedDoor) {
        for (BlockFace face : HORIZONTAL_FACES) {
            Block candidate = clickedBottom.getRelative(face);
            BlockData candidateData = candidate.getBlockData();
            if (!(candidateData instanceof Door candidateDoor)) {
                continue;
            }
            if (candidate.getType() == clickedBottom.getType()
                    && candidateDoor.getHalf() == Bisected.Half.BOTTOM
                    && candidateDoor.getFacing() == clickedDoor.getFacing()
                    && candidateDoor.getHinge() != clickedDoor.getHinge()) {
                return candidate;
            }
        }
        return null;
    }

    private void setDoorOpen(Block bottomBlock, boolean open) {
        setDoorHalfOpen(bottomBlock, open);
        setDoorHalfOpen(bottomBlock.getRelative(BlockFace.UP), open);
    }

    private void setDoorHalfOpen(Block block, boolean open) {
        BlockData blockData = block.getBlockData();
        if (!(blockData instanceof Door door) || door.isOpen() == open) {
            return;
        }
        door.setOpen(open);
        block.setBlockData(door, false);
    }

    private record DoorKey(UUID worldId, int x, int y, int z) {

        private static DoorKey of(Block block) {
            return new DoorKey(block.getWorld().getUID(), block.getX(), block.getY(), block.getZ());
        }
    }
}
