package cn.superiormc.ultimatetweak.tweaks.multiblock;

import cn.superiormc.ultimatetweak.tweaks.display.AbstractBlockDisplayEffect;
import cn.superiormc.ultimatetweak.utils.SchedulerUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

final class MultiBlockDisplayGlow {

    private MultiBlockDisplayGlow() {
    }

    static GlowSession show(World world,
                            Location center,
                            List<Block> blocks,
                            int durationTicks,
                            int viewDistance,
                            int glowColor) {
        if (world == null || center == null || blocks == null || blocks.isEmpty()) {
            return null;
        }
        List<AbstractBlockDisplayEffect.DisplayBlock> displayBlocks = new ArrayList<>(blocks.size());
        for (Block block : blocks) {
            displayBlocks.add(new AbstractBlockDisplayEffect.DisplayBlock(block));
        }
        GlowSession session = new GlowSession(world, center, displayBlocks, viewDistance, glowColor);
        if (!session.show()) {
            return null;
        }
        session.destroyTask = SchedulerUtil.runTaskLater(session::destroy, Math.max(1, durationTicks));
        return session;
    }

    static final class GlowSession extends AbstractBlockDisplayEffect {

        private SchedulerUtil destroyTask;

        private boolean destroyed;

        private GlowSession(World world,
                            Location center,
                            List<DisplayBlock> blocks,
                            int viewDistance,
                            int glowColor) {
            super(world, center, blocks, viewDistance, DisplayOptions.outlineOnly(glowColor));
        }

        private boolean show() {
            return spawnDisplays();
        }

        void destroy() {
            if (destroyed) {
                return;
            }
            destroyed = true;
            if (destroyTask != null) {
                destroyTask.cancel();
                destroyTask = null;
            }
            destroyDisplays();
        }
    }
}
