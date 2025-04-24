package ksucapproj.blockstowerdefense1.commands.mtd;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.concurrent.CompletableFuture;

// this function is the base command for the /mtd hierarchy **
// "/mtd"


/*

    ** MTD COMMAND HIERARCHY **

/mtd:

    party:
        create
        delete
        invite <player>
        kick <player>
        leave
        view-all

    hub:

 */

public class MtdCommand {

    private static final List<String> subcommands = List.of("hub", "party");

    // this function is what connects the execution of the command and its potential suggestions
    // this .register() function registers the base command of "/mtd" for all subcommands
    // registering all mtd-related commands requires only the base command to be registered
    // this registration is done in onEnable()
    @NullMarked
    public static LiteralCommandNode<CommandSourceStack> register() {
        return Commands.literal("mtd")

                // .requires(): in order for the command to be built and executed, this property must first be met
                // in this case, in context (ctx), the one executing the command must be a player, rather than the server

                .requires(ctx -> ctx.getExecutor().hasPermission("blockstd.mtd"))
                .executes(MtdCommand::executeHelpCommand)
                .then(Commands.literal("help")
                        .executes(MtdCommand::executeHelpCommand)
                )

                //register subcommands here
                // these are all commands that follow the structure: "/mtd <subcommand>"
                // this action does not execute the commands here, but simply 'build' them under /mtd so that
                // they're accessible and ready for use when needed
                .then(PartyCommand.register())
                .then(HubCommand.register())

                // this is the only 'logic' for the "/mtd" command, as it has no function when executed w/o a subcommand
                // .suggests() gives the potential options for subcommands to choose from that are actually executable
                .then(Commands.argument("subcommand", StringArgumentType.word())
                        .suggests(MtdCommand::getMtdSuggestions)
                )

                // .build() simply finalizes the command so it all becomes one in this command 'package'
                .build();
    }

    // gives the suggestions of the subcommands for mtd base command
    private static CompletableFuture<Suggestions> getMtdSuggestions(final CommandContext<CommandSourceStack> ctx, final SuggestionsBuilder builder){

        for (String word : subcommands){
            builder.suggest(word);
        }
        return builder.buildFuture();
    }

    private static int executeHelpCommand(final CommandContext<CommandSourceStack> ctx) {
        var executor = ctx.getSource().getExecutor();

        if (executor instanceof Player player) {
            showHelp(player);
        } else {
            showConsoleHelp();
        }

        return Command.SINGLE_SUCCESS;
    }


    private static void showHelp(Player player) {
        player.sendRichMessage("<gray>================= <aqua><bold>MTD Help </bold></aqua>=================</gray>");
        player.sendRichMessage("<aqua>/mtd party</aqua> <gray>- Show party-specific help menu</gray>");
        player.sendRichMessage("<aqua>/mtd hub</aqua> <gray>- Return to the hub</gray>");
        player.sendRichMessage("<aqua>/mtd help</aqua> <gray>- Show this help menu</gray>");
        player.sendRichMessage("<gray>============================================</gray>");
//        player.sendRichMessage("<gray>-----------------------------------</gray>");
    }

    private static void showConsoleHelp() {
        Server server = Bukkit.getServer();
        ConsoleCommandSender console = server.getConsoleSender();

        console.sendRichMessage("<gray>================= <aqua><bold>MTD Help </bold></aqua>=================</gray>");
        console.sendRichMessage("<aqua>/mtd party</aqua> <gray>- Show party-specific help menu</gray>");
        console.sendRichMessage("<aqua>/mtd hub</aqua> <gray>- Return to the hub</gray>");
        console.sendRichMessage("<aqua>/mtd help</aqua> <gray>- Show this help menu</gray>");
        console.sendRichMessage("<gray>============================================</gray>");
//        console.sendRichMessage("<gray>-----------------------------------</gray>");
    }
}