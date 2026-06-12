package cn.superiormc.ultimatetweak.managers;

import cn.superiormc.ultimatetweak.UltimateTweak;
import cn.superiormc.ultimatetweak.listeners.TweakListener;
import com.github.retrooper.packetevents.PacketEvents;
import org.bukkit.Bukkit;

public class ListenerManager {

    public static ListenerManager listenerManager;

    public ListenerManager(){
        listenerManager = this;
        registerListeners();
    }

    private void registerListeners(){
        Bukkit.getPluginManager().registerEvents(new TweakListener(), UltimateTweak.instance);
        PacketEvents.getAPI().getEventManager().registerListener(SwingItemManager.swingItemManager);
    }

    public void shutdown() {
        PacketEvents.getAPI().getEventManager().unregisterListener(SwingItemManager.swingItemManager);
    }

}
