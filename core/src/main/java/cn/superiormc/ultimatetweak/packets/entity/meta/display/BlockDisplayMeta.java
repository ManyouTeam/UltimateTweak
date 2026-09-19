package cn.superiormc.ultimatetweak.packets.entity.meta.display;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;

public final class BlockDisplayMeta extends AbstractDisplayMeta {

    public BlockDisplayMeta(int entityId) {
        super(entityId);
    }

    public void setBlockState(WrappedBlockState state) {
        set(MAX_OFFSET, EntityDataTypes.BLOCK_STATE, state.getGlobalId());
    }
}
