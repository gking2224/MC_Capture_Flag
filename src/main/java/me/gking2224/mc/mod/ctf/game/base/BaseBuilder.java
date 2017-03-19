package me.gking2224.mc.mod.ctf.game.base;

import me.gking2224.mc.mod.ctf.game.Bounds;
import me.gking2224.mc.mod.ctf.game.CtfTeam.TeamColour;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface BaseBuilder {
  Bounds buildBase(BlockPos refPos, TeamColour colour, IBlockState ambientBlock,
    boolean invertZ);

  IBlockState getPrimaryMaterial(TeamColour team);
}
