package cn.superiormc.ultimatetweak.packets.entity.meta;

import cn.superiormc.ultimatetweak.packets.entity.meta.display.BlockDisplayMeta;
import cn.superiormc.ultimatetweak.packets.entity.meta.display.ItemDisplayMeta;
import cn.superiormc.ultimatetweak.packets.entity.meta.display.TextDisplayMeta;
import cn.superiormc.ultimatetweak.packets.entity.meta.projectile.ItemEntityMeta;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataType;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.data.EntityMetadataProvider;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EntityMeta implements EntityMetadataProvider {

    protected final int entityId;

    private final Map<Integer, EntityData<?>> data = new LinkedHashMap<>();

    public EntityMeta(int entityId) {
        this.entityId = entityId;
    }

    public static EntityMeta createMeta(int entityId, EntityType type) {
        if (type == EntityTypes.BLOCK_DISPLAY) {
            return new BlockDisplayMeta(entityId);
        }
        if (type == EntityTypes.ITEM_DISPLAY) {
            return new ItemDisplayMeta(entityId);
        }
        if (type == EntityTypes.TEXT_DISPLAY) {
            return new TextDisplayMeta(entityId);
        }
        if (type == EntityTypes.ITEM) {
            return new ItemEntityMeta(entityId);
        }
        return new EntityMeta(entityId);
    }

    public void setHasNoGravity(boolean noGravity) {
        set(5, EntityDataTypes.BOOLEAN, noGravity);
    }

    public void setGlowing(boolean glowing) {
        byte flags = getByte(0);
        set(0, EntityDataTypes.BYTE,
                glowing ? (byte) (flags | 0x40) : (byte) (flags & ~0x40));
    }

    protected void setMaskBit(int index, int mask, boolean enabled) {
        byte flags = getByte(index);
        set(index, EntityDataTypes.BYTE,
                enabled ? (byte) (flags | mask) : (byte) (flags & ~mask));
    }

    protected <T> void set(int index, EntityDataType<T> type, T value) {
        data.put(index, new EntityData<>(index, type, value));
    }

    private byte getByte(int index) {
        EntityData<?> current = data.get(index);
        return current != null && current.getValue() instanceof Byte value ? value : 0;
    }

    @Override
    public List<EntityData<?>> entityData(ClientVersion version) {
        List<EntityData<?>> result = new ArrayList<>(data.values());
        result.sort(Comparator.comparingInt(EntityData::getIndex));
        return result;
    }
}
