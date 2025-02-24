package me.matthewTest.pluginTest.commands;

import me.matthewTest.pluginTest.interfaces.MTDSubcommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.Map;

public class mtdCommand implements CommandExecutor {

    private final Map<String, MTDSubcommand> subcommands = new HashMap<>();

    private void registerSubcommand(MTDSubcommand subcommand) {
        subcommands.put(subcommand.getName().toLowerCase(), subcommand);
    }

    public mtdCommand(JavaPlugin plugin) {
        // Register new subcommands here
        registerSubcommand(new hubCommand(plugin));
        registerSubcommand(new partyCommand());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Usage: /mtd <subcommand>");
            sender.sendMessage("Available subcommands:");
            for (MTDSubcommand sub : subcommands.values()) {
                sender.sendMessage("- " + sub.getName() + ": " + sub.getDescription());
            }
            return true;
        }

        MTDSubcommand subcommand = subcommands.get(args[0].toLowerCase());
        if (subcommand == null) {
            sender.sendMessage("Unknown subcommand: " + args[0]);
            return true;
        }

        return subcommand.execute(sender, args);
    }
}
