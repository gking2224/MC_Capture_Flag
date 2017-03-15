package me.gking2224.mc.mod.ctf.command;

import java.util.ArrayList;
import java.util.List;

import me.gking2224.mc.mod.ctf.game.Game;
import me.gking2224.mc.mod.ctf.game.GameManager;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public class CurrentGame extends CommandBase {
	private static final String[] NO_ARGS = new String[0];
	private static final boolean[] NO_ARGS_MANDATORY = new boolean[0];

	private final List<String> aliases;

	protected String fullEntityName;
	protected Entity conjuredEntity;
	
	public CurrentGame() {

        aliases = new ArrayList<String>(); 
        aliases.add("cg"); 
	}

	@Override
	public int compareTo(ICommand o) {
		return 0;
	}

	@Override
	public String getName() {
		return "current_game";
	}

	@Override
	public List<String> getAliases() {
		return this.aliases;
	}

	@Override
	protected void doExecute(MinecraftServer server, ICommandSender sender,
			String[] args) throws CommandException {
		Entity e = sender.getCommandSenderEntity();
		
		if (e == null) return;
		if (e instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer)e;
			String playerName = player.getName();
			Game game = GameManager.get().getPlayerActiveGame(playerName);
			if (game == null) {
				server.sendMessage(new TextComponentString("You are not currently in a game. Use /new_ctf_game or /join_ctf_game"));
			}
			else {
				String team = game.getTeamForPlayer(playerName);
				server.sendMessage(new TextComponentString(String.format("You are currently in game %s, in team %s", game.getName(), team)));
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
	protected String[] getArgNames() {
		return NO_ARGS;
	}

	@Override
	protected boolean[] getMandatoryArgs() {
		return NO_ARGS_MANDATORY;
	}

}
