package ksucapproj.blockstowerdefense1.commands.mtd;

import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.alessiodp.parties.api.interfaces.Party;
import com.alessiodp.parties.api.interfaces.PartyPlayer;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import ksucapproj.blockstowerdefense1.BlocksTowerDefense1;
import ksucapproj.blockstowerdefense1.logic.GUI.StartGameGUI;
import ksucapproj.blockstowerdefense1.logic.TeleportationLogic;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import static ksucapproj.blockstowerdefense1.commands.mtd.PartyCommand.checkPartyLeaderStatus;

// this function is under the /mtd hierarchy, and is therefore not its own standalone command **
// "/mtd hub"

public class HubCommand {
    private static TeleportationLogic tpManager;
    private static final BlocksTowerDefense1 instance = BlocksTowerDefense1.getInstance();
    private static final PartiesAPI api = BlocksTowerDefense1.getApi();
    private static Location hubSpawn = getHubFromConfig();

    // this .register() function is registered under the base command "/mtd <subcommand>" as a subcommand
    // command hierarchy display exists in MtdCommand
    public static LiteralCommandNode<CommandSourceStack> register() {
        if (tpManager == null) {
            tpManager = new TeleportationLogic(instance);
        }

        return Commands.literal("hub")
                .requires(ctx -> ctx.getExecutor() instanceof Player)
                .executes(HubCommand::executeCommandLogic)
                .build();
    }

    // this is the execution logic for the registering of the hub teleportation command
    private static int executeCommandLogic(final CommandContext<CommandSourceStack> ctx){
        if (!(ctx.getSource().getExecutor() instanceof Player player)){
            return Command.SINGLE_SUCCESS;
        }

        if ((hubSpawn.getX() == 0) && (hubSpawn.getY() == 0) && (hubSpawn.getZ() == 0)){
            player.sendRichMessage("<red>The hub location has not been set up yet!");
            return Command.SINGLE_SUCCESS;
        }


        //sends player confirmation msg
        player.sendMessage("Teleporting to the hub...");
        if(!StartGameGUI.hasCompass(player)) {
            StartGameGUI.giveMapSelectorCompass(player);
        }

        // if the player is not in a party or is not the party leader
        if (!Boolean.TRUE.equals(checkPartyLeaderStatus(player))) {
//            player.sendMessage("gets into party is null statement"); // for testing

            // passes the target location to the tpManager that employs the function with the teleport logic
            tpManager.teleportWithRetry(player, hubSpawn, 3);
            return Command.SINGLE_SUCCESS;
        }

        PartyPlayer partyPlayer = api.getPartyPlayer(player.getUniqueId());
        Party party = api.getParty(partyPlayer.getPartyId());

        // if the player is in party AND is the party leader
        // player.sendMessage("gets into if statement"); // for testing

        for (PartyPlayer partyMember : party.getOnlineMembers()){
            Player playerInParty = Bukkit.getPlayer(partyMember.getPlayerUUID());


            //teleports all players in the executor's party to their location
            tpManager.teleportWithRetry(playerInParty, hubSpawn, 3 );

        }

//        player.sendMessage("cuts to the end of the command"); // for testing
        return Command.SINGLE_SUCCESS;
    }




    // method that pulls config spawn and initializes hubSpawn as it
    public static Location getHubFromConfig(){
        FileConfiguration config = BlocksTowerDefense1.getInstance().getConfig();
        instance.reloadConfig();

        // gets the world name from the config's world path
        String worldName = config.getString("btd.spawn.world");
        if (worldName == null){ // if null it returns and gives error msg
            instance.getLogger().warning("Cannot get world name from config.yml");
            return null;
        }

        World world = instance.getServer().getWorld(worldName);

        if (world == null){ // if null it returns and gives error message
            instance.getLogger().warning("Cannot resolve world: " + worldName);
            return null;
        }

        // creates the coordinates for the spawn location from the config
        double x = config.getDouble("btd.spawn.x");
        int y = config.getInt("btd.spawn.y");
        double z = config.getDouble("btd.spawn.z");
        float yaw = (float) config.getDouble("btd.spawn.yaw");
        float pitch = (float) config.getDouble("btd.spawn.pitch");

        return hubSpawn = new Location(world, x, y, z, yaw, pitch);
    }
}