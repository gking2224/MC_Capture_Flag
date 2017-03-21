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

  public Bounds(int fx, int fy, int fz, int tx, int ty, int tz) {

    this(new BlockPos(fx, fy, fz), new BlockPos(tx, ty, tz));
  }

  @Override public boolean equals(Object obj) {
    if (this == obj) { return true; }
    if (obj == null) { return false; }
    if (this.getClass() != obj.getClass()) { return false; }
    final Bounds other = (Bounds) obj;
    if (this.from == null) {
      if (other.from != null) { return false; }
    } else if (!this.from.equals(other.from)) { return false; }
    if (this.to == null) {
      if (other.to != null) { return false; }
    } else if (!this.to.equals(other.to)) { return false; }
    return true;
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

  @Override public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.from == null) ? 0 : this.from.hashCode());
    result = prime * result + ((this.to == null) ? 0 : this.to.hashCode());
    return result;
  }

  @Override public String toString() {
    return String.format("[%s : %s]", this.from, this.to);
  }

}
