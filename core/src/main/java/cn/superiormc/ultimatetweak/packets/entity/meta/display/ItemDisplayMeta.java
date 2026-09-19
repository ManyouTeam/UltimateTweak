package cn.superiormc.ultimatetweak.packets.entity.meta.display;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;

public final class ItemDisplayMeta extends AbstractDisplayMeta {

    public ItemDisplayMeta(int entityId) {
        super(entityId);
    }

    public void setItem(ItemStack item) {
        set(MAX_OFFSET, EntityDataTypes.ITEMSTACK, item);
    }

    public void setDisplayType(DisplayType type) {
        set(MAX_OFFSET + 1, EntityDataTypes.BYTE, (byte) type.ordinal());
    }

    public enum DisplayType {
        NONE,
        THIRD_PERSON_LEFT_HAND,
        THIRD_PERSON_RIGHT_HAND,
        FIRST_PERSON_LEFT_HAND,
        FIRST_PERSON_RIGHT_HAND,
        HEAD,
        GUI,
        GROUND,
        FIXED
    }
}
