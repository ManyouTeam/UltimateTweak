package cn.superiormc.ultimatetweak.listeners;

import cn.superiormc.ultimatetweak.managers.SwingItemManager;
import cn.superiormc.ultimatetweak.managers.TweakManager;
import cn.superiormc.ultimatetweak.tweaks.AbstractTweak;
import cn.superiormc.ultimatetweak.tweaks.TweakEventType;
import cn.superiormc.ultimatetweak.utils.SchedulerUtil;
import io.papermc.paper.event.block.BlockBreakProgressUpdateEvent;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDamageAbortEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;

import java.util.UUID;

public class TweakListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent event) {
        for (AbstractTweak<?> tweak : TweakManager.tweakManager.getTweaks(TweakEventType.BLOCK_DAMAGE)) {
            TweakManager.tweakManager.call(tweak, () -> tweak.onBlockDamage(event));
        }
    }

    @EventHandler
    public void onBlockDamageAbort(BlockDamageAbortEvent event) {
        for (AbstractTweak<?> tweak : TweakManager.tweakManager.getTweaks(TweakEventType.BLOCK_DAMAGE_ABORT)) {
            TweakManager.tweakManager.call(tweak, () -> tweak.onBlockDamageAbort(event));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        for (AbstractTweak<?> tweak : TweakManager.tweakManager.getTweaks(TweakEventType.BLOCK_BREAK)) {
            TweakManager.tweakManager.call(tweak, () -> tweak.onBlockBreak(event));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockDropItem(BlockDropItemEvent event) {
        for (AbstractTweak<?> tweak : TweakManager.tweakManager.getTweaks(TweakEventType.BLOCK_DROP_ITEM)) {
            TweakManager.tweakManager.call(tweak, () -> tweak.onBlockDropItem(event));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        for (AbstractTweak<?> tweak : TweakManager.tweakManager.getTweaks(TweakEventType.BLOCK_PLACE)) {
            TweakManager.tweakManager.call(tweak, () -> tweak.onBlockPlace(event));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        for (AbstractTweak<?> tweak : TweakManager.tweakManager.getTweaks(TweakEventType.PLAYER_INTERACT)) {
            TweakManager.tweakManager.call(tweak, () -> tweak.onPlayerInteract(event));
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        SwingItemManager.swingItemManager.track(playerId);
        for (AbstractTweak<?> tweak : TweakManager.tweakManager.getTweaks(TweakEventType.PLAYER_JOIN)) {
            TweakManager.tweakManager.call(tweak, () -> tweak.onPlayerJoin(event));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        SwingItemManager.swingItemManager.stopTracking(playerId);
        for (AbstractTweak<?> tweak : TweakManager.tweakManager.getTweaks(TweakEventType.PLAYER_QUIT)) {
            TweakManager.tweakManager.call(tweak, () -> tweak.onPlayerQuit(event));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent event) {
        for (AbstractTweak<?> tweak : TweakManager.tweakManager.getTweaks(TweakEventType.ITEM_SPAWN)) {
            TweakManager.tweakManager.call(tweak, () -> tweak.onItemSpawn(event));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        for (AbstractTweak<?> tweak : TweakManager.tweakManager.getTweaks(TweakEventType.ENTITY_DAMAGE_BY_ENTITY)) {
            TweakManager.tweakManager.call(tweak, () -> tweak.onEntityDamageByEntity(event));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        for (AbstractTweak<?> tweak : TweakManager.tweakManager.getTweaks(TweakEventType.ENTITY_DEATH)) {
            TweakManager.tweakManager.call(tweak, () -> tweak.onEntityDeath(event));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onVehicleEnter(VehicleEnterEvent event) {
        for (AbstractTweak<?> tweak : TweakManager.tweakManager.getTweaks(TweakEventType.VEHICLE_ENTER)) {
            TweakManager.tweakManager.call(tweak, () -> tweak.onVehicleEnter(event));
        }
    }
}
