package me.gking2224.mc.mod.ctf.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public class StringUtils {

  public static String blockPosStr(BlockPos blockPos) {
    return String.format("(%d %d %d)", blockPos.getX(), blockPos.getY(),
            blockPos.getZ());
  }

  public static ITextComponent toIText(String msg) {
    return new TextComponentString(msg);
  }

}
