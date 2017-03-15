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

public class BackToBase implements ICommand {
	private final List<String> aliases;

	protected String fullEntityName;
	protected Entity conjuredEntity;
	
	public BackToBase() {

        aliases = new ArrayList<String>(); 
        aliases.add("btb"); 
	}

	@Override
	public int compareTo(ICommand o) {
		return 0;
	}

	@Override
	public String getName() {
		return "back_to_base";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "back_to_base";
	}

	@Override
	public List<String> getAliases() {
		return this.aliases;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender,
			String[] args) throws CommandException {
		Entity e = sender.getCommandSenderEntity();
		
		if (e == null) return;
		if (e instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer)e;
			String playerName = player.getName();
			Game game = GameManager.get().getPlayerActiveGame(playerName);
			if (game == null) throw new CommandException("No active game");
			game.sendPlayerToBase(server.getEntityWorld(), playerName);
			
			System.out.printf("Player %s sent back to base\n", playerName);
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

}
