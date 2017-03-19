package me.gking2224.mc.mod.ctf.game.base;

import java.util.List;

import me.gking2224.mc.mod.ctf.game.Bounds;
import me.gking2224.mc.mod.ctf.game.CtfTeam.TeamColour;
import me.gking2224.mc.mod.ctf.util.WorldUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ConfigBaseBuilder implements BaseBuilder {

	public static class BuildConfig {

		public Bounds getBounds() {
			// TODO Auto-generated method stub
			return null;
		}

		public IBlockState getBlockState() {
			// TODO Auto-generated method stub
			return null;
		}

	}


	@Override
	public IBlockState getPrimaryMaterial(TeamColour team) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bounds buildBase(World world, BlockPos refPos, TeamColour colour) {
		List<BuildConfig> configs = null;
		configs.forEach(c -> {
			placeBlocks(world, c.getBounds(), c.getBlockState());
		});
	}
	
	
	private void placeBocks(World world, Bounds bounds, IBlockState state) {
		WorldUtils.placeBlocks(world, bounds, state);
	}

}
