package cn.superiormc.ultimatetweak.managers;

import cn.superiormc.ultimatetweak.tweaks.AbstractTweak;
import cn.superiormc.ultimatetweak.tweaks.TweakEventType;
import cn.superiormc.ultimatetweak.utils.SchedulerUtil;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class SwingItemManager extends PacketListenerAbstract {

    public static SwingItemManager swingItemManager;

    private static final long TIMEOUT_TICKS = 4L;

    private final AtomicLong currentTick = new AtomicLong();

    private final Map<UUID, Long> lastSwingTicks = new ConcurrentHashMap<>();

    private final SchedulerUtil timeoutTask;

    public SwingItemManager() {
        swingItemManager = this;
        timeoutTask = SchedulerUtil.runTaskTimer(this::tick, 1L, 1L);
    }

    public void track(UUID playerId) {
        lastSwingTicks.put(playerId, currentTick.get());
    }

    public void stopTracking(UUID playerId) {
        lastSwingTicks.remove(playerId);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.ANIMATION || event.getUser().getUUID() == null
                || TweakManager.tweakManager == null) {
            return;
        }
        UUID playerId = event.getUser().getUUID();
        lastSwingTicks.computeIfPresent(playerId, (ignored, tick) -> currentTick.get());
        for (AbstractTweak<?> tweak : TweakManager.tweakManager.getTweaks(TweakEventType.SWING_ITEM)) {
            TweakManager.tweakManager.call(tweak, () -> tweak.onSwingItem(playerId));
        }
    }

    public void shutdown() {
        timeoutTask.cancel();
        lastSwingTicks.clear();
    }

    private void tick() {
        long tick = currentTick.incrementAndGet();
        for (Map.Entry<UUID, Long> entry : lastSwingTicks.entrySet()) {
            if (tick - entry.getValue() < TIMEOUT_TICKS
                    || !lastSwingTicks.remove(entry.getKey(), entry.getValue())) {
                continue;
            }
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null) {
                dispatchNoSwingItem(entry.getKey());
            } else {
                SchedulerUtil.runSync(player, () -> dispatchNoSwingItem(entry.getKey()));
            }
        }
    }

    private void dispatchNoSwingItem(UUID playerId) {
        if (TweakManager.tweakManager == null) {
            return;
        }
        for (AbstractTweak<?> tweak : TweakManager.tweakManager.getTweaks(TweakEventType.NO_SWING_ITEM)) {
            TweakManager.tweakManager.call(tweak, () -> tweak.onNoSwingItem(playerId));
        }
    }
}
