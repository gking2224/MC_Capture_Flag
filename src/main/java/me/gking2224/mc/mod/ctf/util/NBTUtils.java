package me.gking2224.mc.mod.ctf.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class NBTUtils {

  public static BlockPos getBlockPos(NBTTagCompound nbt, String prefix) {
    final boolean isNull = nbt.getBoolean(prefix + "null");
    if (isNull) {
      return null;
    } else {
      final int x = nbt.getInteger(prefix + "x");
      final int y = nbt.getInteger(prefix + "y");
      final int z = nbt.getInteger(prefix + "z");
      return new BlockPos(x, y, z);
    }
  }

  public static void setBlockPos(NBTTagCompound nbt, String prefix,
    BlockPos blockPos)
  {
    if (blockPos == null) {
      nbt.setBoolean(prefix + "null", true);
    } else {
      nbt.setBoolean(prefix + "null", false);
      nbt.setInteger(prefix + "x", blockPos.getX());
      nbt.setInteger(prefix + "y", blockPos.getY());
      nbt.setInteger(prefix + "z", blockPos.getZ());
    }
  }

}
