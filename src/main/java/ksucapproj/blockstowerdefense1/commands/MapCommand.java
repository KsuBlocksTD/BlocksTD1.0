package ksucapproj.blockstowerdefense1.commands;

import ksucapproj.blockstowerdefense1.maps.MapData;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static ksucapproj.blockstowerdefense1.maps.MapData.*;

public class MapCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        // Check permission
        if (!player.hasPermission("towerdefense.map")) {
            player.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        // No arguments, show help
        if (args.length == 0) {
            showHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "list":
                // List all available maps
                listMaps(player);
                break;

            case "create":
                // Create a new map with player's current location as start
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /tdmap create <map_id>");
                    return true;
                }
                createMap(player, args[1]);
                break;

            case "delete":
                // Delete a map
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /tdmap delete <map_id>");
                    return true;
                }
                deleteMap(player, args[1]);
                break;

            case "setdefault":
                // Set default map
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /tdmap setdefault <map_id>");
                    return true;
                }
                setDefaultMap(player, args[1]);
                break;

            case "addwp":
                // Add waypoint to a map
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /tdmap addwp <map_id>");
                    return true;
                }
                addWaypoint(player, args[1]);
                break;

            case "clearwp":
                // Clear all waypoints from a map
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /tdmap clearwp <map_id>");
                    return true;
                }
                clearWaypoints(player, args[1]);
                break;

            case "info":
                // Show map info
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /tdmap info <map_id>");
                    return true;
                }
                showMapInfo(player, args[1]);
                break;

            default:
                showHelp(player);
                break;
        }

        return true;
    }

    // Show command help
    private void showHelp(Player player) {
        player.sendMessage("§6==== Tower Defense Map Commands ====");
        player.sendMessage("§e/tdmap list §7- List all available maps");
        player.sendMessage("§e/tdmap create <map_id> §7- Create a new map using your location as start");
        player.sendMessage("§e/tdmap delete <map_id> §7- Delete a map");
        player.sendMessage("§e/tdmap setdefault <map_id> §7- Set default map");
        player.sendMessage("§e/tdmap addwp <map_id> §7- Add waypoint to map at your location");
        player.sendMessage("§e/tdmap clearwp <map_id> §7- Clear all waypoints from map");
        player.sendMessage("§e/tdmap info <map_id> §7- Show map information");
    }



    // List all available maps
    private void listMaps(Player player) {
        List<String> mapIds = getAvailableMaps();
        player.sendMessage("§6Available maps (" + mapIds.size() + "):");

    }

    // Create a new map
    private void createMap(Player player, String mapId) {
        // Check if map ID is valid
        if (!mapId.matches("^[a-zA-Z0-9_-]+$")) {
            player.sendMessage("§cInvalid map ID. Use only letters, numbers, underscores, and hyphens.");
            return;
        }

        // Try to create the map
        if (MapData.createMap(mapId, player.getLocation())) {
            player.sendMessage("§aCreated map §e" + mapId + " §awith start at your location.");
        } else {
            player.sendMessage("§cA map with ID §e" + mapId + " §calready exists.");
        }
    }

    // Delete a map
    private void deleteMap(Player player, String mapId) {
        if (MapData.deleteMap(mapId)) {
            player.sendMessage("§aDeleted map §e" + mapId + "§a.");
        }
        else {
                player.sendMessage("§cMap §e" + mapId + " §cdoes not exist.");
            }

    }

    // Set default map
    private void setDefaultMap(Player player, String mapId) {
        if (MapData.setDefaultMap(mapId)) {
            player.sendMessage("§aSet §e" + mapId + " §aas the default map.");
        } else {
            player.sendMessage("§cMap §e" + mapId + " §cdoes not exist.");
        }
    }

    // Add waypoint to a map
    private void addWaypoint(Player player, String mapId) {
        if (MapData.addWaypoint(mapId, player.getLocation())) {
            int waypointCount = getWaypointCount(mapId);
            player.sendMessage("§aAdded waypoint #" + waypointCount + " to map §e" + mapId + " §aat your location.");
        } else {
            player.sendMessage("§cMap §e" + mapId + " §cdoes not exist.");
        }
    }

    // Clear all waypoints from a map
    private void clearWaypoints(Player player, String mapId) {
        if (MapData.clearWaypoints(mapId)) {
            player.sendMessage("§aCleared all waypoints from map §e" + mapId + "§a.");
        } else {
            player.sendMessage("§cMap §e" + mapId + " §cdoes not exist.");
        }
    }



    // Show map info
    private void showMapInfo(Player player, String mapId) {
        MapData.MapDetails map = MapData.getMap(mapId);
        if (map == null) {
            player.sendMessage("§cMap §e" + mapId + " §cdoes not exist.");
            return;
        }



        Location start = map.getStartLocation();
        List<Location> waypoints = map.getWaypoints();

        player.sendMessage("§6==== Map: " + mapId + " ====");
        player.sendMessage("§eStart: §7X: " + formatCoordinate(start.getX()) +
                ", Y: " + formatCoordinate(start.getY()) +
                ", Z: " + formatCoordinate(start.getZ()));
        player.sendMessage("§eWaypoints: §7" + waypoints.size());

        for (int i = 0; i < waypoints.size(); i++) {
            Location wp = waypoints.get(i);
            player.sendMessage("§e  " + (i + 1) + ": §7X: " + formatCoordinate(wp.getX()) +
                    ", Y: " + formatCoordinate(wp.getY()) +
                    ", Z: " + formatCoordinate(wp.getZ()));
        }
    }

    private String formatCoordinate(double coord) {
        return String.format("%.2f", coord);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            // First argument - subcommands
            List<String> subCommands = Arrays.asList("list", "create", "delete", "setdefault", "addwp", "clearwp", "info");
            return filterStartingWith(subCommands, args[0]);
        } else if (args.length == 2) {
            // Second argument - map ID for all except create
            if (!args[0].equalsIgnoreCase("create")) {
                return filterStartingWith(getAvailableMaps(), args[1]);
            }
        }

        return new ArrayList<>();
    }

    private List<String> filterStartingWith(List<String> list, String prefix) {
        return list.stream()
                .filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase()))
                .collect(Collectors.toList());
    }
}
