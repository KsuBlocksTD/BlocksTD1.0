package ksucapproj.blockstowerdefense1.logic.game_logic;

import ksucapproj.blockstowerdefense1.logic.Economy;
import ksucapproj.blockstowerdefense1.maps.MapData;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class StartGame implements CommandExecutor, Listener {

    private final JavaPlugin plugin;
    private int currentRound = 1;
    private int zombiesPerRound = 5;
    private boolean isReady = false;
    private int zombiesKilled = 0;
    private int totalZombiesThisRound = 0;

    public StartGame(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("startgame")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Only players can start the game!");
                return true;
            }

            Player player = (Player) sender;
            Economy.playerJoin(player);

            World world = Bukkit.getWorld("world");
            Location startLocation = MapData.getStartLocation(world);
            player.teleport(startLocation);

            ItemStack towerEgg = new ItemStack(Material.ZOMBIE_SPAWN_EGG, 5);
            ItemMeta meta = towerEgg.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.AQUA + "Basic Tower");
                towerEgg.setItemMeta(meta);
            }
            player.getInventory().addItem(towerEgg);

            player.sendMessage(ChatColor.GREEN + "Type /readyup to start the first round!");
            return true;
        }
        else if (command.getName().equalsIgnoreCase("readyup")) {
            return onReadyUpCommand(sender);
        }
        return false;
    }

    public boolean onReadyUpCommand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can ready up!");
            return true;
        }

        Player player = (Player) sender;
        isReady = true;
        startRound(player);
        return true;
    }

    private void startRound(Player player) {
        if (!isReady) {
            player.sendMessage(ChatColor.RED + "Use /readyup first!");
            return;
        }

        player.sendMessage(ChatColor.GREEN + "Starting Round " + currentRound + " (" + zombiesPerRound + " zombies)");
        World world = player.getWorld();

        totalZombiesThisRound = zombiesPerRound;
        zombiesKilled = 0;

        new BukkitRunnable() {
            int spawned = 0;

            @Override
            public void run() {
                if (spawned >= totalZombiesThisRound) {
                    cancel();
                    return;
                }

                MobHandler.spawnMob(world);
                spawned++;
            }
        }.runTaskTimer(plugin, 0, 10); // 500ms interval (10 ticks)
    }

    @EventHandler
    public void onPlayerUseEgg(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item != null &&
                item.getType() == Material.ZOMBIE_SPAWN_EGG &&
                item.getItemMeta() != null &&
                "§bBasic Tower".equals(item.getItemMeta().getDisplayName())) {

            event.setCancelled(true); // Prevent zombie spawning

            Economy.getPlayerMoney(player);
            int coins = Integer.parseInt(Economy.getPlayerMoney(player));

            if (coins >= 500) {
                // Deduct coins and place tower
                Economy.addPlayerMoney(player, -500);
                item.setAmount(item.getAmount() - 1);

                // Get targeted block location
                Location placementLocation = event.getInteractionPoint();
                if (placementLocation == null) {
                    placementLocation = player.getLocation();
                }

                // Spawn tower

                SummonTower.spawnTower(placementLocation);
                player.sendMessage("§aTower placed successfully!");
            } else {
                player.sendMessage("§cYou need at least 500 coins to place a tower.");
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Zombie) {
            zombiesKilled++;

            if (zombiesKilled >= totalZombiesThisRound) {
                Bukkit.broadcastMessage(ChatColor.GOLD + "Round " + currentRound + " completed!");
                currentRound++;
                zombiesPerRound += (currentRound <= 10) ? 7 : 10;
                isReady = false;
                Bukkit.broadcastMessage(ChatColor.GREEN + "Type /readyup for Round " + currentRound);
            }
        }
    }
}