package me.gking2224.mc.mod.ctf.blocks;

import java.util.Random;

import me.gking2224.mc.mod.ctf.game.CtfTeam.TeamColour;
import me.gking2224.mc.mod.ctf.item.ModItems;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class PlacedFlag extends BlockBase {

  public static enum EnumFlagColour implements IStringSerializable {

    RED(TeamColour.RED, 0), BLUE(TeamColour.BLUE, 1);

    static EnumFlagColour[] BY_META = {
        RED, BLUE
    };

    public static EnumFlagColour fromMeta(int meta) {
      return BY_META[meta];
    }

    private TeamColour colour;

    private int meta;

    private EnumFlagColour(TeamColour colour, int meta) {
      this.meta = meta;
      this.colour = colour;
    }

    public int getMeta() {
      return this.meta;
    }

    @Override public String getName() {
      return this.colour.getColour();
    }
  }

  public static final PropertyEnum<EnumFlagColour> COLOUR = PropertyEnum
          .<EnumFlagColour> create("colour", EnumFlagColour.class);

  public static IBlockState withColour(EnumFlagColour colour) {
    return ModBlocks.PLACED_FLAG.getDefaultState().withProperty(COLOUR, colour);
  }

  public PlacedFlag(String name) {
    super(Material.WOOD, name);
    final IBlockState iblockstate = this.blockState.getBaseState();

    this.setDefaultState(iblockstate.withProperty(COLOUR, EnumFlagColour.BLUE));

    this.setHardness(0f);
    this.setResistance(1f);
  }

  @Override public BlockStateContainer createBlockState() {
    return new BlockStateContainer(this, COLOUR);

  }

  @Override public Item getItemDropped(IBlockState state, Random rand,
    int fortune)
  {
    if (state.getValue(COLOUR) == EnumFlagColour.RED) {
      return ModItems.RED_FLAG;
    } else if (state.getValue(COLOUR) == EnumFlagColour.BLUE) {
      return ModItems.BLUE_FLAG;
    } else {
      return null;
    }
  }

  @Override public int getMetaFromState(IBlockState state) {
    return state.getValue(COLOUR).getMeta();
  }

  @Override public IBlockState getStateFromMeta(int meta) {
    return this.getDefaultState().withProperty(COLOUR,
            EnumFlagColour.fromMeta(meta));
  }

  @Override public boolean isFullCube(IBlockState state) {
    return false;
  }

  @Override public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
    return true;
  }
}
