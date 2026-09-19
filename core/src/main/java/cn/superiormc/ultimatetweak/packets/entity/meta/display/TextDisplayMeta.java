package cn.superiormc.ultimatetweak.packets.entity.meta.display;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import net.kyori.adventure.text.Component;

public final class TextDisplayMeta extends AbstractDisplayMeta {

    private static final int FLAGS_INDEX = MAX_OFFSET + 4;

    public TextDisplayMeta(int entityId) {
        super(entityId);
    }

    public void setText(Component text) {
        set(MAX_OFFSET, EntityDataTypes.ADV_COMPONENT, text);
    }

    public void setLineWidth(int width) {
        set(MAX_OFFSET + 1, EntityDataTypes.INT, width);
    }

    public void setBackgroundColor(int color) {
        set(MAX_OFFSET + 2, EntityDataTypes.INT, color);
    }

    public void setTextOpacity(byte opacity) {
        set(MAX_OFFSET + 3, EntityDataTypes.BYTE, opacity);
    }

    public void setShadow(boolean enabled) {
        setMaskBit(FLAGS_INDEX, 0x01, enabled);
    }

    public void setSeeThrough(boolean enabled) {
        setMaskBit(FLAGS_INDEX, 0x02, enabled);
    }
}
