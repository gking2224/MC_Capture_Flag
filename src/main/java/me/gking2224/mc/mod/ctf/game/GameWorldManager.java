package me.gking2224.mc.mod.ctf.game;

import static java.lang.Math.max;
import static java.lang.String.format;
import static me.gking2224.mc.mod.ctf.util.StringUtils.blockPosStr;
import static me.gking2224.mc.mod.ctf.util.WorldUtils.offset;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.gking2224.mc.mod.ctf.blocks.ModBlocks;
import me.gking2224.mc.mod.ctf.blocks.PlacedFlag;
import me.gking2224.mc.mod.ctf.game.CtfTeam.TeamColour;
import me.gking2224.mc.mod.ctf.game.base.BaseBuilder;
import me.gking2224.mc.mod.ctf.game.base.BaseBuilderFactory;
import me.gking2224.mc.mod.ctf.game.base.BaseDescription;
import me.gking2224.mc.mod.ctf.game.event.GameEventManager;
import me.gking2224.mc.mod.ctf.item.Flag;
import me.gking2224.mc.mod.ctf.item.ItemBase;
import me.gking2224.mc.mod.ctf.item.ModItems;
import me.gking2224.mc.mod.ctf.util.InventoryUtils;
import me.gking2224.mc.mod.ctf.util.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;

public class GameWorldManager {

  public static class WorldMetrics {
    public static int fromChunk(int n) {
      return n * 16;
    }

    public static int toChunk(int n) {
      return (int) Math.floor(n / 16);
    }
  }

  private static final IBlockState DEFAULT_PERIMITER = Blocks.BARRIER
          .getDefaultState();

  private static Logger LOGGER = Logger.getLogger(GameManager.class.getName());
  private static final IBlockState RED_FLAG = PlacedFlag
          .withColour(PlacedFlag.EnumFlagColour.RED);

  private static final IBlockState BLUE_FLAG = PlacedFlag
          .withColour(PlacedFlag.EnumFlagColour.BLUE);
  private static final List<Integer> FLAG_BLOCK_WHITELIST = new ArrayList<Integer>();

  private static final List<Integer> BASE_BIOME_BLACKLIST = new ArrayList<Integer>();
  private static final int BIOME_ID_OCEAN = 0;
  private static final int BIOME_ID_DEEP_OCEAN = 24;
  private static final int BIOME_ID_EXTREME_HILLS = 3;
  private static final int BIOME_ID_COLD_BEACH = 26;
  private static final int SAND = 12;
  private static final int DIRT = 3;
  private static final int GRAVEL = 13;
  private static final int FLOWING_WATER = 8;
  private static final int STILL_WATER = 9;

  static {
    FLAG_BLOCK_WHITELIST.add(Block.getIdFromBlock(Blocks.DIRT));
    FLAG_BLOCK_WHITELIST.add(Block.getIdFromBlock(Blocks.SANDSTONE));
    FLAG_BLOCK_WHITELIST.add(Block.getIdFromBlock(Blocks.STONE));
    FLAG_BLOCK_WHITELIST.add(Block.getIdFromBlock(Blocks.GRASS));
    FLAG_BLOCK_WHITELIST.add(Block.getIdFromBlock(Blocks.TALLGRASS));
    FLAG_BLOCK_WHITELIST.add(Block.getIdFromBlock(Blocks.SAND));
    FLAG_BLOCK_WHITELIST.add(Block.getIdFromBlock(Blocks.SNOW));

    BASE_BIOME_BLACKLIST.add(BIOME_ID_OCEAN);
    BASE_BIOME_BLACKLIST.add(BIOME_ID_COLD_BEACH);
    BASE_BIOME_BLACKLIST.add(BIOME_ID_DEEP_OCEAN);
    BASE_BIOME_BLACKLIST.add(BIOME_ID_EXTREME_HILLS);
  }

  private static GameWorldManager instance = null;

  private static final int MIN_INSET = 10;

  public final static GameWorldManager get() {
    return instance;
  }

  public static GameWorldManager init(MinecraftServer server, GameManager gm) {
    if (instance != null) { throw new IllegalStateException(); }
    instance = new GameWorldManager(server, gm);
    return instance;
  }

  private final MinecraftServer server;

  private final World world;
  @SuppressWarnings("unused") private final GameManager gm;

