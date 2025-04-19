package ksucapproj.blockstowerdefense1.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import ksucapproj.blockstowerdefense1.maps.MapData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

// this class contains several commands, check each command's build to see its usage

public class MapCommand {

    @NullMarked
    public static LiteralCommandNode<CommandSourceStack> mapCommand() {
        return Commands.literal("tdmap")
                .requires(ctx -> ctx.getExecutor() instanceof Player)

                // List maps
                .then(Commands.literal("list")
                        .executes(MapCommand::executeListMaps)
                )

                // Create map
                .then(Commands.literal("create")
                        .then(Commands.argument("map_id", StringArgumentType.word())
                                .executes(MapCommand::executeCreateMap)
                        )
                )

                // Delete map
                .then(Commands.literal("delete")
                        .then(Commands.argument("map_id", StringArgumentType.word())
                                .suggests(MapCommand::getAvailableMapsSuggestions)
                                .executes(MapCommand::executeDeleteMap)
                        )
                )

                // Set default map
                .then(Commands.literal("setdefault")
                        .then(Commands.argument("map_id", StringArgumentType.word())
                                .suggests(MapCommand::getAvailableMapsSuggestions)
                                .executes(MapCommand::executeSetDefaultMap)
                        )
                )

                // Create a new path with starting point
                .then(Commands.literal("createpath")
                        .then(Commands.argument("map_id", StringArgumentType.word())
                                .suggests(MapCommand::getAvailableMapsSuggestions)
                                .then(Commands.argument("path_id", StringArgumentType.word())
                                        .executes(MapCommand::executeCreatePath)
                                )
                        )
                )

                // Add waypoint to a specific path
                .then(Commands.literal("addwp")
                        .then(Commands.argument("map_id", StringArgumentType.word())
                                .suggests(MapCommand::getAvailableMapsSuggestions)
                                .then(Commands.argument("path_id", StringArgumentType.word())
                                        .suggests(MapCommand::getAvailablePathsSuggestions)
                                        .executes(MapCommand::executeAddWaypointToPath)
                                )
                        )
                )

                // Remove a path
                .then(Commands.literal("removepath")
                        .then(Commands.argument("map_id", StringArgumentType.word())
                                .suggests(MapCommand::getAvailableMapsSuggestions)
                                .then(Commands.argument("path_id", StringArgumentType.word())
                                        .suggests(MapCommand::getAvailablePathsSuggestions)
                                        .executes(MapCommand::executeRemovePath)
                                )
                        )
                )

                // Clear waypoints from a specific path
                .then(Commands.literal("clearwp")
                        .then(Commands.argument("map_id", StringArgumentType.word())
                                .suggests(MapCommand::getAvailableMapsSuggestions)
                                .then(Commands.argument("path_id", StringArgumentType.word())
                                        .suggests(MapCommand::getAvailablePathsSuggestions)
                                        .executes(MapCommand::executeClearSpecificPath)
                                )
                        )
                )

                // List all paths for a map
                .then(Commands.literal("paths")
                        .then(Commands.argument("map_id", StringArgumentType.word())
                                .suggests(MapCommand::getAvailableMapsSuggestions)
                                .executes(MapCommand::executeListPaths)
                        )
                )

                // Show path info
                .then(Commands.literal("pathinfo")
                        .then(Commands.argument("map_id", StringArgumentType.word())
                                .suggests(MapCommand::getAvailableMapsSuggestions)
                                .then(Commands.argument("path_id", StringArgumentType.word())
                                        .suggests(MapCommand::getAvailablePathsSuggestions)
                                        .executes(MapCommand::executeShowPathInfo)
                                )
                        )
                )

                // Show map info
                .then(Commands.literal("info")
                        .then(Commands.argument("map_id", StringArgumentType.word())
                                .suggests(MapCommand::getAvailableMapsSuggestions)
                                .executes(MapCommand::executeShowMapInfo)
                        )
                )

                // Default help command
                .executes(MapCommand::showHelp)
                .build();
    }

    // Suggestion provider for map IDs
    private static CompletableFuture<Suggestions> getAvailableMapsSuggestions(
            final CommandContext<CommandSourceStack> ctx,
            final SuggestionsBuilder builder) {
        MapData.getAvailableMaps().forEach(builder::suggest);
        return builder.buildFuture();
    }

    // Suggestion provider for path IDs for a specific map
    private static CompletableFuture<Suggestions> getAvailablePathsSuggestions(
            final CommandContext<CommandSourceStack> ctx,
            final SuggestionsBuilder builder) {
        try {
            String mapId = StringArgumentType.getString(ctx, "map_id");
            Set<String> pathIds = MapData.getPathIds(mapId);
            pathIds.forEach(builder::suggest);
        } catch (Exception e) {
            // If map_id is not found, suggest nothing
        }
        return builder.buildFuture();
    }

