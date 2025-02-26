package me.matthewTest.pluginTest.logic;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class tabCompleter implements TabCompleter {

    //these are lists that hold their name's respective subcommands
    private final static List<String> mtdCommands = Arrays.asList("party", "hub");
    private final static List<String> partyCommands = Arrays.asList("invite", "kick", "create", "join", "leave");

    @Override
    //strings is an array that holds all arguments in the command
    //argument 0 is immediately after the /, argument 2 is the first subcommand, and so on
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        List<String> suggestions = new ArrayList<>();

        //first layer /mtd <subcommand>
        //ex: works for commands like /mtd party and /mtd hub
        if (strings.length == 1) {

            //takes all partial matches of argument zero (the desired subcommands) from commands
            //then places them into suggestions
            StringUtil.copyPartialMatches(strings[0], mtdCommands, suggestions);
        }

        //second layer: /mtd party <subcommand>
        //ex: works for commands like /mtd party invite and /mtd party leave
        else if (strings.length == 2 && strings[0].equalsIgnoreCase("party")) {
            StringUtil.copyPartialMatches(strings[1], partyCommands, suggestions);
        }

        //suggestions is sorted and non-sensitive to case
        suggestions.sort(String.CASE_INSENSITIVE_ORDER);
        return suggestions;
    }
}
