package me.gking2224.mc.mod.ctf.game.base;

import me.gking2224.mc.mod.ctf.game.Bounds;
import net.minecraft.util.math.BlockPos;

public class BaseDescription {

  private final Bounds bounds;
  private final BlockPos chestPos;

  public BaseDescription(Bounds bounds) {
    this(bounds, null);
  }

  public BaseDescription(Bounds bounds, BlockPos chestPos) {
    this.bounds = bounds;
    this.chestPos = chestPos;
  }

  public BlockPos getChestLocation() {
    return this.chestPos;
  }

}
