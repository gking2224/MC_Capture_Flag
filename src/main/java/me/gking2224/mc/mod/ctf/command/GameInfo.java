package me.gking2224.mc.mod.ctf.command;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class GameInfo extends CommandBase {
	private final List<String> aliases;

	public GameInfo() {

        aliases = new ArrayList<String>(); 
        aliases.add("gi"); 
	}

	@Override
	public int compareTo(ICommand o) {
		return 0;
	}

	@Override
	public String getName() {
		return "ctf:game_info";
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
		
		if (e == null) return;
		if (e instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer)e;
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
		return new boolean[0];
	}

	@Override
	protected String[] getArgNames() {
		return new String[0];
	}
}
