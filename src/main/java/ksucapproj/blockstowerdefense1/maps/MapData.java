package ksucapproj.blockstowerdefense1.maps;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MapData {
    // Store maps with their associated paths
    private static final Map<String, MapDetails> maps = new HashMap<>();
    private static String defaultMap; // Default map to use
    private static File configFile;
    private static JavaPlugin plugin;

    public static class MapDetails {
        // Map of path IDs to their waypoints
        private final Map<String, PathData> paths;

        public MapDetails() {
            this.paths = new HashMap<>();
        }

        // Get all path IDs
        public Set<String> getPathIds() {
            return new HashSet<>(paths.keySet());
        }

        // Get path data for a specific path
        public PathData getPathData(String pathId) {
            return paths.getOrDefault(pathId, null);
        }

        // Add a path with its starting point and waypoints
        public void addPath(String pathId, Location startLocation, List<Location> waypoints) {
            paths.put(pathId, new PathData(pathId, startLocation, waypoints, new ArrayList<>()));
        }

        // Add a branch from a path to another path at a specific waypoint index
        public boolean addBranchPoint(String sourcePathId, int waypointIndex, String targetPathId, float chance) {
            PathData sourcePath = paths.get(sourcePathId);
            if (sourcePath == null || !paths.containsKey(targetPathId)) {
                return false;
            }

            if (waypointIndex < 0 || waypointIndex >= sourcePath.getWaypoints().size()) {
                return false;
            }

            BranchPoint branchPoint = new BranchPoint(waypointIndex, targetPathId, chance);
            sourcePath.addBranchPoint(branchPoint);
            return true;
        }

        // Get waypoints for a specific path
        public List<Location> getWaypoints(String pathId) {
            PathData path = paths.get(pathId);
            if (path == null) {
                return new ArrayList<>();
            }
            return path.getWaypoints();
        }

        // Get all end locations (final waypoint of each path)
        public List<Location> getEndLocations() {
            List<Location> endLocations = new ArrayList<>();
            for (PathData path : paths.values()) {
                Location endLoc = path.getEndLocation();
                if (endLoc != null) {
                    endLocations.add(endLoc.clone());
                }
            }
            return endLocations;
        }

        // Get all start locations (one per path)
        public List<Location> getStartLocations() {
            List<Location> startLocations = new ArrayList<>();
            for (PathData path : paths.values()) {
                Location startLoc = path.getStartLocation();
                if (startLoc != null) {
                    startLocations.add(startLoc.clone());
                }
            }
            return startLocations;
        }

        // Get a random path ID
        public String getRandomPathId() {
            if (paths.isEmpty()) {
                return null;
            }
            List<String> pathIds = new ArrayList<>(paths.keySet());
            int randomIndex = new Random().nextInt(pathIds.size());
            return pathIds.get(randomIndex);
        }

        // Clear a specific path
        public boolean clearPath(String pathId) {
            PathData path = paths.get(pathId);
            if (path == null) {
                return false;
            }
            path.clearWaypoints();
            return true;
        }

        // Remove a path
        public boolean removePath(String pathId) {
            if (paths.size() <= 1) {
                return false; // Don't remove the last path
            }
            return paths.remove(pathId) != null;
        }

        // Update path start location
        public boolean updatePathStart(String pathId, Location startLocation) {
            PathData path = paths.get(pathId);
            if (path == null) {
                return false;
            }
            path.setStartLocation(startLocation.clone());
            return true;
        }
    }

    // Class to represent branch points in a path
    public static class BranchPoint {
        private final int waypointIndex;
        private final String targetPathId;
        private float branchChance; // Probability of taking this branch (0.0 - 1.0)

        public BranchPoint(int waypointIndex, String targetPathId) {
            this(waypointIndex, targetPathId, 0.5f); // Default 50% chance
        }

        public BranchPoint(int waypointIndex, String targetPathId, float branchChance) {
            this.waypointIndex = waypointIndex;
            this.targetPathId = targetPathId;
            this.branchChance = branchChance;
        }

        public int getWaypointIndex() {
            return waypointIndex;
        }

        public String getTargetPathId() {
            return targetPathId;
        }

        public float getBranchChance() {
            return branchChance;
        }

        public void setBranchChance(float chance) {
            this.branchChance = Math.max(0.0f, Math.min(1.0f, chance));
        }
    }

    // Class to represent a path with a start location, waypoints and branch points
    public static class PathData {
        private final String pathId;
        private Location startLocation;
        private final List<Location> waypoints;
        private final List<BranchPoint> branchPoints;

        public PathData(String pathId, Location startLocation, List<Location> waypoints, List<BranchPoint> branchPoints) {
            this.pathId = pathId;
            this.startLocation = startLocation != null ? startLocation.clone() : null;
            this.waypoints = new ArrayList<>();
            for (Location waypoint : waypoints) {
                this.waypoints.add(waypoint.clone());
            }
            this.branchPoints = branchPoints != null ? new ArrayList<>(branchPoints) : new ArrayList<>();
        }

        public String getPathId() {
            return pathId;
        }

        public Location getStartLocation() {
            return startLocation != null ? startLocation.clone() : null;
        }

        public void setStartLocation(Location location) {
            this.startLocation = location.clone();
        }

        public List<Location> getWaypoints() {
            List<Location> waypointsCopy = new ArrayList<>();
            for (Location waypoint : waypoints) {
                waypointsCopy.add(waypoint.clone());
            }
            return waypointsCopy;
        }

        public List<BranchPoint> getBranchPoints() {
            return new ArrayList<>(branchPoints);
        }

        public void addWaypoint(Location location) {
            waypoints.add(location.clone());
        }

        public void addBranchPoint(BranchPoint branchPoint) {
            branchPoints.add(branchPoint);
        }

        public void removeBranchPoint(int index) {
            if (index >= 0 && index < branchPoints.size()) {
                branchPoints.remove(index);
            }
        }

        public void clearWaypoints() {
            waypoints.clear();
            branchPoints.clear();
        }

        public Location getEndLocation() {
            if (waypoints.isEmpty()) {
                return null;
            }
            return waypoints.get(waypoints.size() - 1).clone();
        }
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
        defaultMap = config.getString("default-map", null);

        // Load all maps
        if (config.contains("maps")) {
            for (String mapId : config.getConfigurationSection("maps").getKeys(false)) {
                String mapPath = "maps." + mapId;
                MapDetails mapDetails = new MapDetails();

                // Get world for this map
                String worldName = config.getString(mapPath + ".world", "world");
                World world = plugin.getServer().getWorld(worldName);

                if (world != null) {
                    // Load paths
                    if (config.contains(mapPath + ".paths")) {
                        for (String pathId : config.getConfigurationSection(mapPath + ".paths").getKeys(false)) {
                            String pathPath = mapPath + ".paths." + pathId;

                            // Load start location for this path
                            Location startLocation = null;
                            if (config.contains(pathPath + ".startLocation")) {
                                double startX = config.getDouble(pathPath + ".startLocation.x");
                                double startY = config.getDouble(pathPath + ".startLocation.y");
                                double startZ = config.getDouble(pathPath + ".startLocation.z");
                                float yaw = (float) config.getDouble(pathPath + ".startLocation.yaw", 0);
                                float pitch = (float) config.getDouble(pathPath + ".startLocation.pitch", 0);
                                startLocation = new Location(world, startX, startY, startZ, yaw, pitch);
                            }

                            // Load waypoints for this path
                            List<Location> waypoints = new ArrayList<>();

                            if (config.contains(pathPath + ".waypoints")) {
                                for (String waypointKey : config.getConfigurationSection(pathPath + ".waypoints").getKeys(false)) {
                                    String wpPath = pathPath + ".waypoints." + waypointKey;

                                    double wpX = config.getDouble(wpPath + ".x");
                                    double wpY = config.getDouble(wpPath + ".y");
                                    double wpZ = config.getDouble(wpPath + ".z");
                                    float wpYaw = (float) config.getDouble(wpPath + ".yaw", 0);
                                    float wpPitch = (float) config.getDouble(wpPath + ".pitch", 0);

                                    waypoints.add(new Location(world, wpX, wpY, wpZ, wpYaw, wpPitch));
                                }
                            }

                            // Create path data
                            PathData pathData = new PathData(pathId, startLocation, waypoints, new ArrayList<>());

                            // Load branch points
                            if (config.contains(pathPath + ".branches")) {
                                for (String branchKey : config.getConfigurationSection(pathPath + ".branches").getKeys(false)) {
                                    String branchPath = pathPath + ".branches." + branchKey;

                                    int waypointIndex = config.getInt(branchPath + ".waypointIndex");
                                    String targetPathId = config.getString(branchPath + ".targetPath");
                                    float chance = (float) config.getDouble(branchPath + ".chance", 0.5);

                                    BranchPoint branchPoint = new BranchPoint(waypointIndex, targetPathId, chance);
                                    pathData.addBranchPoint(branchPoint);
                                }
                            }

                            // Add path to map
                            mapDetails.addPath(pathId, startLocation, pathData.getWaypoints());

                            // Add branch points after all paths are loaded
                            for (BranchPoint branch : pathData.getBranchPoints()) {
                                mapDetails.addBranchPoint(pathId, branch.getWaypointIndex(), branch.getTargetPathId(), branch.getBranchChance());
                            }
                        }
                    }

                    // Add map to our collection
                    maps.put(mapId, mapDetails);

                    // If this is the first map and no default is set, make it the default
                    if (defaultMap == null) {
                        defaultMap = mapId;
                    }
                }
            }
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
        if (defaultMap != null) {
            config.set("default-map", defaultMap);
        }

        // Save all maps
        for (Map.Entry<String, MapDetails> entry : maps.entrySet()) {
            String mapId = entry.getKey();
            MapDetails mapDetails = entry.getValue();

            // Get sample location to determine world
            Location sampleLoc = null;

            // Try to find any location to get the world
            for (String pathId : mapDetails.getPathIds()) {
                PathData path = mapDetails.getPathData(pathId);
                if (path.getStartLocation() != null) {
                    sampleLoc = path.getStartLocation();
                    break;
                } else if (!path.getWaypoints().isEmpty()) {
                    sampleLoc = path.getWaypoints().get(0);
                    break;
                }
            }

            if (sampleLoc != null && sampleLoc.getWorld() != null) {
                // Save world name
                config.set("maps." + mapId + ".world", sampleLoc.getWorld().getName());

                // Save paths
                for (String pathId : mapDetails.getPathIds()) {
                    PathData pathData = mapDetails.getPathData(pathId);

                    // Save start location
                    Location startLoc = pathData.getStartLocation();
                    if (startLoc != null) {
                        config.set("maps." + mapId + ".paths." + pathId + ".startLocation.x", startLoc.getX());
                        config.set("maps." + mapId + ".paths." + pathId + ".startLocation.y", startLoc.getY());
                        config.set("maps." + mapId + ".paths." + pathId + ".startLocation.z", startLoc.getZ());
                        config.set("maps." + mapId + ".paths." + pathId + ".startLocation.yaw", startLoc.getYaw());
                        config.set("maps." + mapId + ".paths." + pathId + ".startLocation.pitch", startLoc.getPitch());
                    }

                    // Save waypoints
                    List<Location> waypoints = pathData.getWaypoints();
                    for (int i = 0; i < waypoints.size(); i++) {
                        Location wp = waypoints.get(i);
                        String wpKey = "wp" + (i + 1);

                        config.set("maps." + mapId + ".paths." + pathId + ".waypoints." + wpKey + ".x", wp.getX());
                        config.set("maps." + mapId + ".paths." + pathId + ".waypoints." + wpKey + ".y", wp.getY());
                        config.set("maps." + mapId + ".paths." + pathId + ".waypoints." + wpKey + ".z", wp.getZ());
                        config.set("maps." + mapId + ".paths." + pathId + ".waypoints." + wpKey + ".yaw", wp.getYaw());
                        config.set("maps." + mapId + ".paths." + pathId + ".waypoints." + wpKey + ".pitch", wp.getPitch());
                    }

                    // Save branch points
                    List<BranchPoint> branchPoints = pathData.getBranchPoints();
                    for (int i = 0; i < branchPoints.size(); i++) {
                        BranchPoint branch = branchPoints.get(i);
                        String branchKey = "branch" + (i + 1);

                        config.set("maps." + mapId + ".paths." + pathId + ".branches." + branchKey + ".waypointIndex", branch.getWaypointIndex());
                        config.set("maps." + mapId + ".paths." + pathId + ".branches." + branchKey + ".targetPath", branch.getTargetPathId());
                        config.set("maps." + mapId + ".paths." + pathId + ".branches." + branchKey + ".chance", branch.getBranchChance());
                    }
                }
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

    // Check if a map exists
    public static boolean mapExists(String mapId) {
        return maps.containsKey(mapId);
    }

    // Create a new empty map with the given ID
    public static boolean createMap(String mapId) {
        if (maps.containsKey(mapId)) {
            return false; // Map already exists
        }

        maps.put(mapId, new MapDetails());

        // If this is the first map, make it the default
        if (defaultMap == null) {
            defaultMap = mapId;
        }

        saveMaps();
        return true;
    }

    // Delete a map by ID
    public static boolean deleteMap(String mapId) {
        if (!maps.containsKey(mapId)) {
            return false; // Map doesn't exist
        }

        // Can't delete the default map if it's the only one
        if (mapId.equals(defaultMap) && maps.size() == 1) {
            return false;
        }

        // If deleting the default map, choose another as default
        if (mapId.equals(defaultMap)) {
            for (String otherId : maps.keySet()) {
                if (!otherId.equals(mapId)) {
                    defaultMap = otherId;
                    break;
                }
            }
        }

        maps.remove(mapId);
        saveMaps();
        return true;
    }

    // Create a new path with start location in a map
    public static boolean createPath(String mapId, String pathId, Location startLocation) {
        MapDetails map = maps.get(mapId);
        if (map == null) {
            return false; // Map doesn't exist
        }

        // Check if path already exists
        if (map.getPathData(pathId) != null) {
            return false;
        }

        map.addPath(pathId, startLocation.clone(), new ArrayList<>());
        saveMaps();
        return true;
    }

    // Add a waypoint to a specific path in a map
    public static boolean addWaypoint(String mapId, String pathId, Location waypoint) {
        MapDetails map = maps.get(mapId);
        if (map == null) {
            return false; // Map doesn't exist
        }

        PathData path = map.getPathData(pathId);
        if (path == null) {
            return false; // Path doesn't exist
        }

        // Add waypoint to path
        path.addWaypoint(waypoint.clone());
        saveMaps();
        return true;
    }

    // Add a branch point to a path
    public static boolean addBranchPoint(String mapId, String sourcePathId, int waypointIndex, String targetPathId, float chance) {
        MapDetails map = maps.get(mapId);
        if (map == null) {
            return false; // Map doesn't exist
        }

        if (map.addBranchPoint(sourcePathId, waypointIndex, targetPathId, chance)) {
            saveMaps();
            return true;
        }
        return false;
    }

    // Clear all waypoints from a specific path
    public static boolean clearPath(String mapId, String pathId) {
        MapDetails map = maps.get(mapId);
        if (map == null) {
            return false; // Map doesn't exist
        }

        if (map.clearPath(pathId)) {
            saveMaps();
            return true;
        }
        return false;
    }

    // Remove a path from a map
    public static boolean removePath(String mapId, String pathId) {
        MapDetails map = maps.get(mapId);
        if (map == null) {
            return false; // Map doesn't exist
        }

        if (map.removePath(pathId)) {
            saveMaps();
            return true;
        }
        return false;
    }

    // Update the start location for a path
    public static boolean updatePathStart(String mapId, String pathId, Location startLocation) {
        MapDetails map = maps.get(mapId);
        if (map == null) {
            return false; // Map doesn't exist
        }

        if (map.updatePathStart(pathId, startLocation)) {
            saveMaps();
            return true;
        }
        return false;
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
        return maps.get(mapId);
    }

    // Get default map details
    public static MapDetails getDefaultMap() {
        return defaultMap != null ? maps.get(defaultMap) : null;
    }

    // Get the number of waypoints in a specific path of a map
    public static int getWaypointCount(String mapId, String pathId) {
        MapDetails map = maps.get(mapId);
        if (map == null) {
            return 0;
        }

        List<Location> waypoints = map.getWaypoints(pathId);
        return waypoints != null ? waypoints.size() : 0;
    }

    // Get all path IDs for a map
    public static Set<String> getPathIds(String mapId) {
        MapDetails map = maps.get(mapId);
        if (map == null) {
            return new HashSet<>();
        }
        return map.getPathIds();
    }

    // Get all end locations for a map
    public static List<Location> getEndLocations(World world, String mapId) {
        MapDetails map = maps.get(mapId);
        if (map == null) {
            return new ArrayList<>();
        }

        List<Location> endLocations = map.getEndLocations();
        for (Location loc : endLocations) {
            loc.setWorld(world); // Update the world
        }

        return endLocations;
    }

    // Get end location for a specific path
    public static Location getEndLocation(World world, String mapId, String pathId) {
        MapDetails map = maps.get(mapId);
        if (map == null) {
            return null;
        }

        PathData path = map.getPathData(pathId);
        if (path == null || path.getWaypoints().isEmpty()) {
            return null;
        }

        Location endLoc = path.getEndLocation();
        if (endLoc != null) {
            endLoc.setWorld(world); // Update the world
        }

        return endLoc;
    }

    // Get start location for a specific path
    public static Location getPathStartLocation(World world, String mapId, String pathId) {
        MapDetails map = maps.get(mapId);
        if (map == null) {
            return null;
        }

        PathData path = map.getPathData(pathId);
        if (path == null) {
            return null;
        }

        Location startLoc = path.getStartLocation();
        if (startLoc != null) {
            startLoc.setWorld(world); // Update the world
        }

        return startLoc;
    }

    // Get a random path ID for a mob to follow
    public static String getRandomPathId(String mapId) {
        MapDetails map = maps.get(mapId);
        if (map == null && defaultMap != null) {
            map = maps.get(defaultMap); // Use default map if requested map doesn't exist
        }

        return map != null ? map.getRandomPathId() : null;
    }

    // Check if a path has branch points
    public static boolean hasPathBranches(String mapId, String pathId) {
        MapDetails map = maps.get(mapId);
        if (map == null) {
            return false;
        }

        PathData path = map.getPathData(pathId);
        return path != null && !path.getBranchPoints().isEmpty();
    }

    // Get all branch points for a path
    public static List<BranchPoint> getPathBranches(String mapId, String pathId) {
        MapDetails map = maps.get(mapId);
        if (map == null) {
            return new ArrayList<>();
        }

        PathData path = map.getPathData(pathId);
        return path != null ? path.getBranchPoints() : new ArrayList<>();
    }

    // Get waypoints for a specific path
    public static List<Location> getWaypoints(World world, String mapId, String pathId) {
        MapDetails map = maps.get(mapId);
        if (map == null && defaultMap != null) {
            map = maps.get(defaultMap); // Use default map if requested map doesn't exist
        }

        if (map == null) {
            return new ArrayList<>();
        }

        List<Location> waypoints = map.getWaypoints(pathId);
        for (Location waypoint : waypoints) {
            waypoint.setWorld(world); // Update the world
        }

        return waypoints;
    }
}