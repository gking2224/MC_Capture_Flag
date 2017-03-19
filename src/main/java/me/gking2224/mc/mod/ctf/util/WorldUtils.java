package me.gking2224.mc.mod.ctf.util;

import static net.minecraft.block.Block.getIdFromBlock;
import me.gking2224.mc.mod.ctf.game.Bounds;
import me.gking2224.mc.mod.ctf.game.ChunkLocation;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldUtils {

	public static BlockPos offsetBlockPos(BlockPos refPos, int x, int y, int z) {
		return new BlockPos(refPos.getX() + x, refPos.getY() + y, refPos.getZ() + z);
	}

	public static BlockPos offsetBlockPos(BlockPos refPos, BlockPos offset) {
		return offsetBlockPos(refPos, offset.getX(), offset.getY(), offset.getZ());
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

	public static Bounds offsetBounds(BlockPos refPos, Bounds bounds) {
		return new Bounds(offsetBlockPos(refPos, bounds.getFrom()), offsetBlockPos(refPos, bounds.getTo()));
	}
	

	public static Bounds maximumBounds(Bounds b1, Bounds b2) {
		BlockPos from1 = b1.getFrom();
		BlockPos from2 = b2.getFrom();
		BlockPos to1 = b1.getTo();
		BlockPos to2 = b2.getTo();
		return new Bounds(
				new BlockPos(
						Math.min(from1.getX(), from2.getX()),
						Math.min(from1.getY(), from2.getY()),
						Math.min(from1.getZ(), from2.getZ())),
				new BlockPos(
						Math.min(to1.getX(), to2.getX()),
						Math.min(to1.getY(), to2.getY()),
						Math.min(to1.getZ(), to2.getZ())));
	}
}