package cn.superiormc.ultimatetweak.managers;

import cn.superiormc.ultimatetweak.commands.*;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CommandManager {

    public static CommandManager commandManager;

    private Map<String, AbstractCommand> registeredCommands = new HashMap<>();

    public CommandManager(){
        commandManager = this;
        registerBukkitCommands();
        registerObjectCommand();
    }

    private void registerBukkitCommands(){
        Objects.requireNonNull(Bukkit.getPluginCommand("ultimatetweak")).setExecutor(new MainCommand());
        Objects.requireNonNull(Bukkit.getPluginCommand("ultimatetweak")).setTabCompleter(new MainCommandTab());
    }

    private void registerObjectCommand() {
        registerNewSubCommand(new SubReload());
        registerNewSubCommand(new SubSaveItem());
        registerNewSubCommand(new SubGiveSaveItem());
        registerNewSubCommand(new SubGenerateItemFormat());
        registerNewSubCommand(new SubDebug());
    }

    public Map<String, AbstractCommand> getSubCommandsMap() {
        return registeredCommands;
    }

    public void registerNewSubCommand(AbstractCommand command) {
        registeredCommands.put(command.getId(), command);
    }

}