  private GameWorldManager(MinecraftServer server, GameManager gm) {
    this.server = server;
    this.world = server.getEntityWorld();
    this.gm = gm;
  }

  private BlockPos adjustBasePosition(BlockPos refPos, boolean invertZ) {

    final int refX = refPos.getX();
    final int refZ = refPos.getZ();
    WorldUtils.ensureBlockGenerated(this.world, refPos);

    for (int x = 7; x < 15; x++) {
      for (int z = 7; z < 15; z++) {
        final BlockPos testPos = new BlockPos(refX + x, 0,
                refZ + ((invertZ) ? z : z * -1));
        final BlockPos testSurface = WorldUtils.getSurfaceBlock(this.world,
                testPos);
        System.out.println(String.format("Testing block at %s... ",
                blockPosStr(testSurface)));
        final Block block = this.world.getBlockState(testSurface).getBlock();
        final Biome b = this.getBiome(testSurface);
        System.out.println(String.format("biome %s... ", b.getBiomeName()));
        if (!FLAG_BLOCK_WHITELIST.contains(Block.getIdFromBlock(block))) {
          // System.out.println(String.format("not creating base on block %s\n",
          // block.getLocalizedName());
        } else {
          System.out.println(String.format("creating base on block %s\n",
                  block.getLocalizedName()));
          return testSurface;
        }

      }
    }
    final Biome b = this.getBiome(refPos);
    System.out
            .println(String.format(
                    "Could not find suitable block for base,"
                            + " - you're in %s. Good luck!\n",
                    b.getBiomeName()));
    return WorldUtils.getSurfaceBlock(this.world, refPos);
  }

  public void buildBases(Game game) {

    final boolean rndBool = this.world.rand.nextBoolean();
    final BaseBuilder builder = BaseBuilderFactory.getBaseBuilder(this.server,
            game);

    game.getTeamColours().forEach(colour -> this.createBase(game, builder,
            colour, colour == TeamColour.RED ^ rndBool));

    this.resetFlags(game);
  }

  private void buildPerimeter(Bounds bounds, int lowPoint, int highPoint,
    IBlockState state)
  {
    // LHS
    int x = bounds.getFrom().getX();
    WorldUtils.replaceBlocks(this.world, Blocks.AIR,
            new Bounds(new BlockPos(x, lowPoint, bounds.getFrom().getZ()),
                    new BlockPos(x, highPoint, bounds.getTo().getZ())),
            state);
    // RHS
    x = bounds.getTo().getX();
    WorldUtils.replaceBlocks(this.world, Blocks.AIR,
            new Bounds(new BlockPos(x, lowPoint, bounds.getFrom().getZ()),
                    new BlockPos(x, highPoint, bounds.getTo().getZ())),
            state);
    // FRONT
    int z = bounds.getFrom().getZ();
    WorldUtils.replaceBlocks(this.world, Blocks.AIR,
            new Bounds(new BlockPos(bounds.getFrom().getX(), lowPoint, z),
                    new BlockPos(bounds.getTo().getX(), highPoint, z)),
            state);
    // FRONT
    z = bounds.getTo().getZ();
    WorldUtils.replaceBlocks(this.world, Blocks.AIR,
            new Bounds(new BlockPos(bounds.getFrom().getX(), lowPoint, z),
                    new BlockPos(bounds.getTo().getX(), highPoint, z)),
            state);
  }

  private void buildPerimiter(Game game) {
    final Optional<String> o = game.getOptions()
            .getString(GameOption.PERIMITER);
    o.ifPresent(option -> {
      IBlockState material = Block.getBlockFromName(option).getDefaultState();
      if (material == null) {
        System.out.println(
                String.format("Perimiter material %s not found\n", option));
        material = DEFAULT_PERIMITER;
      }
      final int redY = game.getBaseLocation(TeamColour.RED).getY();
      final int blueY = game.getBaseLocation(TeamColour.BLUE).getY();
      this.buildPerimeter(game.getBounds(), Math.min(redY, blueY) - 20,
              Math.max(redY, blueY) + 20, material);
    });
  }

