package me.gking2224.mc.mod.ctf.game.base;

import me.gking2224.mc.mod.ctf.game.Bounds;
import net.minecraft.util.math.BlockPos;

public class BaseDescription {

  private final Bounds bounds;
  private final BlockPos chestPos;
  private final BlockPos oppFlagPos;

  public BaseDescription(Bounds bounds) {
    this(bounds, null, null);
  }

  public BaseDescription(Bounds bounds, BlockPos chestPos,
          BlockPos oppFlagPos)
  {
    this.bounds = bounds;
    this.chestPos = chestPos;
    this.oppFlagPos = oppFlagPos;
  }

  public BlockPos getChestLocation() {
    return this.chestPos;
  }

  public BlockPos getOppFlagLocation() {
    return this.oppFlagPos;
  }

}
