package me.matthewTest.pluginTest;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class multiCommandHandler implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if (!(sender instanceof Player)){
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        String cmd = command.getName().toLowerCase();

        switch(cmd){
            case "test" -> player.sendMessage("This is a test.");
            case "hello" -> Bukkit.broadcastMessage("Hello World!");
            default -> player.sendMessage("Unknown command.");
        }

        return false;
    }
}
