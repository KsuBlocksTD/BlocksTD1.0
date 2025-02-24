package me.matthewTest.pluginTest.commands;

import me.matthewTest.pluginTest.interfaces.MTDSubcommand;
import me.matthewTest.pluginTest.logic.teleportationLogic;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class hubCommand implements MTDSubcommand {

    //mtd hub | COMMAND ------------------------------------
    private final teleportationLogic tpManager;

    //constructer to use plugin and tpManager

    public hubCommand(JavaPlugin plugin){
        this.tpManager = new teleportationLogic(plugin);
    }

    //getName is just reading the name for the .yml and help to know what to call it
    @Override
    public String getName(){
        return "hub";
    }
    //gives the /... hub cmd a desc. for explanation for new users
    @Override
    public String getDescription() {
        return "Teleports you to the hub.";
    }
    //shows how to use the cmd properly
    @Override
    public String getUsage() {
        return "/mtd hub";
    }

    //actual function executing the code
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        //if the sender of the cmd isn't a player, don't send cmd
        //also creates the local player object as the sender
        if (!(sender instanceof Player player)) {
            //command does not work if caller of cmd is not a player
            sender.sendMessage("Only players can use this command.");
            return false;
        }
        //sends player confirmation msg
        player.sendMessage("Teleporting to the hub...");

        //creates target location for the player's request
        Location targetLocation = new Location(player.getWorld(), player.getX(), player.getY()+2, player.getZ());

        //passes the target location to the tpManager that employs the function with the teleport logic
        tpManager.teleportWithRetry(player, targetLocation, 3 );
        return true;
    }
}
