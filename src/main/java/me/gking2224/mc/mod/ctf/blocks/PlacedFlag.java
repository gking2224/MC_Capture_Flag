package me.gking2224.mc.mod.ctf.blocks;

import java.util.Random;

import me.gking2224.mc.mod.ctf.item.ModItems;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.IStringSerializable;

public class PlacedFlag extends BlockBase {

    public static final PropertyEnum<EnumFlagColour> COLOUR = PropertyEnum.<EnumFlagColour>create("colour", EnumFlagColour.class);

	public PlacedFlag(String name) {
		super(Material.WOOD, name);
        IBlockState iblockstate = this.blockState.getBaseState();

        this.setDefaultState(iblockstate.withProperty(COLOUR, EnumFlagColour.BLUE));

		setHardness(0f);
		setResistance(1f);
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
		if (state.getValue(COLOUR) == EnumFlagColour.RED) {
			return ModItems.RED_FLAG;
		}
		else if (state.getValue(COLOUR) == EnumFlagColour.BLUE) {
			return ModItems.BLUE_FLAG;
		}
		else return null;
    }
	
	@Override
	public BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, COLOUR);
		
	}
	
	@Override public int getMetaFromState(IBlockState state) {
		return state.getValue(COLOUR).getMeta();
	}
	
	@Override public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(COLOUR, EnumFlagColour.fromMeta(meta));
	}
	
	public static enum EnumFlagColour implements IStringSerializable{
		
		RED("red", 0),
		BLUE("blue", 1);
		
		static EnumFlagColour[] BY_META = {RED, BLUE};
		
		private String colour;
		private int meta;
		
		private EnumFlagColour(String colour, int meta) {
			this.meta = meta;
			this.colour = colour;
		}

		@Override
		public String getName() {
			return this.colour;
		}
		
		public int getMeta() {
			return meta;
		}
		
		public static EnumFlagColour fromMeta(int meta) {
			return BY_META[meta];
		}
	}
	
	public static IBlockState withColour(EnumFlagColour colour) {
		return ModBlocks.PLACED_FLAG.getDefaultState().withProperty(COLOUR, colour);
	}
}