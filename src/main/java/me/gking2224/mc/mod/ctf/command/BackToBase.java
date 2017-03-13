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
			
			System.out.printf("Player %s wishes to go back to base 81", player.getName());
			player.setPosition(-69, 81, 526);	
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
