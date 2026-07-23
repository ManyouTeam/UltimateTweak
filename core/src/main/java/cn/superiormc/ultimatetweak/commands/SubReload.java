package cn.superiormc.ultimatetweak.commands;

import cn.superiormc.ultimatetweak.UltimateTweak;
import cn.superiormc.ultimatetweak.managers.ConfigManager;
import cn.superiormc.ultimatetweak.managers.LanguageManager;
import cn.superiormc.ultimatetweak.managers.TweakManager;
import org.bukkit.entity.Player;

public class SubReload extends AbstractCommand {

    public SubReload() {
        this.id = "reload";
        this.requiredPermission =  "ultimatetweak." + id;
        this.onlyInGame = false;
        this.requiredArgLength = new Integer[]{1};
    }

    @Override
    public void executeCommandInGame(String[] args, Player player) {
        UltimateTweak.instance.reloadConfig();
        new ConfigManager();
        if (TweakManager.tweakManager == null) {
            new TweakManager();
        } else {
            TweakManager.tweakManager.reload();
        }
        new LanguageManager();
        LanguageManager.languageManager.sendStringText(player, "plugin.reloaded");
    }

    @Override
    public void executeCommandInConsole(String[] args) {
        UltimateTweak.instance.reloadConfig();
        new ConfigManager();
        if (TweakManager.tweakManager == null) {
            new TweakManager();
        } else {
            TweakManager.tweakManager.reload();
        }
        new LanguageManager();
        LanguageManager.languageManager.sendStringText("plugin.reloaded");
    }
}
