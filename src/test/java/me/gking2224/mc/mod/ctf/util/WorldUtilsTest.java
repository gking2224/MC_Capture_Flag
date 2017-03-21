package me.gking2224.mc.mod.ctf.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import me.gking2224.mc.mod.ctf.game.Bounds;
import net.minecraft.util.math.BlockPos;

public class WorldUtilsTest {

  @Test public void testGetDelta() {
    final BlockPos p1 = new BlockPos(5, 5, 5);
    final BlockPos p2 = new BlockPos(6, 7, 8);

    final BlockPos expected = new BlockPos(1, 2, 3);
    assertEquals(expected, WorldUtils.getDelta(p1, p2));
  }

  @Test public void testInvertZ() {
    final BlockPos p1 = new BlockPos(1, 1, 1);
    final BlockPos p2 = new BlockPos(5, 5, 5);
    final Bounds b = new Bounds(p1, p2);
    final Bounds expected = new Bounds(1, 1, -5, 5, 5, -1);
    assertEquals(expected, WorldUtils.invertZ(b));
  }

  @Test public void testMaximumBounds() {
    final BlockPos f1 = new BlockPos(1, 1, 1);
    final BlockPos t1 = new BlockPos(5, 5, 5);

    final BlockPos f2 = new BlockPos(2, 0, 1);
    final BlockPos t2 = new BlockPos(4, 5, 6);

    final Bounds b1 = new Bounds(f1, t1);
    final Bounds b2 = new Bounds(f2, t2);

    final Bounds expected = new Bounds(1, 0, 1, 5, 5, 6);
    assertEquals(expected, WorldUtils.maximumBounds(b1, b2));
  }

}
