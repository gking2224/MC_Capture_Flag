package me.gking2224.mc.mod.ctf.util;

import static net.minecraft.block.Block.getIdFromBlock;
import me.gking2224.mc.mod.ctf.game.Bounds;
import me.gking2224.mc.mod.ctf.game.ChunkLocation;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldUtils {

	public static BlockPos offsetBlockPos(BlockPos refPos, int x, int y, int z) {
		return new BlockPos(refPos.getX() + x, refPos.getY() + y, refPos.getZ() + z);
	}

	public static BlockPos getDelta(BlockPos p1, BlockPos p2) {
		return new BlockPos(delta(p1.getX(), p2.getX()), delta(p1.getY(), p2.getY()), delta(p1.getZ(), p2.getZ()));
	}

	private static int delta(int p1, int p2) {
		return Math.abs(p1 - p2);
	}

	public static ChunkLocation toChunkLocation(BlockPos pos) {
		return new ChunkLocation(pos.getX() / 16, pos.getZ() / 16);
	}

	public static void replaceBlocks(World world, Block replaceBlockType, Bounds bounds, IBlockState state) {
		for (int x = bounds.getFrom().getX(); x <= bounds.getTo().getX(); x++) {
			for (int z = bounds.getFrom().getZ(); z <= bounds.getTo().getZ(); z++) {
				for (int y = bounds.getFrom().getY(); y <= bounds.getTo().getY(); y++) {
					BlockPos pos = new BlockPos(x, y, z);
					Block existingBlock = world.getBlockState(pos).getBlock();
					if (replaceBlockType == null || getIdFromBlock(existingBlock) == getIdFromBlock(replaceBlockType)) {
						world.setBlockState(pos, state);
					}
				}
			}
		}
	}

	public static void placeBlocks(World world, Bounds bounds, IBlockState state) {
		replaceBlocks(world, null, bounds, state);
	}
}