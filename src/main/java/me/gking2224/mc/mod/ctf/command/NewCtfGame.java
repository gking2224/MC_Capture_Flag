package me.gking2224.mc.mod.ctf.command;

import static java.lang.String.format;
import static me.gking2224.mc.mod.ctf.util.StringUtils.toIText;

import java.util.ArrayList;
import java.util.List;

import me.gking2224.mc.mod.ctf.game.CtfTeam;
import me.gking2224.mc.mod.ctf.game.Game;
import me.gking2224.mc.mod.ctf.game.GameCreationException;
import me.gking2224.mc.mod.ctf.game.GameManager;
import me.gking2224.mc.mod.ctf.util.InventoryUtils;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class NewCtfGame extends CommandBase {
	private final List<String> aliases;

	protected String fullEntityName;
	protected Entity conjuredEntity;
	
	public NewCtfGame() {

        aliases = new ArrayList<String>(); 
        aliases.add("ng"); 
	}

	@Override
	public int compareTo(ICommand o) {
		return 0;
	}

	@Override
	public String getName() {
		return "new_ctf_game";
	}

	@Override
	public List<String> getAliases() {
		return this.aliases;
	}

	@Override
	protected void doExecute(
			MinecraftServer server, ICommandSender sender,
			String[] args
	) throws CommandException {

		Entity e = sender.getCommandSenderEntity();
		String name = args.length >= 1 ? args[0] : null;
		
		if (e == null) return;
		if (e instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer)e;
			String playerName = player.getName();
			
			try {
				GameManager gameManager = GameManager.get();
				gameManager.playerLeaveAllGames(playerName);
				Game game = gameManager.newGame(name, player);
				CtfTeam team = game.addPlayer(playerName);
				//TODO refactor join game code
				player.setSpawnPoint(game.getBaseLocation(team.getColour()), true);
				gameManager.sendPlayerToBase(game, player);
				gameManager.toolUpPlayer(player);
				sender.sendMessage(toIText(
						format("Created game %s and joined team %s", game.getName(), team.getColour())));
			}
			catch (GameCreationException ex) {
				throw new CommandException(ex.getMessage());
			}
		}
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return true;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server,
			ICommandSender sender, String[] args, BlockPos targetPos) {
		return null;
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return false;
	}


	@Override
	protected boolean[] getMandatoryArgs() {
		return new boolean[] { false, false };
	}

	@Override
	protected String[] getArgNames() {
		return new String[] { "name", "options" };
	}
}