  private boolean checkBiomesSuitable(Bounds bounds) {

    final int x1 = bounds.getFrom().getX();
    final int x2 = bounds.getTo().getX();
    final int z1 = bounds.getFrom().getZ();
    int z2 = bounds.getTo().getZ();

    final int xDiff = x2 - x1;
    final int zDiff = z2 = z1;
    final int xInc = Math.max(1, xDiff / 30);
    final int zInc = Math.max(1, zDiff / 30);

    int numChecks = 0;
    int numUnsuitable = 0;
    for (int x = x1; x <= x2; x += xInc) {
      for (int z = z1; z <= z2; z += zInc) {
        numChecks++;
        final BlockPos blockPos = WorldUtils.getSurfaceBlock(this.world,
                new BlockPos(x, 0, z));
        final Biome biome = this.getBiome(blockPos);
        System.out.println(biome.getBiomeName());
        if (!this.isSuitableBiome(biome)) {
          numUnsuitable++;
        }
      }
    }
    System.out.println(
            String.format("Num biome-suitablility checks: %d\n", numChecks));
    final double numUnsuitable2 = numUnsuitable;
    final double numChecks2 = numChecks;
    final double percentUnsuitable = (numUnsuitable2 / numChecks2) * 100;
    if (percentUnsuitable >= 30) {
      System.out.printf(
              "Game bounds %s not suitable as %5.0f "
                      + "points had unsuitable biome\n",
              bounds, percentUnsuitable);
      return false;
    }
    System.out.printf(
            "Game bounds %s suitable as %5.0f "
                    + "points had unsuitable biome\n",
            bounds, percentUnsuitable);
    return true;
  }

  private IBlockState convertAmbientBlock(IBlockState ambientBlock) {
    final int blockId = Block.getIdFromBlock(ambientBlock.getBlock());
    switch (blockId) {
    case SAND:
      return Blocks.SANDSTONE.getDefaultState();
    case DIRT:
      return Blocks.GRASS.getDefaultState();
    case GRAVEL:
      return Blocks.STONE.getDefaultState();
    case STILL_WATER:
    case FLOWING_WATER:
      return Blocks.SANDSTONE.getDefaultState();
    default:
      return ambientBlock;
    }
  }

  private void createBase(Game game, BaseBuilder builder, TeamColour team,
    boolean invertZ)
  {
    final Bounds gameBounds = game.getBounds();
    final BlockPos refPos = this.getBasePos(gameBounds, invertZ);
    WorldUtils.ensureBlockGenerated(this.world, refPos);
    System.out.println(String.format("%s game Zs: %d->%d; base Zs: %d\n", team,
            gameBounds.getFrom().getZ(), gameBounds.getTo().getZ(),
            refPos.getZ()));
    final IBlockState ambientBlock = this.getAmbientBlock(refPos);
    final BaseDescription bd = this.createBaseStructure(game, builder, refPos,
            team, ambientBlock, invertZ);
    System.out.println(String.format("Team %s base location at %s\n", team,
            blockPosStr(refPos)));
    game.setBaseLocation(team, refPos);
    game.setChestLocation(team, bd.getChestLocation());
  }

  private BaseDescription createBaseStructure(Game game, BaseBuilder builder,
    BlockPos refPos, TeamColour team, IBlockState ambientBlock,
    boolean invertZ)
  {
    return builder.buildBase(refPos, team, ambientBlock, invertZ);
  }

  public void createGameArea(Game game) {
    this.buildBases(game);
    this.buildPerimiter(game);
  }

  public void deletePlacedFlag(Game game, TeamColour teamColour) {
    final BlockPos pos = game.getFlagPosition(teamColour);
    if (pos != null) {
      LOGGER.log(Level.INFO, format("Delete %s flag from %s", teamColour, pos));
      LOGGER.log(Level.INFO, "Delete {} flag from {}",
              new Object[]
      {
          teamColour, pos
      });
      final IBlockState current = WorldUtils.getBlockAt(this.world, pos);
      System.out.println(String.format("%s: Check block at position %s: %s\n",
              Thread.currentThread().getName(), pos, current));
      this.world.destroyBlock(pos, false);
    }
  }

  private IBlockState getAmbientBlock(final BlockPos refPos) {
    return this.convertAmbientBlock(
            this.world.getBlockState(offset(refPos, new BlockPos(0, -1, 0))));
  }

