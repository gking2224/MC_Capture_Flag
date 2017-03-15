package me.gking2224.mc.mod.ctf.util;

import net.minecraft.util.math.BlockPos;

public class WorldUtils {

	public static BlockPos move(BlockPos refPos, int x, int y, int z) {
		return new BlockPos(refPos.getX() + x, refPos.getY() + y, refPos.getZ() + z);
	}

	public static BlockPos getDelta(BlockPos p1, BlockPos p2) {
		return new BlockPos(delta(p1.getX(), p2.getX()), delta(p1.getY(), p2.getY()), delta(p1.getZ(), p2.getZ()));
	}

	private static int delta(int p1, int p2) {
		return Math.abs(p1 - p2);
	}

}
