package cn.superiormc.ultimatetweak.expansion;

import cn.superiormc.ultimatetweak.UltimateTweak;
import cn.superiormc.ultimatetweak.managers.ErrorManager;
import cn.superiormc.ultimatetweak.utils.TextUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

public final class ExpansionModuleManager {

    private static final List<ExpansionModule> modules = new ArrayList<>();

    private ExpansionModuleManager() {
    }

    public static void load(UltimateTweak plugin) {
        if (!modules.isEmpty()) {
            return;
        }

        try {
            ServiceLoader<ExpansionModule> providers = ServiceLoader
                    .load(ExpansionModule.class, plugin.getClass().getClassLoader());
            int discovered = 0;
            for (ExpansionModule provider : providers) {
                discovered++;
                try {
                    provider.enable(plugin);
                    modules.add(provider);
                } catch (Throwable throwable) {
                    ErrorManager.errorManager.sendErrorMessage("§cError: Could not enable expansion module "
                            + provider.getClass().getName() + ": " + throwable.getMessage());
                }
            }
            if (discovered == 0) {
                TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fRunning the free edition.");
                return;
            }
            if (!modules.isEmpty()) {
                TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fEnabled " + modules.size()
                        + " expansion module(s).");
            }
        } catch (Throwable throwable) {
            ErrorManager.errorManager.sendErrorMessage("§cError: Could not discover expansion modules: "
                    + throwable.getMessage());
        }
    }

    public static void unload() {
        List<ExpansionModule> loaded = new ArrayList<>(modules);
        modules.clear();
        Collections.reverse(loaded);
        for (ExpansionModule module : loaded) {
            try {
                module.disable();
            } catch (Throwable throwable) {
                ErrorManager.errorManager.sendErrorMessage("§cError: Could not disable expansion module "
                        + module.getClass().getName() + ": " + throwable.getMessage());
            }
        }
    }

    public static boolean isExpansionLoaded() {
        return !modules.isEmpty();
    }
}
