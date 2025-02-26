package me.matthewTest.pluginTest.interfaces;

import org.bukkit.command.CommandSender;

public interface PARTYSubcommand {
    String getName();
    String getDescription();
    String getUsage();
    boolean execute(CommandSender sender, String[] args);
}
