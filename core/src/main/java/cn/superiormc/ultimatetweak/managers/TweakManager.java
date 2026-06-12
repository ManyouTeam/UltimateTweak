package cn.superiormc.ultimatetweak.managers;

import cn.superiormc.ultimatetweak.UltimateTweak;
import cn.superiormc.ultimatetweak.tweaks.AbstractTweak;
import cn.superiormc.ultimatetweak.tweaks.besttool.BestToolTweak;
import cn.superiormc.ultimatetweak.tweaks.dynamiclight.DynamicLightTweak;
import cn.superiormc.ultimatetweak.tweaks.entityvehiclerestriction.EntityVehicleRestrictionTweak;
import cn.superiormc.ultimatetweak.tweaks.multiblock.treecutter.TreeCutterTweak;
import cn.superiormc.ultimatetweak.tweaks.multiblock.treereplant.TreeReplantTweak;
import cn.superiormc.ultimatetweak.tweaks.veinmine.VeinMineTweak;
import cn.superiormc.ultimatetweak.tweaks.TweakEventType;
import cn.superiormc.ultimatetweak.tweaks.config.TreeCutterConfig;
import cn.superiormc.ultimatetweak.tweaks.config.BestToolConfig;
import cn.superiormc.ultimatetweak.tweaks.config.DynamicLightConfig;
import cn.superiormc.ultimatetweak.tweaks.config.EntityVehicleRestrictionConfig;
import cn.superiormc.ultimatetweak.tweaks.config.TreeReplantConfig;
import cn.superiormc.ultimatetweak.tweaks.config.VeinMineConfig;
import cn.superiormc.ultimatetweak.utils.TextUtil;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class TweakManager {

    public static TweakManager tweakManager;

    private final Map<String, AbstractTweak<?>> tweakMap = new LinkedHashMap<>();

    private final Map<TweakEventType, Collection<AbstractTweak<?>>> eventTweakMap =
            new EnumMap<>(TweakEventType.class);

    public TweakManager() {
        tweakManager = this;
        init();
    }

    private void init() {
        File dir = new File(UltimateTweak.instance.getDataFolder(), "tweaks");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        registerDefaultTweaks(dir);
        rebuildEventTweakMap();
        for (AbstractTweak<?> tweak : tweakMap.values()) {
            call(tweak, tweak::onLoad);
        }
        TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fLoaded " + tweakMap.size() + " tweaks!");
    }

    private void registerDefaultTweaks(File dir) {
        registerTweak(new BestToolTweak(new BestToolConfig(new File(dir, "best-tool.yml"))));
        registerTweak(new TreeCutterTweak(new TreeCutterConfig(new File(dir, "tree-cuter.yml"))));
        registerTweak(new TreeReplantTweak(new TreeReplantConfig(new File(dir, "tree-replant.yml"))));
        registerTweak(new VeinMineTweak(new VeinMineConfig(new File(dir, "vein-mine.yml"))));
        registerTweak(new EntityVehicleRestrictionTweak(
                new EntityVehicleRestrictionConfig(new File(dir, "entity-vehicle-restriction.yml"))));
        registerTweak(new DynamicLightTweak(new DynamicLightConfig(new File(dir, "dynamic-light.yml"))));
    }

    private void registerTweak(AbstractTweak<?> tweak) {
        if (tweakMap.containsKey(tweak.getId())) {
            ErrorManager.errorManager.sendErrorMessage("§cError: Already loaded a tweak called: " + tweak.getId() + "!");
            return;
        }
        TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fLoaded tweak " + tweak.getId() + "!");
        tweakMap.put(tweak.getId(), tweak);
    }

    private void rebuildEventTweakMap() {
        Map<TweakEventType, Collection<AbstractTweak<?>>> tempMap = new EnumMap<>(TweakEventType.class);
        for (TweakEventType eventType : TweakEventType.values()) {
            tempMap.put(eventType, new java.util.ArrayList<>());
        }
        for (AbstractTweak<?> tweak : tweakMap.values()) {
            if (!tweak.isEnabled()) {
                continue;
            }
            for (TweakEventType eventType : tweak.getEventTypes()) {
                tempMap.get(eventType).add(tweak);
            }
        }
        eventTweakMap.clear();
        for (Map.Entry<TweakEventType, Collection<AbstractTweak<?>>> entry : tempMap.entrySet()) {
            eventTweakMap.put(entry.getKey(), Collections.unmodifiableCollection(entry.getValue()));
        }
    }

    public void reload() {
        for (AbstractTweak<?> tweak : tweakMap.values()) {
            callEvenDisabled(tweak, tweak::onReload);
        }
        rebuildEventTweakMap();
    }

    public void shutdown() {
        for (AbstractTweak<?> tweak : tweakMap.values()) {
            callEvenDisabled(tweak, tweak::onDisable);
        }
    }

    public AbstractTweak<?> getTweak(String id) {
        return tweakMap.get(id);
    }

    public Collection<AbstractTweak<?>> getTweaks() {
        return Collections.unmodifiableCollection(tweakMap.values());
    }

    public Collection<AbstractTweak<?>> getTweaks(TweakEventType eventType) {
        Collection<AbstractTweak<?>> tweaks = eventTweakMap.get(eventType);
        return tweaks == null ? Collections.emptyList() : tweaks;
    }

    public void call(AbstractTweak<?> tweak, Runnable runnable) {
        if (!tweak.isEnabled()) {
            return;
        }
        callEvenDisabled(tweak, runnable);
    }

    private void callEvenDisabled(AbstractTweak<?> tweak, Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable throwable) {
            ErrorManager.errorManager.sendErrorMessage("§cError: Tweak " + tweak.getId() + " failed: " + throwable.getMessage());
            throwable.printStackTrace();
        }
    }
}