  private BlockPos getBasePos(Bounds bounds, boolean invertZ) {
    System.out.println(
            String.format("Creating base for game bounds %s (size: %s)\n",
                    bounds, bounds.getSize()));
    final double zRatio = 0.1;
    final int w = bounds.getWidth();
    final int d = bounds.getDepth();
    final int xInset = this.world.rand.nextInt(w - 2 * MIN_INSET) + MIN_INSET;
    final int zInset = max(MIN_INSET,
            this.world.rand.nextInt((int) (d * zRatio)));
    final int endZ = invertZ ? bounds.getFrom().getZ() + zInset
            : bounds.getTo().getZ() - zInset;
    final int x = bounds.getFrom().getX() + xInset;
    final int z = endZ;

    System.out.println(String.format("Try to create base at %d, %d\n", x, z));
    return this.adjustBasePosition(new BlockPos(x, 0, z), invertZ);
  }

  private Biome getBiome(BlockPos blockPos) {
    final Chunk c = this.world.getChunkFromBlockCoords(blockPos);
    if (!c.isLoaded()) {
      System.out.println(
              String.format("ERROR: chunk not loaded at %s\n", blockPos));
    }
    return this.world.getBiome(blockPos);
  }

  public boolean isInHomeBase(Game game, TeamColour colour, BlockPos blockPos) {
    final BlockPos pos = game.getBaseLocation(colour);
    final BlockPos delta = WorldUtils.getDelta(pos, blockPos, true);
    return (delta.getX() <= 3 && delta.getY() <= 3 && delta.getZ() <= 3);
  }

  private boolean isSuitableBiome(Biome biome) {
    return !BASE_BIOME_BLACKLIST.contains(Biome.getIdForBiome(biome));
  }

  public boolean isSuitableForGame(Bounds bounds) {
    return this.checkBiomesSuitable(bounds);
  }

  private void placeFlag(Game game, TeamColour colour, IBlockState flag) {
    final BlockPos flagPos = offset(game.getBaseLocation(colour), 0, 1, 0);
    this.world.setBlockState(flagPos, flag);
    game.updateFlagBlockPosition(colour, flagPos);
  }

  private void placeFlagHolder(Game game, TeamColour colour) {
    final IBlockState block = ModBlocks.FLAG_HOLDER.getDefaultState();
    final BlockPos flagPos = game.getBaseLocation(colour);
    this.world.setBlockState(flagPos, block);
    game.updateFlagBlockPosition(colour, flagPos);
  }

  public void removeFlagFromAllPlayerInventories(Game game, ItemBase flag) {
    game.getAllPlayers()
            .forEach(p -> this.removeFlagFromPlayerInventory(p, flag));
  }

  public void removeFlagFromAllPlayerInventories(Game game, TeamColour colour) {
    this.removeFlagFromAllPlayerInventories(game, Flag.getForColour(colour));
  }

  public void removeFlagFromPlayerInventory(EntityPlayer player,
    ItemBase flag)
  {
    InventoryUtils.removeAllSimilarItemsFromPlayerInventory(player, flag);
  }

  public void removeFlagFromPlayerInventory(EntityPlayer player,
    TeamColour colour)
  {
    final ItemBase flag = Flag.getForColour(colour);
    this.removeFlagFromPlayerInventory(player, flag);
  }

  public void removeFlagFromPlayerInventory(String playerName, ItemBase flag) {
    final Optional<EntityPlayer> p = GameManager.get()
            .getPlayerByName(playerName);

    p.ifPresent(player -> GameEventManager.get()
            .schedule(() -> this.removeFlagFromPlayerInventory(player, flag)));
  }

  public void removeFlagsFromPlayerInventories(Game game) {
    ModItems.ALL_FLAGS.forEach(
            flag -> this.removeFlagFromAllPlayerInventories(game, flag));
  }

  public void resetFlag(Game game, TeamColour colour) {

    this.deletePlacedFlag(game, colour);
    this.removeFlagFromAllPlayerInventories(game, colour);
    this.placeFlagHolder(game, colour);
    this.placeFlag(game, colour,
            TeamColour.BLUE.equals(colour) ? BLUE_FLAG : RED_FLAG);
  }

  public void resetFlags(Game game) {
    game.getTeamColours().forEach(colour -> this.resetFlag(game, colour));
  }
}
