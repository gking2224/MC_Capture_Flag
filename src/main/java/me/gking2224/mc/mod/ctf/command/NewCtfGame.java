package me.gking2224.mc.mod.ctf.command;

import java.util.ArrayList;
import java.util.List;

import me.gking2224.mc.mod.ctf.game.GameCreationException;
import me.gking2224.mc.mod.ctf.game.GameManager;
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
		String name = args[0];
		
		if (e == null) return;
		if (e instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer)e;
			
			try {
				GameManager.get().newGame(name, player);
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
		return new boolean[] { true, false };
	}

	@Override
	protected String[] getArgNames() {
		return new String[] { "name", "options" };
	}
}
