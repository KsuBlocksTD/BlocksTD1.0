package me.matthewTest.pluginTest.commands;

import me.matthewTest.pluginTest.interfaces.MTDSubcommand;
import me.matthewTest.pluginTest.interfaces.PARTYSubcommand;
import org.bukkit.command.CommandSender;
import java.util.HashMap;
import java.util.Map;

public class partyCommand implements MTDSubcommand {

    private void registerSubcommand(PARTYSubcommand subcommand) {
        subcommands.put(subcommand.getName().toLowerCase(), subcommand);
    }
    public partyCommand() {
        // Register new subcommands here
        registerSubcommand(new invitePartyCommand());
    }

    @Override
    public String getName() {
        return "party";
    }

    @Override
    public String getDescription() {
        return "Manage your party.";
    }

    @Override
    public String getUsage() {
        return "/mtd party <subcommand>";
    }

    private final Map<String, PARTYSubcommand> subcommands = new HashMap<>();

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /mtd party <subcommand>");
            sender.sendMessage("Available subcommands:");
            for (PARTYSubcommand sub : subcommands.values()) {
                sender.sendMessage("- " + sub.getName() + ": " + sub.getDescription());
            }
            return true;
        }

        PARTYSubcommand subcommand = subcommands.get(args[1].toLowerCase());
        if (subcommand == null) {
            sender.sendMessage("Unknown party subcommand: " + args[1]);
            return true;
        }

        return subcommand.execute(sender, args);
    }





}
