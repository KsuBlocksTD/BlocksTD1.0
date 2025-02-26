package me.matthewTest.pluginTest.logic;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


//this has been relegated to a test class and is not meant to be used or worked in
//only has functional ties to the test cmds /test and /hello
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
