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

public class JoinCtfGame extends CommandBase {
	private final List<String> aliases;

	protected String fullEntityName;
	protected Entity conjuredEntity;
	
	public JoinCtfGame() {

        aliases = new ArrayList<String>(); 
        aliases.add("jg"); 
	}

	@Override
	public int compareTo(ICommand o) {
		return 0;
	}

	@Override
	public String getName() {
		return "join_ctf_game";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "join_ctf_game <name>";
	}

	@Override
	public List<String> getAliases() {
		return this.aliases;
	}

	@Override
	protected void doExecute(MinecraftServer server, ICommandSender sender,
			String[] args) throws CommandException {
		
		Entity e = sender.getCommandSenderEntity();
		String gameName = args[0];
		
		if (e == null) return;
		if (e instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer)e;
			
			Game game = GameManager.get().getGame(gameName);
			if (game == null) throw new CommandException("Game %s not found", gameName);
			
			String playerName = player.getName();
			String team = game.getTeamForPlayer(playerName);
			if (team != null) {
				sender.sendMessage(new TextComponentString(
						String.format("Already in game %s on team %s", gameName, team)));
			}
			else {
				team = game.addPlayer(playerName);
				GameManager.get().save();
				sender.sendMessage(new TextComponentString(
						String.format("Joined game %s on team %s", gameName, team)));
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
		return new String[] { "name" };
	}

	@Override
	protected boolean[] getMandatoryArgs() {
		return new boolean[] { true };
	}
}
