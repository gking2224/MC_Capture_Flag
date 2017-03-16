package me.gking2224.mc.mod.ctf.item;

import static java.util.Optional.ofNullable;
import static me.gking2224.mc.mod.ctf.game.CtfTeam.TeamColour.fromString;

import java.util.Optional;

import me.gking2224.mc.mod.ctf.game.CtfTeam;
import me.gking2224.mc.mod.ctf.game.CtfTeam.TeamColour;
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

	public static TeamColour getFlagColour(ItemBase item) {
		return fromString(
				item.getName().substring(Flag.FLAG_PREFIX.length()));
	}
	
	public static Optional<ItemBase> toFlag(ItemStack item) {
		return toFlag(item.getItem());
	}
	
	public static Optional<ItemBase> toFlag(Item item) {
		return ofNullable(isFlag(item) ? (ItemBase)item : null);
	}

	public static boolean isOwnTeamFlag(ItemBase flag, TeamColour teamColour) {
		return getFlagColour(flag) == teamColour;
	}

	public static TeamColour getOppositeColour(TeamColour colour) {
		return colour == TeamColour.RED ? TeamColour.BLUE : TeamColour.RED;
	}

	public static boolean isOwnTeamFlag(ItemBase flag, CtfTeam team) {
		return isOwnTeamFlag(flag, team.getColour());
	}

}
