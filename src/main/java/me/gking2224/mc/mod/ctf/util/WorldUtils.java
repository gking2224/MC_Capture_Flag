package me.gking2224.mc.mod.ctf.util;

import static me.gking2224.mc.mod.ctf.util.WorldUtils.MoveAction.DOWN;
import static me.gking2224.mc.mod.ctf.util.WorldUtils.MoveAction.LATERAL;
import static me.gking2224.mc.mod.ctf.util.WorldUtils.MoveAction.UP;
import static net.minecraft.block.Block.getIdFromBlock;

import java.util.HashSet;
import java.util.Set;

import me.gking2224.mc.mod.ctf.game.Bounds;
import me.gking2224.mc.mod.ctf.game.ChunkLocation;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldUtils {

  public static enum MoveAction {
    DOWN, LATERAL, UP;
  }

  private static final Set<Integer> OVER_SURFACE_BLOCKS = new HashSet<Integer>();
  private static final Set<Integer> UNDER_SURFACE_BLOCKS = new HashSet<Integer>();
  private static final Set<Integer> CAN_TELEPORT_TO_BLOCKS = new HashSet<Integer>();
  private static final Set<Integer> CAN_TELEPORT_ABOVE_BLOCKS = new HashSet<Integer>();

  static {
    UNDER_SURFACE_BLOCKS.add(Block.getIdFromBlock(Blocks.GRAVEL));
    UNDER_SURFACE_BLOCKS.add(Block.getIdFromBlock(Blocks.STONE));
    UNDER_SURFACE_BLOCKS.add(Block.getIdFromBlock(Blocks.SANDSTONE));
    UNDER_SURFACE_BLOCKS.add(Block.getIdFromBlock(Blocks.DIRT));
    CAN_TELEPORT_ABOVE_BLOCKS.add(Block.getIdFromBlock(Blocks.SAND));

    OVER_SURFACE_BLOCKS.add(Block.getIdFromBlock(Blocks.LEAVES));
    OVER_SURFACE_BLOCKS.add(Block.getIdFromBlock(Blocks.LEAVES2));
    OVER_SURFACE_BLOCKS.add(Block.getIdFromBlock(Blocks.AIR));

    CAN_TELEPORT_ABOVE_BLOCKS.add(Block.getIdFromBlock(Blocks.GRASS));
    CAN_TELEPORT_ABOVE_BLOCKS.add(Block.getIdFromBlock(Blocks.GRASS_PATH));
    CAN_TELEPORT_ABOVE_BLOCKS.add(Block.getIdFromBlock(Blocks.RED_FLOWER));
    CAN_TELEPORT_ABOVE_BLOCKS.add(Block.getIdFromBlock(Blocks.YELLOW_FLOWER));
    CAN_TELEPORT_ABOVE_BLOCKS.add(Block.getIdFromBlock(Blocks.WATER));
  }

  private static int delta(int p1, int p2) {
    return Math.abs(p1 - p2);
  }

  public static void ensureBlockGenerated(World world, BlockPos pos) {
    if (!world.isBlockLoaded(pos)) {
      world.getChunkFromBlockCoords(pos);
      if (!world.isBlockLoaded(pos)) {
        System.out.println("ERROR ... but not showing as loaded :-(");
      }
    }
  }

  public static IBlockState getBlockAt(World world, BlockPos pos) {
    ensureBlockGenerated(world, pos);
    return world.getBlockState(pos);
  }

  public static BlockPos getDelta(BlockPos p1, BlockPos p2) {
    return new BlockPos(delta(p1.getX(), p2.getX()),
            delta(p1.getY(), p2.getY()), delta(p1.getZ(), p2.getZ()));
  }

  private static MoveAction getMoveAction(IBlockState blockAt) {
    System.out.println("Which way to move for " + blockAt.getBlock());
    final int idFromBlock = Block.getIdFromBlock(blockAt.getBlock());
    if (OVER_SURFACE_BLOCKS.contains(idFromBlock)) {
      System.out.println("Try going down");
      return DOWN;
    } else {
      if (CAN_TELEPORT_ABOVE_BLOCKS.contains(idFromBlock)
              || UNDER_SURFACE_BLOCKS.contains(idFromBlock))
      {
        System.out.println("Try going up");
        return UP;
      } else {
        System.out.println("Try going laterally");
        return LATERAL;
      }
    }
  }

  public static BlockPos getNearestSuitableTeleportLocation(World world, int x,
    int z)
  {
    BlockPos pos = getSurfaceBlock(world, x, z);
    System.out.println(
            String.format("Get nearest teleport location to %s\n", pos));
    while (!suitableForTeleport(world, pos)) {
      pos = tryDifferentTeleportLocation(world, pos);
    }
    System.out.println(String.format("Using %s\n", pos));
    return pos;
  }

  public static BlockPos getSurfaceBlock(World world, BlockPos pos) {
    return getSurfaceBlock(world, pos.getX(), pos.getZ());
  }

  public static BlockPos getSurfaceBlock(World world, int x, int z) {
    return new BlockPos(x, getWorldHeight(world, x, z) - 1, z);
  }

  public static int getWorldHeight(World world, BlockPos pos) {
    ensureBlockGenerated(world, pos);
    final int y = world.getHeight(pos.getX(), pos.getZ());
    return y;
  }

  public static int getWorldHeight(World world, int x, int z) {
    return getWorldHeight(world, new BlockPos(x, 0, z));
  }

  public static Bounds invertZ(Bounds b) {
    final int fromZ = b.getFrom().getZ() * -1;
    final int toZ = b.getTo().getZ() * -1;
    final Bounds inverted = new Bounds(
            new BlockPos(b.getFrom().getX(), b.getFrom().getY(), toZ),
            new BlockPos(b.getTo().getX(), b.getTo().getY(), fromZ));
    System.out
            .println(String.format("Inverted z from %s to %s\n", b, inverted));
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
            new BlockPos(Math.max(to1.getX(), to2.getX()),
                    Math.max(to1.getY(), to2.getY()),
                    Math.max(to1.getZ(), to2.getZ())));
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
    System.out.println(String.format("placeBlocks %s at %s\n", state, bounds));
    replaceBlocks(world, null, bounds, state);
  }

  public static void replaceBlocks(World world, Block replaceBlockType,
    Bounds bounds, IBlockState state)
  {
    for (int x = bounds.getFrom().getX(); x <= bounds.getTo().getX(); x++) {
      for (int y = bounds.getFrom().getY(); y <= bounds.getTo().getY(); y++) {
        for (int z = bounds.getFrom().getZ(); z <= bounds.getTo().getZ(); z++) {
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

  private static boolean suitableForTeleport(World world, BlockPos pos) {
    final BlockPos above = new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ());
    final IBlockState blockAt = getBlockAt(world, pos);
    final IBlockState blockAbove = getBlockAt(world, above);
    final BlockPos below = new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ());
    final IBlockState blockBelow = getBlockAt(world, below);
    if (CAN_TELEPORT_ABOVE_BLOCKS
            .contains(Block.getIdFromBlock(blockBelow.getBlock()))
            || UNDER_SURFACE_BLOCKS
                    .contains(Block.getIdFromBlock(blockBelow.getBlock())))
    {
      if (Blocks.AIR == blockAt.getBlock()) {
        if (Blocks.AIR == blockAbove.getBlock()) {
          System.out.println(String.format("Can teleport to %s (%s|%s|%s)\n",
                  pos, blockBelow.getBlock(), blockAt.getBlock(),
                  blockAbove.getBlock()));
          return true;
        }
      }
    }
    System.out.println(String.format("Not teleporting to %s (%s|%s|%s)\n", pos,
            blockBelow.getBlock(), blockAt.getBlock(), blockAbove.getBlock()));
    return false;
  }

  public static ChunkLocation toChunkLocation(BlockPos pos) {
    return new ChunkLocation(pos.getX() / 16, pos.getZ() / 16);
  }

  private static BlockPos tryDifferentTeleportLocation(World world,
    BlockPos pos)
  {
    final IBlockState blockAt = getBlockAt(world, pos);
    switch (getMoveAction(blockAt)) {

    case DOWN:
      return new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ());
    case UP:
      return new BlockPos(pos.getX(), pos.getY() + 3, pos.getZ());
    case LATERAL:
    default:
      final int x = world.rand.nextInt(5) - 2;
      final int z = world.rand.nextInt(5) - 2;
      return new BlockPos(pos.getX() + x, pos.getY() + 3, pos.getZ() + z);
    }
  }
}
