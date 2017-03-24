package me.gking2224.mc.mod.ctf.util;

import static java.lang.Math.abs;
import static java.lang.Math.floor;
import static java.lang.String.format;
import static me.gking2224.mc.mod.ctf.util.WorldUtils.MoveAction.DOWN;
import static me.gking2224.mc.mod.ctf.util.WorldUtils.MoveAction.LATERAL;
import static me.gking2224.mc.mod.ctf.util.WorldUtils.MoveAction.UP;
import static net.minecraft.block.Block.getIdFromBlock;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import me.gking2224.mc.mod.ctf.game.Bounds;
import me.gking2224.mc.mod.ctf.game.ChunkLocation;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldUtils {

  public enum Direction {

    NORTH("north", 0), NORTH_EAST("north-east", 45), EAST("east",
            90), SOUTH_EAST("south-east", 135), SOUTH("south", 180), SOUTH_WEST(
                    "south-west",
                    225), WEST("west", 270), NORTH_WEST("north-west", 315);

    public static final double HEADING_MIDPOINT = Math.ceil(45 / 2);

    public static Set<Direction> all() {
      return new HashSet<Direction>(Arrays.asList(NORTH, SOUTH, EAST, WEST,
              NORTH_EAST, NORTH_WEST, SOUTH_EAST, SOUTH_WEST));
    }

    private final int heading;
    private final String name;

    Direction(String name, int heading) {
      this.heading = heading;
      this.name = name;
    }

    public int getHeading() {
      return this.heading;
    }

    @Override public String toString() {
      return this.name;
    }

  }

  public static class DistanceAndHeading {

    private final double distance;
    private final Direction direction;

    public DistanceAndHeading(double distance, Direction direction) {
      this.distance = distance;
      this.direction = direction;
    }

    public Direction getDirection() {
      return this.direction;
    }

    public double getDistance() {
      return this.distance;
    }

  }

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
    UNDER_SURFACE_BLOCKS.add(Block.getIdFromBlock(Blocks.SAND));

    OVER_SURFACE_BLOCKS.add(Block.getIdFromBlock(Blocks.LEAVES));
    OVER_SURFACE_BLOCKS.add(Block.getIdFromBlock(Blocks.LEAVES2));
    OVER_SURFACE_BLOCKS.add(Block.getIdFromBlock(Blocks.AIR));

    CAN_TELEPORT_ABOVE_BLOCKS.add(Block.getIdFromBlock(Blocks.GRASS));
    CAN_TELEPORT_ABOVE_BLOCKS.add(Block.getIdFromBlock(Blocks.GRASS_PATH));
    CAN_TELEPORT_ABOVE_BLOCKS.add(Block.getIdFromBlock(Blocks.RED_FLOWER));
    CAN_TELEPORT_ABOVE_BLOCKS.add(Block.getIdFromBlock(Blocks.YELLOW_FLOWER));
    CAN_TELEPORT_ABOVE_BLOCKS.add(Block.getIdFromBlock(Blocks.WATER));
  }

  private static double adjustAngle(double a, int x, int z) {
    if (x < 0 && z < 0) { // both negative
      return 360 - a;
    } else if (x > 0 && z > 0) { // both positive
      return 180 - a;
    } else if (x < 0 && z > 0) { // x negative
      return 180 + a;
    } else {
      return a;
    }
  }

  public static BlockPos adjustY(World world, BlockPos pos) {
    System.out.println(
            String.format("Get nearest teleport location to %s\n", pos));
    int count = 0;
    while (!suitableForTeleport(world, pos) && count < 100) {
      pos = tryDifferentTeleportLocation(world, pos);
      count++;
    }
    System.out.println(String.format("Using %s\n", pos));
    return pos;
  }

  private static int delta(int p1, int p2, boolean absolute) {
    final int d = p1 - p2;
    return absolute ? Math.abs(d) : d;
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
    return getDelta(p1, p2, false);
  }

  public static BlockPos getDelta(BlockPos p1, BlockPos p2, boolean absolute) {
    return new BlockPos(delta(p1.getX(), p2.getX(), absolute),
            delta(p1.getY(), p2.getY(), absolute),
            delta(p1.getZ(), p2.getZ(), absolute));
  }

  public static Direction getDirectionFromAngle(double a) {
    final Set<Direction> ds = Direction.all().stream().filter(d -> {
      final int heading = d.getHeading();
      final double deltaFromHeading = floor(abs(a - heading));
      final double deltaFromHeading2 = floor(abs((a - 360) - heading));
      System.out.println(format("%5.3f: %s delta: %5.3f/%5.3f :: %5.3f", a, d,
              deltaFromHeading, deltaFromHeading2, Direction.HEADING_MIDPOINT));
      return deltaFromHeading <= Direction.HEADING_MIDPOINT
              || deltaFromHeading2 <= Direction.HEADING_MIDPOINT;
    }).collect(Collectors.toSet());
    System.out.println(format("valid directions: %s", ds));
    return ds.iterator().next();
  }

  public static DistanceAndHeading getDistanceAndHeading(
    BlockPos playerPosition, BlockPos basePosition)
  {
    System.out.println("player position: " + playerPosition);
    final BlockPos delta = WorldUtils.getDelta(basePosition, playerPosition,
            false);

    System.out.println(String.format("delta: %s", delta));

    final int x = delta.getX();
    final int z = delta.getZ();
    final double distance = Math.sqrt(Math.pow(x, 2) + Math.pow(z, 2));
    // if (distance < (nearLimit * 16)) { return String.format("near by"); }

    final double oppOverAdj = (z == 0) ? 0 : (double) x / (double) z;
    final double atan = Math.atan(oppOverAdj);
    final double a = Math.abs(atan * 180 / Math.PI);
    System.out.println("angle: " + a);
    final double aa = to360DegreeAngle(a, x, z);
    System.out.println("adjusted angle: " + aa);

    final Direction d = WorldUtils.getDirectionFromAngle(aa);

    return new DistanceAndHeading(distance, d);
  }

  private static MoveAction getMoveAction(IBlockState blockAt) {
//    System.out.println("Which way to move for " + blockAt.getBlock());
    final int idFromBlock = Block.getIdFromBlock(blockAt.getBlock());
    if (OVER_SURFACE_BLOCKS.contains(idFromBlock)) {
//      System.out.println("Try going down");
      return DOWN;
    } else {
      if (CAN_TELEPORT_ABOVE_BLOCKS.contains(idFromBlock)
              || UNDER_SURFACE_BLOCKS.contains(idFromBlock))
      {
//        System.out.println("Try going up");
        return UP;
      } else {
//        System.out.println("Try going laterally");
        return LATERAL;
      }
    }
  }

  // public static BlockPos getNearestSuitableTeleportLocation(World world, int
  // x,
  // int z)
  // {
  // return getSurfaceBlock(world, x, z, true);
  // }

  public static BlockPos getSurfaceBlock(World world, BlockPos pos) {
    return getSurfaceBlock(world, pos, true);
  }

  public static BlockPos getSurfaceBlock(World world, BlockPos pos,
    boolean adjust)
  {
    return getSurfaceBlock(world, pos.getX(), pos.getZ(), adjust);
  }

  public static BlockPos getSurfaceBlock(World world, int x, int z) {
    return getSurfaceBlock(world, x, z, true);
  }

  public static BlockPos getSurfaceBlock(World world, int x, int z,
    boolean adjust)
  {
    final BlockPos pos = new BlockPos(x, getWorldHeight(world, x, z) - 1, z);
    if (adjust) {
      return adjustY(world, pos);
    } else {
      return pos;
    }
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

  public static boolean isBlockAbove(BlockPos pos, BlockPos beneath) {
    final BlockPos delta = getDelta(pos, beneath);
    return (delta.getX() == 0 && delta.getZ() == 0 && delta.getY() == 1);
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

  public static TileEntityChest placeChest(World world, BlockPos pos) {
    world.setBlockState(pos, Blocks.CHEST.getDefaultState());
    final TileEntityChest tileEntity = (TileEntityChest) world
            .getTileEntity(pos);
    return tileEntity;
  }

  public static BlockPos randomPointInBounds(World world, Bounds bounds) {

    final int gameWidth = bounds.getWidth();
    final int gameDepth = bounds.getDepth();
    final int x = world.rand.nextInt(gameWidth);
    final int z = world.rand.nextInt(gameDepth);
    final BlockPos offset = new BlockPos(x, 0, z);
    return getSurfaceBlock(world, offset(bounds.getFrom(), offset), true);
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
//    System.out.println(String.format("Not teleporting to %s (%s|%s|%s)\n", pos,
//            blockBelow.getBlock(), blockAt.getBlock(), blockAbove.getBlock()));
    return false;
  }

  private static double to360DegreeAngle(double a, int x, int z) {
    double aa = a;
    if (Math.ceil(a) == 0) {
      aa = toStraightLineAngle(x, z);
    } else {
      aa = adjustAngle(a, x, z);
    }
    return (aa == 360 ? 0 : aa);
  }

  public static ChunkLocation toChunkLocation(BlockPos pos) {
    return new ChunkLocation(pos.getX() / 16, pos.getZ() / 16);
  }

  private static double toStraightLineAngle(int x, int z) {
    if (x == 0) {
      return (z < 0) ? 0 : 180;
    } else if (z == 0) {
      return (x < 0) ? 270 : 90;
    } else {
      throw new IllegalArgumentException("Either x or z must be 0");
    }
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
