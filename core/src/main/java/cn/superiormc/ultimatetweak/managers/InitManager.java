package cn.superiormc.ultimatetweak.managers;

import cn.superiormc.ultimatetweak.UltimateTweak;
import cn.superiormc.ultimatetweak.utils.CommonUtil;

import java.io.File;

public class InitManager {

    public static InitManager initManager;

    private boolean firstLoad = false;

    public InitManager() {
        initManager = this;
        File file = new File(UltimateTweak.instance.getDataFolder(), "config.yml");
        if (!file.exists()) {
            UltimateTweak.instance.saveDefaultConfig();
            firstLoad = true;
        }
        init();
    }

    public void init() {
        resourceOutput("tweaks/best-tool.yml", true);
        resourceOutput("tweaks/better-drop-display.yml", true);
        resourceOutput("tweaks/double-door.yml", true);
        resourceOutput("tweaks/tree-cuter.yml", true);
        resourceOutput("tweaks/tree-replant.yml", true);
        resourceOutput("tweaks/vein-mine.yml", true);
        resourceOutput("tweaks/entity-vehicle-restriction.yml", true);
        resourceOutput("tweaks/dynamic-light.yml", true);
        resourceOutput("tweaks/biome-announcer.yml", true);
        resourceOutput("tweaks/structure-announcer.yml", true);
        resourceOutput("tweaks/structure-auto-protect.yml", true);
        resourceOutput("tree_determine_settings/oak.yml", true);
        resourceOutput("tree_determine_settings/spruce.yml", true);
        resourceOutput("tree_determine_settings/birch.yml", true);
        resourceOutput("tree_determine_settings/jungle.yml", true);
        resourceOutput("tree_determine_settings/acacia.yml", true);
        resourceOutput("tree_determine_settings/dark_oak.yml", true);
        resourceOutput("tree_determine_settings/mangrove.yml", true);
        resourceOutput("tree_determine_settings/cherry.yml", true);
        resourceOutput("tree_determine_settings/crimson_fungus.yml", true);
        resourceOutput("tree_determine_settings/warped_fungus.yml", true);
        resourceOutput("tree_determine_settings/craftengine_palm.yml", true);
        resourceOutput("languages/en_US.yml", true);
        resourceOutput("languages/zh_CN.yml", true);
    }

    private void resourceOutput(String fileName, boolean regenerate) {
        File tempVal1 = new File(UltimateTweak.instance.getDataFolder(), fileName);
        if (!tempVal1.exists()) {
            if (!firstLoad && !regenerate) {
                return;
            }
            if (tempVal1.getParentFile() != null) {
                CommonUtil.mkDir(tempVal1.getParentFile());
            }
            UltimateTweak.instance.saveResource(fileName, false);
        }
    }

    public boolean isFirstLoad() {
        return firstLoad;
    }
}
