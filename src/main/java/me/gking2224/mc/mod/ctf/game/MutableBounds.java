package me.gking2224.mc.mod.ctf.game;

import net.minecraft.util.math.BlockPos;

public class MutableBounds extends Bounds {

  public MutableBounds() {
    super();
  }

  public MutableBounds(BlockPos from, BlockPos to) {
    super(from, to);
  }

  public MutableBounds(Bounds b) {
    super(b.getFrom(), b.getTo());
  }

  public void setFrom(BlockPos from) {
    this.from = from;
  }

  public void setTo(BlockPos to) {
    this.to = to;
  }

  public Bounds toImmutable() {
    return new Bounds(this.from, this.to);
  }

}
