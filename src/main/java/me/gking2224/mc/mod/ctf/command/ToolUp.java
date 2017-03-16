package me.gking2224.mc.mod.ctf.command;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.gking2224.mc.mod.ctf.util.InventoryUtils;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class ToolUp extends CommandBase {
	private final List<String> aliases;

	protected String fullEntityName;
	protected Entity conjuredEntity;
	
	public ToolUp() {

        aliases = new ArrayList<String>(); 
        aliases.add("tu"); 
	}

	@Override
	public int compareTo(ICommand o) {
		return 0;
	}

	@Override
	public String getName() {
		return "ctf:tool_up";
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
			
			Set<ItemStack> items = getItems();
			InventoryUtils.addItemsToPlayerInventory(player, items);
		}
	}

	private Set<ItemStack> getItems() {
		Set<ItemStack> set = new HashSet<ItemStack>();
		set.add(new ItemStack(Items.WOODEN_SWORD, 1));
		set.add(new ItemStack(Items.WOODEN_AXE, 1));
		set.add(new ItemStack(Items.WOODEN_SHOVEL, 1));
		set.add(new ItemStack(Items.BOW, 1));
		set.add(new ItemStack(Items.ARROW, 10));
		set.add(new ItemStack(Items.COOKED_BEEF, 10));
		set.add(new ItemStack(Items.BOAT, 1));
		set.add(new ItemStack(Blocks.TORCH, 64));
		return set;
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
