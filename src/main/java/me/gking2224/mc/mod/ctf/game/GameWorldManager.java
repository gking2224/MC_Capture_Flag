package me.gking2224.mc.mod.ctf.game;

import static java.lang.String.format;
import static me.gking2224.mc.mod.ctf.game.GameWorldManager.WorldMetrics.fromChunk;
import static me.gking2224.mc.mod.ctf.game.GameWorldManager.WorldMetrics.toChunk;
import static me.gking2224.mc.mod.ctf.util.StringUtils.blockPosStr;
import static me.gking2224.mc.mod.ctf.util.WorldUtils.offsetBlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.gking2224.mc.mod.ctf.blocks.PlacedFlag;
import me.gking2224.mc.mod.ctf.game.CtfTeam.TeamColour;
import me.gking2224.mc.mod.ctf.game.base.BaseBuilder;
import me.gking2224.mc.mod.ctf.game.base.BaseBuilderFactory;
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
import net.minecraft.world.chunk.IChunkProvider;

public class GameWorldManager {
	
	private static Logger LOGGER = Logger.getLogger(GameManager.class.getName()); 
	
	private static final IBlockState RED_FLAG = PlacedFlag.withColour(PlacedFlag.EnumFlagColour.RED);
	private static final IBlockState BLUE_FLAG = PlacedFlag.withColour(PlacedFlag.EnumFlagColour.BLUE);
	
	private static final List<Integer> FLAG_BLOCK_WHITELIST = new ArrayList<Integer>();
	private static final List<Integer> BASE_BIOME_BLACKLIST = new ArrayList<Integer>();
	
