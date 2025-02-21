package me.matthewTest.pluginTest;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class partyCommand implements MTDSubcommand {

    @Override
    public String getName() {
        return "party";
    }

    @Override
    public String getDescription() {
        return "Manages your party.";
    }

    @Override
    public String getUsage() {
        return "/mtd party";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        player.sendMessage("Party management coming soon...");
        // Add your party logic here
        return true;
    }



}
