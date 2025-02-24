package me.matthewTest.pluginTest.commands;

import me.matthewTest.pluginTest.interfaces.PARTYSubcommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

public class invitePartyCommand implements PARTYSubcommand {


    @Override
    public String getName() {
        return "invite";
    }

    @Override
    public String getDescription() {
        return "Invite players to your party.";
    }

    @Override
    public String getUsage() {
        return "/mtd party invite <player>";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        //if the sender of the cmd isn't a player, don't send cmd
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("Usage: " + getUsage());
            return true;
        }

        //searches for player in the server
        Player target = Bukkit.getPlayer(args[2]);
        if (target == null) {
            sender.sendMessage("Player not found: " + args[2]);
            return true;
        }

        //not really working party functionality... this is just a test implementation for the time being
        sender.sendMessage("Invited " + target.getName() + " to your party!");
        target.sendMessage("You have been invited to a party by " + sender.getName() + "!");
        return true;
    }
}
