package cn.superiormc.ultimatetweak.packets.entity.meta.projectile;

import cn.superiormc.ultimatetweak.packets.entity.meta.EntityMeta;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;

public final class ItemEntityMeta extends EntityMeta {

    public ItemEntityMeta(int entityId) {
        super(entityId);
    }

    public void setItem(ItemStack item) {
        set(8, EntityDataTypes.ITEMSTACK, item);
    }
}
