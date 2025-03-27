package ksucapproj.blockstowerdefense1.maps;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class MapData {
    public static boolean mapExists(String mapId) {
        return maps.containsKey(mapId);
    }
    // Store maps with their associated waypoints
    private static final Map<String, MapDetails> maps = new HashMap<>();
    private static String defaultMap = "map1"; // Default map to use
    private static File configFile;
    private static JavaPlugin plugin;

    // Map details class to store start location and waypoints
    public static class MapDetails {
        private final Location startLocation;
        private final List<Location> waypoints;

        public MapDetails(Location startLocation, List<Location> waypoints) {
            this.startLocation = startLocation;
            this.waypoints = waypoints;
        }

        public Location getStartLocation() {
            return startLocation.clone();
        }

        public List<Location> getWaypoints() {
            // Return a deep copy of waypoints to prevent modifications
            List<Location> waypointsCopy = new ArrayList<>();
            for (Location waypoint : waypoints) {
                waypointsCopy.add(waypoint.clone());
            }
            return waypointsCopy;
        }

        public Location getEndLocation() {
            if (waypoints.isEmpty()) {
                return startLocation.clone();
            }
            return waypoints.get(waypoints.size() - 1).clone();
        }

        // Add a waypoint to this map
        public void addWaypoint(Location waypoint) {
            waypoints.add(waypoint.clone());
        }

        // Clear all waypoints
        public void clearWaypoints() {
            waypoints.clear();
        }
    }

    // Initialize maps with default values
    static {
        // This will be called when the class is loaded
        initDefaultMaps();
    }

    // Initialize maps with some default values
    private static void initDefaultMaps() {
        // We'll add a default implementation that matches your original code
        // This will be used as a fallback if no config file exists
    }

    // Load maps from configuration file
    public static void loadMaps(JavaPlugin pluginInstance) {
        // Store plugin reference
        plugin = pluginInstance;

        // Clear existing maps
        maps.clear();

        // Create config if it doesn't exist
        configFile = new File(plugin.getDataFolder(), "maps.yml");
        if (!configFile.exists()) {
            plugin.saveResource("maps.yml", false);
        }

        // Load config
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Get default map
        defaultMap = config.getString("default-map", "map1");

        // Load all maps
        if (config.contains("maps")) {
            for (String mapId : config.getConfigurationSection("maps").getKeys(false)) {
                String mapPath = "maps." + mapId;

                // Process this map only if it has start section
                if (config.contains(mapPath + ".start")) {
                    // Get world for this map
                    String worldName = config.getString(mapPath + ".world", "world");
                    World world = plugin.getServer().getWorld(worldName);

                    if (world != null) {
                        // Load start location
                        double startX = config.getDouble(mapPath + ".start.x");
                        double startY = config.getDouble(mapPath + ".start.y");
                        double startZ = config.getDouble(mapPath + ".start.z");
                        float yaw = (float) config.getDouble(mapPath + ".start.yaw", 0);
                        float pitch = (float) config.getDouble(mapPath + ".start.pitch", 0);
                        Location startLoc = new Location(world, startX, startY, startZ, yaw, pitch);

                        // Load waypoints
                        List<Location> waypoints = new ArrayList<>();

                        // Check if waypoints section exists
                        if (config.contains(mapPath + ".waypoints")) {
                            for (String waypointKey : config.getConfigurationSection(mapPath + ".waypoints").getKeys(false)) {
                                String wpPath = mapPath + ".waypoints." + waypointKey;

                                // Load waypoint from config (absolute coordinates)
                                if (config.contains(wpPath + ".x") && config.contains(wpPath + ".y") && config.contains(wpPath + ".z")) {
                                    double wpX = config.getDouble(wpPath + ".x");
                                    double wpY = config.getDouble(wpPath + ".y");
                                    double wpZ = config.getDouble(wpPath + ".z");
                                    float wpYaw = (float) config.getDouble(wpPath + ".yaw", 0);
                                    float wpPitch = (float) config.getDouble(wpPath + ".pitch", 0);
                                    waypoints.add(new Location(world, wpX, wpY, wpZ, wpYaw, wpPitch));
                                }
                                // Load waypoint as relative offset to start
                                else if (config.contains(wpPath + ".offsetX") && config.contains(wpPath + ".offsetY") && config.contains(wpPath + ".offsetZ")) {
                                    double offsetX = config.getDouble(wpPath + ".offsetX");
                                    double offsetY = config.getDouble(wpPath + ".offsetY");
                                    double offsetZ = config.getDouble(wpPath + ".offsetZ");
                                    waypoints.add(startLoc.clone().add(offsetX, offsetY, offsetZ));
                                }
                            }
                        }

                        // Add map to our collection
                        maps.put(mapId, new MapDetails(startLoc, waypoints));
                    }
                }
            }
        }

        // If no maps were loaded, initialize with defaults
        if (maps.isEmpty()) {
            initDefaultMapsWithWorld(plugin.getServer().getWorlds().get(0));
        }
    }

    // Save maps to configuration file
    public static void saveMaps() {
        if (configFile == null || plugin == null) {
            return;
        }

        // Create new configuration
        FileConfiguration config = new YamlConfiguration();

        // Set default map
        config.set("default-map", defaultMap);

        // Save all maps
        for (Map.Entry<String, MapDetails> entry : maps.entrySet()) {
            String mapId = entry.getKey();
            MapDetails mapDetails = entry.getValue();

            // Save start location
            Location start = mapDetails.getStartLocation();
            config.set("maps." + mapId + ".world", start.getWorld().getName());
            config.set("maps." + mapId + ".start.x", start.getX());
            config.set("maps." + mapId + ".start.y", start.getY());
            config.set("maps." + mapId + ".start.z", start.getZ());
            config.set("maps." + mapId + ".start.yaw", start.getYaw());
            config.set("maps." + mapId + ".start.pitch", start.getPitch());

            // Save waypoints
            List<Location> waypoints = mapDetails.getWaypoints();
            for (int i = 0; i < waypoints.size(); i++) {
                Location wp = waypoints.get(i);
                String wpKey = "wp" + (i + 1);

                config.set("maps." + mapId + ".waypoints." + wpKey + ".x", wp.getX());
                config.set("maps." + mapId + ".waypoints." + wpKey + ".y", wp.getY());
                config.set("maps." + mapId + ".waypoints." + wpKey + ".z", wp.getZ());
                config.set("maps." + mapId + ".waypoints." + wpKey + ".yaw", wp.getYaw());
                config.set("maps." + mapId + ".waypoints." + wpKey + ".pitch", wp.getPitch());
            }
        }

        // Save configuration to file
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save maps configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Generate default configuration file
    public static void saveDefaultConfig(JavaPlugin plugin) {
        File configFile = new File(plugin.getDataFolder(), "maps.yml");
        if (!configFile.exists()) {
            // Create the parent directories if they don't exist
            if (!configFile.getParentFile().exists()) {
                configFile.getParentFile().mkdirs();
            }

            // Create default configuration
            FileConfiguration config = new YamlConfiguration();

            // Set default map
            config.set("default-map", "map1");

            // Add map1 (based on your original hardcoded values)
            config.set("maps.map1.world", "world");
            config.set("maps.map1.start.x", 69.5);
            config.set("maps.map1.start.y", 64);
            config.set("maps.map1.start.z", -585.5);

            // Add waypoints using offsets for clarity
            config.set("maps.map1.waypoints.wp1.offsetX", 0);
            config.set("maps.map1.waypoints.wp1.offsetY", 0);
            config.set("maps.map1.waypoints.wp1.offsetZ", 23);

            config.set("maps.map1.waypoints.wp2.offsetX", 8);
            config.set("maps.map1.waypoints.wp2.offsetY", 0);
            config.set("maps.map1.waypoints.wp2.offsetZ", 23);

            config.set("maps.map1.waypoints.wp3.offsetX", 8);
            config.set("maps.map1.waypoints.wp3.offsetY", 0);
            config.set("maps.map1.waypoints.wp3.offsetZ", 14);

            config.set("maps.map1.waypoints.wp4.offsetX", -8);
            config.set("maps.map1.waypoints.wp4.offsetY", 0);
            config.set("maps.map1.waypoints.wp4.offsetZ", 14);

            config.set("maps.map1.waypoints.wp5.offsetX", -8);
            config.set("maps.map1.waypoints.wp5.offsetY", 0);
            config.set("maps.map1.waypoints.wp5.offsetZ", 8);

            config.set("maps.map1.waypoints.wp6.offsetX", -14);
            config.set("maps.map1.waypoints.wp6.offsetY", 0);
            config.set("maps.map1.waypoints.wp6.offsetZ", 8);

            try {
                config.save(configFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Initialize with hard-coded defaults for a specific world (fallback)
    private static void initDefaultMapsWithWorld(World world) {
        // Map 1 - same as your original
        Location map1Start = new Location(world, 69.5, 64, -585.5);
        List<Location> map1Waypoints = new ArrayList<>();
        map1Waypoints.add(map1Start.clone().add(0, 0, 22));
        map1Waypoints.add(map1Start.clone().add(8, 0, 22));
        map1Waypoints.add(map1Start.clone().add(8, 0, 13));
        map1Waypoints.add(map1Start.clone().add(-8, 0, 13));
        map1Waypoints.add(map1Start.clone().add(-8, 0, 7));
        map1Waypoints.add(map1Start.clone().add(-14, 0, 7));

        maps.put("map1", new MapDetails(map1Start, map1Waypoints));
    }

    // Create a new map with the given ID and start location
    public static boolean createMap(String mapId, Location startLocation) {
        if (maps.containsKey(mapId)) {
            return false; // Map already exists
        }

        maps.put(mapId, new MapDetails(startLocation.clone(), new ArrayList<>()));
        saveMaps();
        return true;
    }

    // Delete a map by ID
    public static boolean deleteMap(String mapId) {
        if (!maps.containsKey(mapId) || mapId.equals(defaultMap)) {
            return false; // Map doesn't exist or is the default map
        }

        maps.remove(mapId);
        saveMaps();
        return true;
    }

    // Add a waypoint to a map
    public static boolean addWaypoint(String mapId, Location waypoint) {
        MapDetails map = maps.get(mapId);
        if (map == null) {
            return false; // Map doesn't exist
        }

        map.addWaypoint(waypoint.clone());
        saveMaps();
        return true;
    }

    // Clear all waypoints from a map
    public static boolean clearWaypoints(String mapId) {
        MapDetails map = maps.get(mapId);
        if (map == null) {
            return false; // Map doesn't exist
        }

        map.clearWaypoints();
        saveMaps();
        return true;
    }

    // Get available map IDs
    public static List<String> getAvailableMaps() {
        return new ArrayList<>(maps.keySet());
    }

    // Set the default map
    public static boolean setDefaultMap(String mapId) {
        if (!maps.containsKey(mapId)) {
            return false; // Map doesn't exist
        }

        defaultMap = mapId;
        saveMaps();
        return true;
    }

    // Get map details by ID
    public static MapDetails getMap(String mapId) {
        return maps.getOrDefault(mapId, maps.get(defaultMap));
    }

    // Get default map details
    public static MapDetails getDefaultMap() {
        return maps.get(defaultMap);
    }

    // Get the number of waypoints in a map
    public static int getWaypointCount(String mapId) {
        MapDetails map = maps.get(mapId);
        if (map == null) {
            return 0;
        }
        return map.getWaypoints().size();
    }

    // The original methods can now delegate to our data structure
    public static Location getStartLocation(World world) {
        return getStartLocation(world, defaultMap);
    }

    public static Location getStartLocation(World world, String mapId) {
        MapDetails map = maps.get(mapId);
        if (map == null) {
            // If requested map doesn't exist, use default
            return getDefaultMap().getStartLocation();
        }
        // Update the world of the location
        Location loc = map.getStartLocation();
        loc.setWorld(world);
        return loc;
    }

    public static List<Location> getWaypoints(World world) {
        return getWaypoints(world, defaultMap);
    }

    public static List<Location> getWaypoints(World world, String mapId) {
        MapDetails map = maps.get(mapId);
        if (map == null) {
            // If requested map doesn't exist, use default
            return getDefaultMap().getWaypoints();
        }

        // Update all waypoint worlds
        List<Location> waypoints = map.getWaypoints();
        for (Location waypoint : waypoints) {
            waypoint.setWorld(world);
        }

        return waypoints;
    }

    public static Location getEndLocation(World world) {
        return getEndLocation(world, defaultMap);
    }

    public static Location getEndLocation(World world, String mapId) {
        MapDetails map = maps.get(mapId);
        if (map == null) {
            // If requested map doesn't exist, use default
            return getDefaultMap().getEndLocation();
        }

        // Get the end location and update its world
        Location endLoc = map.getEndLocation();
        endLoc.setWorld(world);
        return endLoc;
    }

    // Command executor for map management
    public static class MapCommand implements CommandExecutor, TabCompleter {
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

            for (String mapId : mapIds) {
                String isDefault = mapId.equals(defaultMap) ? " §a(default)" : "";
                player.sendMessage("§e- " + mapId + isDefault + " §7(" + getWaypointCount(mapId) + " waypoints)");
            }
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
            } else {
                if (mapId.equals(defaultMap)) {
                    player.sendMessage("§cCannot delete the default map. Set another map as default first.");
                } else {
                    player.sendMessage("§cMap §e" + mapId + " §cdoes not exist.");
                }
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
                int waypointCount = MapData.getWaypointCount(mapId);
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
            MapDetails map = MapData.getMap(mapId);
            if (map == null) {
                player.sendMessage("§cMap §e" + mapId + " §cdoes not exist.");
                return;
            }

            Location start = map.getStartLocation();
            List<Location> waypoints = map.getWaypoints();

            player.sendMessage("§6==== Map: " + mapId + " ====");
            player.sendMessage("§eDefault: " + (mapId.equals(defaultMap) ? "§aYes" : "§cNo"));
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
}