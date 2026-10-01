package cn.superiormc.ultimatetweak.commands;

import cn.superiormc.ultimatetweak.managers.CommandManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class MainCommandTab implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> tempVal1 = new ArrayList<>();
        if (args.length == 1) {
            for (AbstractCommand object : CommandManager.commandManager.getSubCommandsMap().values()) {
                if (!object.hasRequiredPermission(sender, new String[]{object.getId()})) {
                    continue;
                }
                tempVal1.add(object.getId());
            }
        } else {
            AbstractCommand tempVal2 = CommandManager.commandManager.getSubCommandsMap().get(args[0]);
            if (tempVal2 != null && tempVal2.hasRequiredPermission(sender, args)) {
                AbstractCommand object = CommandManager.commandManager.getSubCommandsMap().get(args[0]);
                tempVal1 = object.filterTabResult(args, sender instanceof Player ? (Player) sender : null);
            }
        }
        return tempVal1;
    }
}