    // List Maps
    private static int executeListMaps(final CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getExecutor() instanceof Player player)) {
            return 1;
        }

        List<String> mapIds = MapData.getAvailableMaps();
        player.sendMessage(Component.text("Available maps (" + mapIds.size() + "):", NamedTextColor.GOLD));
        mapIds.forEach(mapId -> player.sendMessage(Component.text("- " + mapId, NamedTextColor.YELLOW)));
        return 1;
    }

    // Create Map (no starting point)
    private static int executeCreateMap(final CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getExecutor() instanceof Player player)) {
            return 1;
        }

        String mapId = StringArgumentType.getString(ctx, "map_id");

        // Validate map ID
        if (!mapId.matches("^[a-zA-Z0-9_-]+$")) {
            player.sendMessage(Component.text("Invalid map ID. Use only letters, numbers, underscores, and hyphens.", NamedTextColor.RED));
            return 1;
        }

        // Try to create the map
        if (MapData.createMap(mapId)) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(
                    "Created map <yellow>" + mapId + "</yellow>. Use '/tdmap createpath " + mapId + " <path_id>' to add paths."
            ));
        } else {
            player.sendMessage(MiniMessage.miniMessage().deserialize(
                    "A map with ID <yellow>" + mapId + "</yellow> already exists."
            ));
        }
        return 1;
    }

    // Delete Map
    private static int executeDeleteMap(final CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getExecutor() instanceof Player player)) {
            return 1;
        }

        String mapId = StringArgumentType.getString(ctx, "map_id");

        if (MapData.deleteMap(mapId)) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(
                    "Deleted map <yellow>" + mapId + "</yellow>."
            ));
        } else {
            player.sendMessage(MiniMessage.miniMessage().deserialize(
                    "Map <yellow>" + mapId + "</yellow> does not exist or is the default map."
            ));
        }
        return 1;
    }

    // Set Default Map
    private static int executeSetDefaultMap(final CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getExecutor() instanceof Player player)) {
            return 1;
        }

        String mapId = StringArgumentType.getString(ctx, "map_id");

        if (MapData.setDefaultMap(mapId)) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(
                    "Set <yellow>" + mapId + "</yellow> as the default map."
            ));
        } else {
            player.sendMessage(MiniMessage.miniMessage().deserialize(
                    "Map <yellow>" + mapId + "</yellow> does not exist."
            ));
        }
        return 1;
    }

    // Create a new path with starting point at player's location
    private static int executeCreatePath(final CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getExecutor() instanceof Player player)) {
            return 1;
        }

        String mapId = StringArgumentType.getString(ctx, "map_id");
        String pathId = StringArgumentType.getString(ctx, "path_id");

        // Validate path ID
        if (!pathId.matches("^[a-zA-Z0-9_-]+$")) {
            player.sendMessage(Component.text("Invalid path ID. Use only letters, numbers, underscores, and hyphens.", NamedTextColor.RED));
            return 1;
        }

        MapData.MapDetails mapDetails = MapData.getMap(mapId);
        if (mapDetails == null) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(
                    "Map <yellow>" + mapId + "</yellow> does not exist."
            ));
            return 1;
        }

        // Check if path already exists
        if (mapDetails.getPathIds().contains(pathId)) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(
                    "Path <green>" + pathId + "</green> already exists on map <yellow>" + mapId + "</yellow>."
            ));
            return 1;
        }

        // Create the path with starting point at player's location
        if (MapData.createPath(mapId, pathId, player.getLocation())) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(
                    "Created path <green>" + pathId +
                            "</green> on map <yellow>" + mapId +
                            "</yellow> with starting point at your location."
            ));
        } else {
            player.sendMessage(MiniMessage.miniMessage().deserialize(
                    "Failed to create path <green>" + pathId + "</green>."
            ));
        }
        return 1;
    }

    // Add Waypoint to specific path
    private static int executeAddWaypointToPath(final CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getExecutor() instanceof Player player)) {
            return 1;
        }

        String mapId = StringArgumentType.getString(ctx, "map_id");
        String pathId = StringArgumentType.getString(ctx, "path_id");

        if (MapData.addWaypoint(mapId, pathId, player.getLocation())) {
            int waypointCount = MapData.getWaypointCount(mapId, pathId);
            player.sendMessage(MiniMessage.miniMessage().deserialize(
                    "Added waypoint #<yellow>" + waypointCount +
                            "</yellow> to path <green>" + pathId +
                            "</green> on map <yellow>" + mapId + "</yellow>."
            ));

            // If this is the first waypoint added after the starting point, clarify
            if (waypointCount == 2) {
                player.sendMessage(MiniMessage.miniMessage().deserialize(
                        "<gray>Each path's last waypoint will be used as an end point.</gray>"
                ));
            }
        } else {
            player.sendMessage(MiniMessage.miniMessage().deserialize(
                    "Map <yellow>" + mapId + "</yellow> or path <green>" + pathId + "</green> does not exist."
            ));
        }
        return 1;
    }

    // Remove a path
    private static int executeRemovePath(final CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getExecutor() instanceof Player player)) {
            return 1;
        }

        String mapId = StringArgumentType.getString(ctx, "map_id");
        String pathId = StringArgumentType.getString(ctx, "path_id");

        if (MapData.removePath(mapId, pathId)) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(
                    "Removed path <green>" + pathId + "</green> from map <yellow>" + mapId + "</yellow>."
            ));
        } else {
            player.sendMessage(MiniMessage.miniMessage().deserialize(
                    "Path <green>" + pathId + "</green> does not exist or map <yellow>" + mapId + "</yellow> does not exist."
            ));
        }
        return 1;
    }

    // Clear specific path
    private static int executeClearSpecificPath(final CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getExecutor() instanceof Player player)) {
            return 1;
        }

        String mapId = StringArgumentType.getString(ctx, "map_id");
        String pathId = StringArgumentType.getString(ctx, "path_id");

        if (MapData.clearPath(mapId, pathId)) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(
                    "Cleared all waypoints from path <green>" + pathId +
                            "</green> on map <yellow>" + mapId + "</yellow>."
            ));
        } else {
            player.sendMessage(MiniMessage.miniMessage().deserialize(
                    "Map <yellow>" + mapId + "</yellow> or path <green>" + pathId + "</green> does not exist."
            ));
        }
        return 1;
    }

    // List all paths for a map
    private static int executeListPaths(final CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getExecutor() instanceof Player player)) {
            return 1;
        }

        String mapId = StringArgumentType.getString(ctx, "map_id");
        Set<String> pathIds = MapData.getPathIds(mapId);

        if (pathIds.isEmpty()) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(
                    "Map <yellow>" + mapId + "</yellow> does not exist or has no paths."
            ));
            return 1;
        }

        player.sendMessage(Component.text("==== Paths for map " + mapId + " (" + pathIds.size() + ") ====", NamedTextColor.GOLD));
        for (String pathId : pathIds) {
            int waypointCount = MapData.getWaypointCount(mapId, pathId);

            Component pathInfo = Component.text("- " + pathId, NamedTextColor.GREEN)
                    .append(Component.text(" (" + waypointCount + " waypoints)", NamedTextColor.YELLOW));


            player.sendMessage(pathInfo);
        }
        return 1;
    }

    // Show path info
    private static int executeShowPathInfo(final CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getExecutor() instanceof Player player)) {
            return 1;
        }

        String mapId = StringArgumentType.getString(ctx, "map_id");
        String pathId = StringArgumentType.getString(ctx, "path_id");

        MapData.MapDetails map = MapData.getMap(mapId);
        if (map == null) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(
                    "Map <yellow>" + mapId + "</yellow> does not exist."
            ));
            return 1;
        }

        MapData.PathData pathData = map.getPathData(pathId);
        if (pathData == null) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(
                    "Path <green>" + pathId + "</green> does not exist on map <yellow>" + mapId + "</yellow>."
            ));
            return 1;
        }

        List<Location> waypoints = pathData.getWaypoints();

        player.sendMessage(Component.text("==== Path: " + pathId + " on Map: " + mapId + " ====", NamedTextColor.GOLD));

        // Show start point (first waypoint)
        if (!waypoints.isEmpty()) {
            Location start = waypoints.get(0);
            player.sendMessage(Component.text("Start Point: ", NamedTextColor.GREEN)
                    .append(Component.text("X: " + formatCoordinate(start.getX()) +
                            ", Y: " + formatCoordinate(start.getY()) +
                            ", Z: " + formatCoordinate(start.getZ()), NamedTextColor.GRAY)));
        }

        // Show waypoints (excluding start point)
        int waypointCount = waypoints.size() - 1; // Exclude start point
        player.sendMessage(Component.text("Waypoints: " + waypointCount, NamedTextColor.YELLOW));

        for (int i = 1; i < waypoints.size(); i++) {
            Location wp = waypoints.get(i);

            // Special highlighting for the end point (last waypoint)
            NamedTextColor indexColor = (i == waypoints.size() - 1) ? NamedTextColor.RED : NamedTextColor.YELLOW;
            String prefix = (i == waypoints.size() - 1) ? "End: " : i + ": ";

            player.sendMessage(Component.text("  " + prefix, indexColor)
                    .append(Component.text("X: " + formatCoordinate(wp.getX()) +
                            ", Y: " + formatCoordinate(wp.getY()) +
                            ", Z: " + formatCoordinate(wp.getZ()), NamedTextColor.GRAY)));
        }

        return 1;
    }

    // Show Map Info
    private static int executeShowMapInfo(final CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getExecutor() instanceof Player player)) {
            return 1;
        }

        String mapId = StringArgumentType.getString(ctx, "map_id");
        MapData.MapDetails map = MapData.getMap(mapId);

        if (map == null) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(
                    "Map <yellow>" + mapId + "</yellow> does not exist."
            ));
            return 1;
        }

        Set<String> pathIds = map.getPathIds();

        player.sendMessage(Component.text("==== Map: " + mapId + " ====", NamedTextColor.GOLD));

        // Show paths summary
        player.sendMessage(Component.text("Paths: " + pathIds.size(), NamedTextColor.GREEN));
        for (String pathId : pathIds) {
            List<Location> waypoints = map.getWaypoints(pathId);
            int wpCount = waypoints.size();

            Component pathInfo = Component.text("  " + pathId + ": ", NamedTextColor.GREEN)
                    .append(Component.text((wpCount > 0 ? wpCount - 1 : 0) + " waypoints", NamedTextColor.GRAY));


            // Show start and end points if available
            if (wpCount > 0) {
                Location start = waypoints.get(0);
                pathInfo = pathInfo.append(Component.text("\n    Start: ", NamedTextColor.YELLOW)
                        .append(Component.text("X: " + formatCoordinate(start.getX()) +
                                ", Y: " + formatCoordinate(start.getY()) +
                                ", Z: " + formatCoordinate(start.getZ()), NamedTextColor.GRAY)));

                if (wpCount > 1) {
                    Location end = waypoints.get(wpCount - 1);
                    pathInfo = pathInfo.append(Component.text("\n    End: ", NamedTextColor.RED)
                            .append(Component.text("X: " + formatCoordinate(end.getX()) +
                                    ", Y: " + formatCoordinate(end.getY()) +
                                    ", Z: " + formatCoordinate(end.getZ()), NamedTextColor.GRAY)));
                }
            }

            player.sendMessage(pathInfo);
        }

        player.sendMessage(Component.text("Use '/tdmap paths " + mapId + "' for path list", NamedTextColor.GRAY));
        player.sendMessage(Component.text("Use '/tdmap pathinfo " + mapId + " <path_id>' for detailed path info", NamedTextColor.GRAY));
        return 1;
    }

    // Default Help Command
    private static int showHelp(final CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getExecutor() instanceof Player player)) {
            return 1;
        }

        player.sendMessage(Component.text("==== Tower Defense Map Commands ====", NamedTextColor.GOLD));
        player.sendMessage(Component.text("/tdmap list", NamedTextColor.YELLOW)
                .append(Component.text(" - List all available maps", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/tdmap create <map_id>", NamedTextColor.YELLOW)
                .append(Component.text(" - Create a new empty map", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/tdmap delete <map_id>", NamedTextColor.YELLOW)
                .append(Component.text(" - Delete a map", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/tdmap setdefault <map_id>", NamedTextColor.YELLOW)
                .append(Component.text(" - Set default map", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/tdmap createpath <map_id> <path_id>", NamedTextColor.YELLOW)
                .append(Component.text(" - Create a new path with start at your location", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/tdmap addwp <map_id> <path_id>", NamedTextColor.YELLOW)
                .append(Component.text(" - Add waypoint to path", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/tdmap removepath <map_id> <path_id>", NamedTextColor.YELLOW)
                .append(Component.text(" - Remove a path", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/tdmap clearwp <map_id> <path_id>", NamedTextColor.YELLOW)
                .append(Component.text(" - Clear waypoints from path", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/tdmap paths <map_id>", NamedTextColor.YELLOW)
                .append(Component.text(" - List all paths in a map", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/tdmap pathinfo <map_id> <path_id>", NamedTextColor.YELLOW)
                .append(Component.text(" - Show detailed path information", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/tdmap info <map_id>", NamedTextColor.YELLOW)
                .append(Component.text(" - Show map information", NamedTextColor.GRAY)));
        return 1;
    }

    // Utility method to format coordinates
    private static String formatCoordinate(double coord) {
        return String.format("%.2f", coord);
    }
}