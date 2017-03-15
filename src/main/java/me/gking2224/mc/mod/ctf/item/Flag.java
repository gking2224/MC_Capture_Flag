package me.gking2224.mc.mod.ctf.item;

import java.util.Optional;

import me.gking2224.mc.mod.ctf.game.CtfTeam;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class Flag {

	public static final String FLAG_PREFIX = "flag_";

	public static boolean isFlag(ItemStack item) {
		return Flag.isFlag(item.getItem());
	}

	public static boolean isFlag(Item item) {
		return item != null && ItemBase.class.isAssignableFrom(item.getClass()) && ((ItemBase)item).getName().startsWith(Flag.FLAG_PREFIX);
	}

	public static String getFlagColour(ItemBase item) {
		return item.getName().substring(Flag.FLAG_PREFIX.length());
	}
	
	public static Optional<ItemBase> toFlag(ItemStack item) {
		return toFlag(item.getItem());
	}
	
	public static Optional<ItemBase> toFlag(Item item) {
		return Optional.ofNullable(isFlag(item) ? (ItemBase)item : null);
	}

	public static boolean isOwnTeamFlag(ItemBase item, String team) {
		return item.getName().equals(FLAG_PREFIX+team);
	}

	public static String getOppositeColour(ItemBase flag) {
		return getFlagColour(flag).equals(CtfTeam.RED) ? CtfTeam.BLUE : CtfTeam.RED;
	}

}
