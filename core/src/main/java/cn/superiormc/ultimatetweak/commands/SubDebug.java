package cn.superiormc.ultimatetweak.commands;

import cn.superiormc.ultimatetweak.managers.ConfigManager;
import cn.superiormc.ultimatetweak.managers.LanguageManager;
import cn.superiormc.ultimatetweak.managers.TreeDetermineManager;
import cn.superiormc.ultimatetweak.managers.TreeDetermineManager.TreeDefinition;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SubDebug extends AbstractCommand {

    public SubDebug() {
        this.id = "debug";
        this.requiredPermission = "ultimatetweak." + id;
        this.onlyInGame = true;
        this.requiredArgLength = new Integer[]{2};
    }

    @Override
    public void executeCommandInGame(String[] args, Player player) {
        if (args[1].equalsIgnoreCase("off")) {
            TreeDetermineManager.treeDetermineManager.setDebugDefinition(player, null);
            LanguageManager.languageManager.sendStringText(player, "tree-debug.disabled");
            return;
        }

        TreeDefinition definition = ConfigManager.configManager.getTreeDefinition(args[1]);
        if (definition == null) {
            LanguageManager.languageManager.sendStringText(player, "tree-debug.not-found", "id", args[1]);
            return;
        }

        TreeDetermineManager.treeDetermineManager.setDebugDefinition(player, definition);
        LanguageManager.languageManager.sendStringText(player, "tree-debug.enabled", "id", definition.getId());
        LanguageManager.languageManager.sendStringText(player, "tree-debug.enabled-hint");
    }

    @Override
    public List<String> getTabResult(String[] args, Player player) {
        List<String> result = new ArrayList<>();
        if (args.length == 2) {
            result.add("off");
            for (TreeDefinition definition : ConfigManager.configManager.getTreeDefinitions()) {
                result.add(definition.getId());
            }
        }
        return result;
    }

}
