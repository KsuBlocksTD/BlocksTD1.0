package ksucapproj.blockstowerdefence1;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class UpgradeTowerCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player player) {
            int balance = Economy.getBalance(player);
            if (balance >= 50) {
                player.sendMessage("Tower Upgraded!");
                // Increase tower range or damage
                Economy.playerMoney.put(player, balance - 50);
            } else {
                player.sendMessage("Not enough money!");
            }
        }
        return true;
    }
}