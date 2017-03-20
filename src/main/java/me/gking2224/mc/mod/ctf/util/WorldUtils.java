package me.gking2224.mc.mod.ctf.util;

import static net.minecraft.block.Block.getIdFromBlock;

import me.gking2224.mc.mod.ctf.game.Bounds;
import me.gking2224.mc.mod.ctf.game.ChunkLocation;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldUtils {

  private static int delta(int p1, int p2) {
    return Math.abs(p1 - p2);
  }

  public static BlockPos getDelta(BlockPos p1, BlockPos p2) {
    return new BlockPos(delta(p1.getX(), p2.getX()),
            delta(p1.getY(), p2.getY()), delta(p1.getZ(), p2.getZ()));
  }

  public static Bounds invertZ(Bounds b) {
    final int fromZ = b.getFrom().getZ() * -1;
    final int toZ = b.getTo().getZ() * -1;
    final Bounds inverted = new Bounds(
            new BlockPos(b.getFrom().getX(), b.getFrom().getY(), toZ),
            new BlockPos(b.getTo().getX(), b.getTo().getY(), fromZ));
    System.out.printf("Inverted z from %s to %s\n", b, inverted);
    return inverted;
  }

  public static Bounds maximumBounds(Bounds b1, Bounds b2) {
    final BlockPos from1 = b1.getFrom();
    final BlockPos from2 = b2.getFrom();
    final BlockPos to1 = b1.getTo();
    final BlockPos to2 = b2.getTo();
    return new Bounds(
            new BlockPos(Math.min(from1.getX(), from2.getX()),
                    Math.min(from1.getY(), from2.getY()),
                    Math.min(from1.getZ(), from2.getZ())),
            new BlockPos(Math.min(to1.getX(), to2.getX()),
                    Math.min(to1.getY(), to2.getY()),
                    Math.min(to1.getZ(), to2.getZ())));
  }

  public static BlockPos offset(BlockPos refPos, BlockPos offset) {
    return offset(refPos, offset.getX(), offset.getY(), offset.getZ());
  }

  public static Bounds offset(BlockPos refPos, Bounds bounds) {
    return new Bounds(offset(refPos, bounds.getFrom()),
            offset(refPos, bounds.getTo()));
  }

  public static BlockPos offset(BlockPos refPos, int x, int y, int z) {
    return new BlockPos(refPos.getX() + x, refPos.getY() + y,
            refPos.getZ() + z);
  }

  public static void placeBlocks(World world, Bounds bounds,
    IBlockState state)
  {
    System.out.printf("placeBlocks %s at %s\n", state, bounds);
    replaceBlocks(world, null, bounds, state);
  }

  public static void replaceBlocks(World world, Block replaceBlockType,
    Bounds bounds, IBlockState state)
  {
    for (int x = bounds.getFrom().getX(); x <= bounds.getTo().getX(); x++) {
      for (int z = bounds.getFrom().getZ(); z <= bounds.getTo().getZ(); z++) {
        for (int y = bounds.getFrom().getY(); y <= bounds.getTo().getY(); y++) {
          final BlockPos pos = new BlockPos(x, y, z);
          final Block existingBlock = world.getBlockState(pos).getBlock();
          if (replaceBlockType == null || getIdFromBlock(
                  existingBlock) == getIdFromBlock(replaceBlockType))
          {
            world.setBlockState(pos, state);
          }
        }
      }
    }
  }

  public static ChunkLocation toChunkLocation(BlockPos pos) {
    return new ChunkLocation(pos.getX() / 16, pos.getZ() / 16);
  }
}
