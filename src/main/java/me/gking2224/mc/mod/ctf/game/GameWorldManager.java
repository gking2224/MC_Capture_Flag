package me.gking2224.mc.mod.ctf.game;

import java.util.Random;

import me.gking2224.mc.mod.ctf.blocks.PlacedFlag;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GameWorldManager {
	
	private static final int WOOL_BLUE = 11;
	private static final int WOOL_RED = 14;
	private static final IBlockState RED_BLOCK = Block.getStateById(Block.getIdFromBlock(Blocks.WOOL) + (WOOL_RED << 12));
	private static final IBlockState BLUE_BLOCK = Block.getStateById(Block.getIdFromBlock(Blocks.WOOL) + (WOOL_BLUE << 12));
	
	private static final IBlockState RED_FLAG = PlacedFlag.withColour(PlacedFlag.EnumFlagColour.RED);
	private static final IBlockState BLUE_FLAG = PlacedFlag.withColour(PlacedFlag.EnumFlagColour.BLUE);
	
	private GameWorldManager() {}
	
	private static GameWorldManager instance = new GameWorldManager();
	
	public final static GameWorldManager get() { return instance; }

	public void createGameBases(Game game, World world) {
		
		boolean rndBool = world.rand.nextBoolean();
		createBase(world, game, CtfTeam.RED, RED_BLOCK, rndBool);
		createBase(world, game, CtfTeam.BLUE, BLUE_BLOCK, !rndBool);
		
		resetFlags(game, world);
	}
	
	private void resetFlags(Game game, World world) {
		placeFlag(world, game.getBaseLocation(CtfTeam.RED), RED_FLAG);
		placeFlag(world, game.getBaseLocation(CtfTeam.BLUE), BLUE_FLAG);
	}

	private void placeFlag(World world, BlockPos refPos, IBlockState flag) {
		world.setBlockState(move(refPos, 0, 1, 0), flag);
	}

	private BlockPos move(BlockPos refPos, int x, int y, int z) {
		return new BlockPos(refPos.getX() + x, refPos.getY() + y, refPos.getZ() + z);
	}

	private void createBase(World world, Game game, String team, IBlockState state, boolean invertZ) {
		BlockPos refPos = getEndPos(world, game.getBounds(), invertZ);
		world.setBlockState(refPos, state);
		game.setBaseLocation(team, refPos);
	}

	private BlockPos getEndPos(World world, Bounds bounds, boolean invertZ) {
		Random rand = world.rand;
		int endZ = invertZ ? bounds.getFrom().getZ() : bounds.getTo().getZ();
		int midPointX = bounds.getFrom().getX() + (bounds.getTo().getX() - bounds.getFrom().getX()) / 2;
		int x = fromChunk(midPointX) + rand.nextInt() % 15;
		int z = fromChunk(endZ) + rand.nextInt() % 15;
		return new BlockPos(x,  world.getHeight(x, z), z);
	}

	private int fromChunk(int n) {
		return n * 16;
	}
}
