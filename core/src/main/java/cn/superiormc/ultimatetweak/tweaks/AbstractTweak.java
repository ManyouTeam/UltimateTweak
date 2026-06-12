package cn.superiormc.ultimatetweak.tweaks;

import cn.superiormc.ultimatetweak.tweaks.config.AbstractTweakConfig;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDamageAbortEvent;
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

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

public class AbstractTweak<C extends AbstractTweakConfig> {

    private final String id;

    private final C config;

    protected AbstractTweak(String id, C config) {
        this.id = id;
        this.config = config;
    }

    public String getId() {
        return id;
    }

    public C getConfig() {
        return config;
    }

    public boolean isEnabled() {
        return config.isEnabled();
    }

    public Set<TweakEventType> getEventTypes() {
        return EnumSet.noneOf(TweakEventType.class);
    }

    public void onLoad() {
    }

    public void onReload() {
        config.reload();
    }

    public void onDisable() {
    }

    public void onBlockDamage(BlockDamageEvent event) {
    }

    public void onBlockDamageAbort(BlockDamageAbortEvent event) {
    }

    public void onBlockBreak(BlockBreakEvent event) {
    }

    public void onBlockDropItem(BlockDropItemEvent event) {
    }

    public void onBlockPlace(BlockPlaceEvent event) {
    }

    public void onPlayerInteract(PlayerInteractEvent event) {
    }

    public void onSwingItem(UUID playerId) {
    }

    public void onNoSwingItem(UUID playerId) {
    }

    public void onPlayerJoin(PlayerJoinEvent event) {
    }

    public void onPlayerQuit(PlayerQuitEvent event) {
    }

    public void onItemSpawn(ItemSpawnEvent event) {
    }

    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
    }

    public void onEntityDeath(EntityDeathEvent event) {
    }

    public void onVehicleEnter(VehicleEnterEvent event) {
    }
}
