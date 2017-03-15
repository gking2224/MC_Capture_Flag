package me.gking2224.mc.mod.ctf.util;

import net.minecraft.util.math.BlockPos;

public class StringUtils {

	public static String blockPosStr(BlockPos blockPos) {
		return String.format("(%d %d %d)", blockPos.getX(), blockPos.getY(), blockPos.getZ());
	}

}
