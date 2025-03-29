package ksucapproj.blockstowerdefense1.commands;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import ksucapproj.blockstowerdefense1.maps.MapData;

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

                // Add waypoint
                .then(Commands.literal("addwp")
                        .then(Commands.argument("map_id", StringArgumentType.word())
                                .suggests(MapCommand::getAvailableMapsSuggestions)
                                .executes(MapCommand::executeAddWaypoint)
                        )
                )

                // Clear waypoints
                .then(Commands.literal("clearwp")
                        .then(Commands.argument("map_id", StringArgumentType.word())
                                .suggests(MapCommand::getAvailableMapsSuggestions)
                                .executes(MapCommand::executeClearWaypoints)
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
        MapData.getAvailableMaps().stream()
                .forEach(builder::suggest);
        return builder.buildFuture();
    }

    // List Maps
    private static int executeListMaps(final CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getExecutor() instanceof Player player)) {
            return 1;
        }

        List<String> mapIds = MapData.getAvailableMaps();
        player.sendMessage(Component.text("§6Available maps (" + mapIds.size() + "):"));
        mapIds.forEach(mapId -> player.sendMessage(Component.text("§e- " + mapId)));
        return 1;
    }

    // Create Map
    private static int executeCreateMap(final CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getExecutor() instanceof Player player)) {
            return 1;
        }

        String mapId = StringArgumentType.getString(ctx, "map_id");

        // Validate map ID
        if (!mapId.matches("^[a-zA-Z0-9_-]+$")) {
            player.sendMessage(Component.text("§cInvalid map ID. Use only letters, numbers, underscores, and hyphens."));
            return 1;
        }

        // Try to create the map
        if (MapData.createMap(mapId, player.getLocation())) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(
                    "Created map <yellow>" + mapId + "</yellow> with start at your location."
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
                    "Map <yellow>" + mapId + "</yellow> does not exist."
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

    // Add Waypoint
    private static int executeAddWaypoint(final CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getExecutor() instanceof Player player)) {
            return 1;
        }

        String mapId = StringArgumentType.getString(ctx, "map_id");

        if (MapData.addWaypoint(mapId, player.getLocation())) {
            int waypointCount = MapData.getWaypointCount(mapId);
            player.sendMessage(MiniMessage.miniMessage().deserialize(
                    "Added waypoint #<yellow>" + waypointCount + "</yellow> to map <yellow>" + mapId + "</yellow> at your location."
            ));
        } else {
            player.sendMessage(MiniMessage.miniMessage().deserialize(
                    "Map <yellow>" + mapId + "</yellow> does not exist."
            ));
        }
        return 1;
    }

    // Clear Waypoints
    private static int executeClearWaypoints(final CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getExecutor() instanceof Player player)) {
            return 1;
        }

        String mapId = StringArgumentType.getString(ctx, "map_id");

        if (MapData.clearWaypoints(mapId)) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(
                    "Cleared all waypoints from map <yellow>" + mapId + "</yellow>."
            ));
        } else {
            player.sendMessage(MiniMessage.miniMessage().deserialize(
                    "Map <yellow>" + mapId + "</yellow> does not exist."
            ));
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

        Location start = map.getStartLocationinternal();
        List<Location> waypoints = map.getWaypoints();

        player.sendMessage(Component.text("§6==== Map: " + mapId + " ===="));
        player.sendMessage(Component.text("§eStart: §7X: " + formatCoordinate(start.getX()) +
                ", Y: " + formatCoordinate(start.getY()) +
                ", Z: " + formatCoordinate(start.getZ())));
        player.sendMessage(Component.text("§eWaypoints: §7" + waypoints.size()));

        for (int i = 0; i < waypoints.size(); i++) {
            Location wp = waypoints.get(i);
            player.sendMessage(Component.text("§e  " + (i + 1) + ": §7X: " + formatCoordinate(wp.getX()) +
                    ", Y: " + formatCoordinate(wp.getY()) +
                    ", Z: " + formatCoordinate(wp.getZ())));
        }
        return 1;
    }

    // Default Help Command
    private static int showHelp(final CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getExecutor() instanceof Player player)) {
            return 1;
        }

        player.sendMessage(Component.text("§6==== Tower Defense Map Commands ===="));
        player.sendMessage(Component.text("§e/tdmap list §7- List all available maps"));
        player.sendMessage(Component.text("§e/tdmap create <map_id> §7- Create a new map using your location as start"));
        player.sendMessage(Component.text("§e/tdmap delete <map_id> §7- Delete a map"));
        player.sendMessage(Component.text("§e/tdmap setdefault <map_id> §7- Set default map"));
        player.sendMessage(Component.text("§e/tdmap addwp <map_id> §7- Add waypoint to map at your location"));
        player.sendMessage(Component.text("§e/tdmap clearwp <map_id> §7- Clear all waypoints from map"));
        player.sendMessage(Component.text("§e/tdmap info <map_id> §7- Show map information"));
        return 1;
    }

    // Utility method to format coordinates
    private static String formatCoordinate(double coord) {
        return String.format("%.2f", coord);
    }
}