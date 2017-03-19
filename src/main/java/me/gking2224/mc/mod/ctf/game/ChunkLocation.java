package me.gking2224.mc.mod.ctf.game;

public class ChunkLocation {

  private int x;
  private int z;

  public ChunkLocation() {

  }

  public ChunkLocation(int x, int z) {
    super();
    this.x = x;
    this.z = z;
  }

  public int getX() {
    return x;
  }

  public void setX(int x) {
    this.x = x;
  }

  public int getZ() {
    return z;
  }

  public void setZ(int z) {
    this.z = z;
  }

  public String toString() {
    return String.format("(%d, %d)", x, z);
  }
}
