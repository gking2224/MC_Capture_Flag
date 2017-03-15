package me.gking2224.mc.mod.ctf.game;

import static me.gking2224.mc.mod.ctf.game.GameWorldManager.WorldMetrics.fromChunk;

import java.util.Random;

import me.gking2224.mc.mod.ctf.blocks.PlacedFlag;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeOcean;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;

public class GameWorldManager {
	
	private static final int WOOL_BLUE = 11;
	private static final int WOOL_RED = 14;
	private static final IBlockState RED_BLOCK = Block.getStateById(Block.getIdFromBlock(Blocks.WOOL) + (WOOL_RED << 12));
	private static final IBlockState BLUE_BLOCK = Block.getStateById(Block.getIdFromBlock(Blocks.WOOL) + (WOOL_BLUE << 12));
	
	private static final IBlockState RED_FLAG = PlacedFlag.withColour(PlacedFlag.EnumFlagColour.RED);
	private static final IBlockState BLUE_FLAG = PlacedFlag.withColour(PlacedFlag.EnumFlagColour.BLUE);
	
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
		createBase(game, CtfTeam.RED, RED_BLOCK, rndBool);
		createBase(game, CtfTeam.BLUE, BLUE_BLOCK, !rndBool);
		
		resetFlags(game);
	}
	
	private void resetFlags(Game game) {
		placeFlag(game.getBaseLocation(CtfTeam.RED), RED_FLAG);
		placeFlag(game.getBaseLocation(CtfTeam.BLUE), BLUE_FLAG);
	}

	private void placeFlag(BlockPos refPos, IBlockState flag) {
		world.setBlockState(move(refPos, 0, 1, 0), flag);
	}

	private BlockPos move(BlockPos refPos, int x, int y, int z) {
		return new BlockPos(refPos.getX() + x, refPos.getY() + y, refPos.getZ() + z);
	}

	private void createBase(Game game, String team, IBlockState state, boolean invertZ) {
		BlockPos refPos = getEndPos(game.getBounds(), invertZ);
		ensureChunkGenerated(refPos.getX() / 16, refPos.getZ() / 16);
		world.setBlockState(refPos, state);
		game.setBaseLocation(team, refPos);
	}

	private BlockPos getEndPos(Bounds bounds, boolean invertZ) {
		Random rand = world.rand;
		int endZ = invertZ ? bounds.getFrom().getZ() : bounds.getTo().getZ();
		int midPointX = bounds.getFrom().getX() + (bounds.getTo().getX() - bounds.getFrom().getX()) / 2;
		int x = fromChunk(midPointX) + rand.nextInt() % 15;
		int z = fromChunk(endZ) + rand.nextInt() % 15;
		int y = getWorldHeight(x, z);
		return new BlockPos(x,  y, z);
	}
	
	public int getWorldHeight(int x, int z) {
		ensureChunkGenerated(x / 16, z / 16);
		return world.getHeight(x, z);
	}

	private void ensureChunkGenerated(int x, int z) {
		if (!world.isChunkGeneratedAt(x, z)) {
			IChunkProvider cps = world.getChunkProvider();
			cps.provideChunk(x, z);
		}
	}

//	private int fromChunk(int n) {
//		return n * 16;
//	}

	public boolean isSuitableForGame(Bounds bounds) {
		return checkBiomesSuitable(bounds);
	}

	private boolean checkBiomesSuitable(Bounds bounds) {
		
		int width = bounds.getTo().getX() - bounds.getFrom().getX();
		int length = bounds.getTo().getZ() - bounds.getFrom().getZ();
		
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
				System.out.printf("Checking for suitable biome at (%d, %d)\n", x, z);
				int y = getWorldHeight(x, z);
				BlockPos blockPos = new BlockPos(x, y, z);
				if (!isSuitableBiome(getBiome(blockPos))) numUnsuitable++;
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
		return !BiomeOcean.class.isAssignableFrom(biome.getClass());
	}
	
	public static class WorldMetrics {
		public static int fromChunk(int n) {
			return n * 16;
		}
	}
}
