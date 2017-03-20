package me.gking2224.mc.mod.ctf.game;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class Bounds {
  public static Bounds readFromNBT(NBTTagCompound nbt, String key) {
    final int[] a = nbt.getIntArray(key);
    final Bounds rv = new Bounds(new BlockPos(a[0], 0, a[1]),
            new BlockPos(a[2], 0, a[3]));
    return rv;
  }

  public static int[] toIntArray(Bounds b) {
    return new int[] {
        b.from.getX(), b.from.getZ(), b.to.getX(), b.to.getZ()
    };
  }

  public static void writeToNBT(NBTTagCompound nbt, String key, Bounds b) {
    nbt.setIntArray(key, toIntArray(b));
  }

  protected BlockPos from;

  protected BlockPos to;

  public Bounds() {}

  public Bounds(BlockPos from, BlockPos to) {
    this.from = from;
    this.to = to;
  }

  public int getDepth() {
    return this.getTo().getZ() - this.getFrom().getZ();
  }

  public BlockPos getFrom() {
    return this.from;
  }

  public Vec3i getSize() {
    return new Vec3i(this.to.getX() - this.from.getX(), 0,
            this.to.getZ() - this.from.getZ());
  }

  public BlockPos getTo() {
    return this.to;
  }

  public int getWidth() {
    return this.getTo().getX() - this.getFrom().getX();
  }

  @Override public String toString() {
    return String.format("[%s : %s]", this.from, this.to);
  }

}
