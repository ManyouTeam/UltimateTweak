package cn.superiormc.ultimatetweak.tweaks.structureautoprotect;

import cn.superiormc.ultimatetweak.UltimateTweak;
import cn.superiormc.ultimatetweak.hooks.protection.ProtectionRegion;
import cn.superiormc.ultimatetweak.hooks.protection.ProtectionRegionResult;
import cn.superiormc.ultimatetweak.managers.HookManager;
import cn.superiormc.ultimatetweak.tweaks.AbstractTweak;
import cn.superiormc.ultimatetweak.tweaks.TweakEventType;
import cn.superiormc.ultimatetweak.tweaks.config.StructureAutoProtectConfig;
import cn.superiormc.ultimatetweak.utils.SchedulerUtil;
import cn.superiormc.ultimatetweak.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.generator.structure.GeneratedStructure;
import org.bukkit.util.BoundingBox;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class StructureAutoProtectTweak extends AbstractTweak<StructureAutoProtectConfig> {

    private final Set<String> attemptedRegions = ConcurrentHashMap.newKeySet();

    private SchedulerUtil task;

    public StructureAutoProtectTweak(StructureAutoProtectConfig config) {
        super("StructureAutoProtect", config);
    }

    @Override
    public Set<TweakEventType> getEventTypes() {
        return EnumSet.noneOf(TweakEventType.class);
    }

    @Override
    public void onLoad() {
        startTask();
    }

    @Override
    public void onReload() {
        stopTask();
        super.onReload();
        if (isEnabled()) {
            startTask();
        }
    }

    @Override
    public void onDisable() {
        stopTask();
    }

    private void startTask() {
        if (task != null) {
            return;
        }
        task = SchedulerUtil.runTaskTimer(this::tick, 1L, getConfig().getCheckIntervalTicks());
    }

    private void stopTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        attemptedRegions.clear();
    }

    private void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (UltimateTweak.isFolia) {
                SchedulerUtil.runSync(player, () -> scanPlayerChunk(player));
            } else {
                scanPlayerChunk(player);
            }
        }
    }

    private void scanPlayerChunk(Player player) {
        if (!player.isOnline() || !isWorldEnabled(player.getWorld())) {
            return;
        }
        Location location = player.getLocation();
        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;
        for (GeneratedStructure structure : player.getWorld().getStructures(chunkX, chunkZ)) {
            protectStructure(player, structure);
        }
    }

    private void protectStructure(Player player, GeneratedStructure structure) {
        World world = player.getWorld();
        String structureId = structure.getStructure().getKey().toString().toLowerCase(Locale.ENGLISH);
        if (!getConfig().isStructureAllowed(structureId)) {
            return;
        }
        BoundingBox box = structure.getBoundingBox();
        String regionId = getRegionId(box);
        if (!attemptedRegions.add(regionId)) {
            return;
        }

        int expandY = getConfig().getExpandY();
        ProtectionRegion region = new ProtectionRegion(regionId, world,
                (int) Math.floor(box.getMinX()),
                Math.max(world.getMinHeight(), (int) Math.floor(box.getMinY()) - expandY),
                (int) Math.floor(box.getMinZ()),
                (int) Math.ceil(box.getMaxX()),
                Math.min(world.getMaxHeight() - 1, (int) Math.ceil(box.getMaxY()) + expandY),
                (int) Math.ceil(box.getMaxZ()));
        ProtectionRegionResult result = HookManager.hookManager.createProtectionRegion(getConfig().getProtectionHook(), region);
        if (getConfig().isDebugEnabled() || !result.success()) {
            TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fStructureAutoProtect " + regionId
                    + ": " + result.message());
        }
    }

    private String getRegionId(BoundingBox box) {
        return sanitize(getConfig().getRegionIdPrefix()
                + (int) Math.floor(box.getMinX()) + "_"
                + (int) Math.floor(box.getMinY()) + "_"
                + (int) Math.floor(box.getMinZ()));
    }

    private String sanitize(String input) {
        return input.toLowerCase(Locale.ENGLISH).replaceAll("[^a-z0-9_\\-]", "_");
    }
}
