package me.gking2224.mc.mod.ctf.game.base;

import me.gking2224.mc.mod.ctf.game.CtfTeam.TeamColour;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

public interface BaseBuilder {
  BaseDescription buildBase(BlockPos refPos, TeamColour colour,
    IBlockState ambientBlock, boolean invertZ);

  IBlockState getPrimaryMaterial(TeamColour team);
}
