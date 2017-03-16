package me.gking2224.mc.mod.ctf.game.base;

import me.gking2224.mc.mod.ctf.game.Bounds;
import me.gking2224.mc.mod.ctf.game.CtfTeam.TeamColour;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BaseBuilderFactory {

	private static final BaseBuilder defaultBaseBuilder = new DefaultBaseBuilder();
	public static BaseBuilder defaultBaseBuilder() {
		return defaultBaseBuilder;
	}
	
	static class DefaultBaseBuilder implements BaseBuilder {
		
		private static final int WOOL_BLUE_ID = 11;
		private static final int WOOL_RED_ID = 14;
		@Override
		public Bounds buildBase(World world, BlockPos refPos, TeamColour colour) {

			world.setBlockState(refPos, getPrimaryMaterial(colour));
			return null;
		}

		@Override
		public IBlockState getPrimaryMaterial(TeamColour colour) {
			return Block.getStateById(Block.getIdFromBlock(getPrimaryBaseBlock()) + (getPrimaryBaseStateIdModifier(colour) << 12));
		}

		private int getPrimaryBaseStateIdModifier(TeamColour colour) {
			return (colour == TeamColour.BLUE) ? WOOL_BLUE_ID : WOOL_RED_ID;
		}

		private static Block getPrimaryBaseBlock() {
			return Blocks.WOOL;
		}
		
	}

}
