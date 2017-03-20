package me.gking2224.mc.mod.ctf.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;

public class FlagHolder extends BlockBase {

  public FlagHolder(String name) {
    super(Material.WOOD, name);
    final IBlockState iblockstate = this.blockState.getBaseState();

    this.setHardness(999f);
    this.setResistance(1f);

    this.setDefaultState(iblockstate);
  }

  @Override public BlockStateContainer createBlockState() {
    return new BlockStateContainer(this);
  }
}