	private static final int BIOME_ID_OCEAN = 0;
	private static final int BIOME_ID_DEEP_OCEAN = 24;
	private static final int BIOME_ID_COLD_BEACH = 26;
	
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
	}
	
	@SuppressWarnings("unused")
	private MinecraftServer server;
	
	private World world;
	
	private GameWorldManager(MinecraftServer server) {
		this.server = server;
		this.world = server.getEntityWorld();
	}

	public static void init(MinecraftServer server) {
		if (instance != null) throw new IllegalStateException();
		instance = new GameWorldManager(server);
	}
	
	private static GameWorldManager instance = null;
	
	public final static GameWorldManager get() { return instance; }

	public void createGameBases(Game game) {
		
		boolean rndBool = world.rand.nextBoolean();

		game.getTeamColours().forEach(colour -> createBase(game, colour, colour == TeamColour.RED && rndBool));
		
		resetFlags(game);
	}
	
	public void resetFlags(Game game) {
		game.getTeamColours().forEach(colour -> resetFlag(game, colour));
	}

	public void resetFlag(Game game, TeamColour colour) {
		
		deletePlacedFlag(game, colour);
		removeFlagFromAllPlayerInventories(game, colour);
		placeFlag(game, colour, TeamColour.BLUE.equals(colour) ? BLUE_FLAG : RED_FLAG);
	}

	public void removeFlagsFromPlayerInventories(Game game) {
		ModItems.ALL_FLAGS.forEach(flag -> removeFlagFromAllPlayerInventories(game, flag));
	}

	public void removeFlagFromAllPlayerInventories(Game game, TeamColour colour) {
		removeFlagFromAllPlayerInventories(game, Flag.getForColour(colour));
	}

	public void removeFlagFromAllPlayerInventories(Game game, ItemBase flag) {
		game.getAllPlayers().forEach(p -> removeFlagFromPlayerInventory(p, flag));
	}

	public void removeFlagFromPlayerInventory(EntityPlayer player, TeamColour colour) {
		ItemBase flag = Flag.getForColour(colour);
		removeFlagFromPlayerInventory(player, flag);
	}
	
	public void removeFlagFromPlayerInventory(String playerName, ItemBase flag) {
		Optional<EntityPlayer> p = GameManager.get().getPlayerByName(playerName);
		p.ifPresent(player -> removeFlagFromPlayerInventory(player, flag));
	}

	public void removeFlagFromPlayerInventory(EntityPlayer player, ItemBase flag) {
		InventoryUtils.removeAllSimilarItemsFromPlayerInventory(player, flag);
	}

	public void deletePlacedFlag(Game game, TeamColour teamColour) {
		BlockPos pos = game.getFlagPosition(teamColour);
		if (pos != null) {
			LOGGER.log(Level.INFO, format("Delete %s flag from %s", teamColour, pos));
			LOGGER.log(Level.INFO, "Delete {} flag from {}", new Object[] {teamColour, pos});
			IBlockState current = getBlockAt(pos);
			System.out.printf("%s: Check block at position %s: %s\n", Thread.currentThread().getName(), pos, current);
			world.destroyBlock(pos, false);
		}
	}

	private IBlockState getBlockAt(BlockPos pos) {
		ensureBlockGenerated(pos);
		return world.getBlockState(pos);
	}

	private void placeFlag(Game game, TeamColour colour, IBlockState flag) {
		BlockPos flagPos = offsetBlockPos(game.getBaseLocation(colour), 0, 1, 0);
		world.setBlockState(flagPos, flag);
		game.setFlagBlockPosition(colour, flagPos);
	}


	private void createBase(Game game, TeamColour team, boolean invertZ) {
		BlockPos refPos = getBasePos(game.getBounds(), invertZ);
		ensureBlockGenerated(refPos);
		createBaseStructure(refPos, team);
		System.out.printf("Team %s base location at %s\n", team, blockPosStr(refPos));
		game.setBaseLocation(team, refPos);
	}

	private Bounds createBaseStructure(BlockPos refPos, TeamColour colour) {
		BaseBuilder builder = BaseBuilderFactory.defaultBaseBuilder();
		return builder.buildBase(world, refPos, colour);
	}

	private BlockPos adjustBasePosition(BlockPos refPos) {

		int refX = refPos.getX();
		int refZ = refPos.getZ();
		ensureBlockGenerated(refPos);
		
		for (int x = -7; x < 15; x++) {
			for (int z = -7; z < 15; z++) {
				BlockPos testPos = new BlockPos(refX + x, 0, refZ + z);
				BlockPos testSurface = getSurfaceBlock(testPos);
				System.out.printf("Testing block at %s... ", blockPosStr(testSurface));
				Block block = world.getBlockState(testSurface).getBlock();
				Biome b = getBiome(testSurface);
				System.out.printf("biome %s... ", b.getBiomeName());
				if (!FLAG_BLOCK_WHITELIST.contains(Block.getIdFromBlock(block))) {
					System.out.printf("not creating base on block %s\n", block.getLocalizedName());
				}
				else {
					System.out.printf("creating base on block %s\n", block.getLocalizedName());
					return testSurface;
				}
				
			}
		}
		Biome b = getBiome(refPos);
		System.out.printf("Could not find suitable block for base, hope for the best - you're in %s!\n", b.getBiomeName());
		return getSurfaceBlock(refPos);
	}

	private BlockPos getSurfaceBlock(BlockPos pos) {
		int x = pos.getX();
		int z = pos.getZ();
		return new BlockPos(x, getWorldHeight(x, z) -1, z);
	}

	private BlockPos getBasePos(Bounds bounds, boolean invertZ) {
		Random rand = world.rand;
		int endZ = invertZ ? bounds.getFrom().getZ() : bounds.getTo().getZ();
		int midPointX = bounds.getFrom().getX() + (bounds.getTo().getX() - bounds.getFrom().getX()) / 2;
		int x = fromChunk(midPointX) + (rand.nextInt() % 15);
		int z = fromChunk(endZ) + (rand.nextInt() % 15);
		return adjustBasePosition(new BlockPos(x, 0, z));
	}
	
	public int getWorldHeight(int x, int z) {
		ensureChunkGenerated(toChunk(x), toChunk(z));
		return world.getHeight(x, z);
	}

	private void ensureBlockGenerated(BlockPos pos) {
		ensureChunkGenerated(toChunk(pos.getX()), toChunk(pos.getZ()));
	}

	private void ensureChunkGenerated(int x, int z) {
		if (!world.isChunkGeneratedAt(x, z)) {
			IChunkProvider cps = world.getChunkProvider();
			cps.provideChunk(x, z);
			System.out.printf("Force loaded chunk at %d, %d\n", x, z);
			if (!world.isChunkGeneratedAt(x, z)) {
				System.out.println(".. but not showing as loaded :-(");
			}
		}
	}

	public boolean isSuitableForGame(Bounds bounds) {
		return checkBiomesSuitable(bounds);
	}

	private boolean checkBiomesSuitable(Bounds bounds) {
		
		int x1 = fromChunk(bounds.getFrom().getX());
		int x2 = fromChunk(bounds.getTo().getX()) + 15;
		int z1 = fromChunk(bounds.getFrom().getZ());
		int z2 = fromChunk(bounds.getTo().getZ()) + 15;
		
		int xDiff = x2 - x1;
		int zDiff = z2 = z1;
		int xInc = Math.max(1,  (int)(xDiff / 30));
		int zInc = Math.max(1, (int)(zDiff / 30));
		
		int numChecks = 0;
		int numUnsuitable = 0;
		for (int x = x1; x <= x2; x += xInc) {
			for (int z = z1; z <= z2; z += zInc) {
				System.out.printf("Checking for suitable biome at (%d, %d)...", x, z);
				BlockPos blockPos = getSurfaceBlock(new BlockPos(x, 0, z));
				Biome biome = getBiome(blockPos);
				System.out.println(biome.getBiomeName());
				if (!isSuitableBiome(biome)) numUnsuitable++;
				numChecks++;
			}
		}
		double percentUnsuitable = (numUnsuitable / numChecks)*100;
		if (percentUnsuitable >= 0.3d) {
			System.out.printf("Game boundary %s not suitable as %5.1f points had unsuitable biome\n", bounds, percentUnsuitable);
			return false;
		}
		System.out.printf("Game boundary %s suitable as only %5.1f points had unsuitable biome\n", bounds, percentUnsuitable);
		return true;
	}

	private Biome getBiome(BlockPos blockPos) {
		Chunk c = world.getChunkFromBlockCoords(blockPos);
		if (!c.isLoaded()) System.out.printf("ERROR: chunk not loaded at %s\n", blockPos);
		return world.getBiome(blockPos);
	}

	private boolean isSuitableBiome(Biome biome) {
		return !BASE_BIOME_BLACKLIST.contains(Biome.getIdForBiome(biome));
	}
	
	public static class WorldMetrics {
		public static int fromChunk(int n) {
			return n * 16;
		}
		public static int toChunk(int n) {
			return (int)Math.floor(n / 16);
		}
	}

	public boolean isInHomeBase(Game game, TeamColour colour, BlockPos blockPos) {
		BlockPos pos = game.getBaseLocation(colour);
		BlockPos delta = WorldUtils.getDelta(pos, blockPos);
		return (delta.getX() <= 3 && delta.getY() <= 3 && delta.getZ() <= 3);
	}
}
