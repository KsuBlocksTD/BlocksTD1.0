package me.matthewTest.pluginTest.interfaces;

import org.bukkit.command.CommandSender;

public interface MTDSubcommand {
    String getName();
    String getDescription();
    String getUsage();
    boolean execute(CommandSender sender, String[] args);
}

