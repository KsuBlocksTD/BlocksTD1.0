package ksucapproj.blockstowerdefense1.commands;

import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.alessiodp.parties.api.interfaces.Party;
import com.alessiodp.parties.api.interfaces.PartyPlayer;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import ksucapproj.blockstowerdefense1.BlocksTowerDefense1;
import ksucapproj.blockstowerdefense1.logic.TeleportationLogic;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class HubCommand {
    private static TeleportationLogic tpManager;
    private static final BlocksTowerDefense1 instance = BlocksTowerDefense1.getInstance();
    private static final PartiesAPI api = BlocksTowerDefense1.getApi();
    private static final FileConfiguration config = instance.getConfig();
    private static Location hubSpawn = null;

    public HubCommand(){
    }



    public static LiteralCommandNode<CommandSourceStack> register() {
        if (tpManager == null) {
            tpManager = new TeleportationLogic(instance);
        }

        return Commands.literal("hub")
                .requires(ctx -> ctx.getExecutor() instanceof Player)
                .executes(HubCommand::executeCommandLogic)
                .build();
    }


    private static int executeCommandLogic(final CommandContext<CommandSourceStack> ctx){
        if (!(ctx.getSource().getExecutor() instanceof Player player)){
            return Command.SINGLE_SUCCESS;
        }
//        Location targetLocation = new Location(player.getWorld(), player.getX(), player.getY()+2, player.getZ());
        //sends player confirmation msg
        player.sendMessage("Teleporting to the hub...");


        PartyPlayer partyPlayer = api.getPartyPlayer(player.getUniqueId());
        Party party = api.getParty(partyPlayer.getPartyId());

        // if the player is not in a party or is not the party leader
        if (party == null || !(party.getLeader().equals(partyPlayer.getPlayerUUID()))){
//            player.sendMessage("gets into party is null statement"); // for testing

            // passes the target location to the tpManager that employs the function with the teleport logic
            tpManager.teleportWithRetry(player, hubSpawn, 3 );
            return Command.SINGLE_SUCCESS;
        }

        // if the player is in party AND is the party leader
        if (party.getLeader().equals(partyPlayer.getPlayerUUID())){
//            player.sendMessage("gets into if statement"); // for testing
            for (PartyPlayer partyMember : party.getOnlineMembers()){
                Player playerInParty = Bukkit.getPlayer(partyMember.getPlayerUUID());


                //teleports all players in the executor's party to their location
                tpManager.teleportWithRetry(playerInParty, hubSpawn, 3 );

            }
        }

//        player.sendMessage("cuts to the end of the command"); // for testing
        return Command.SINGLE_SUCCESS;
    }




    public Location getHubFromConfig(){

        String worldName = config.getString("spawn.world");
        if (worldName == null){
            instance.getLogger().warning("Cannot get world name from config.yml");
            return null;
        }

        World world = instance.getServer().getWorld(worldName);

        if (world == null){
            instance.getLogger().warning("Cannot resolve world: " + worldName);
        }

        int x = config.getInt("spawn.x");
        int y = config.getInt("spawn.y");
        int z = config.getInt("spawn.z");
        float yaw = (float) config.getDouble("spawn.yaw");
        float pitch = (float) config.getDouble("spawn.pitch");


        return hubSpawn = new Location(world, x, y, z, yaw, pitch);
    }
}