package me.gking2224.mc.mod.ctf.item;

import me.gking2224.mc.mod.ctf.ModCaptureTheFlag;
import me.gking2224.mc.mod.ctf.blocks.BlockBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemBase extends Item {

	protected String name;
	private IBlockState placesAsBlock;

	public ItemBase(String name) {
		this.name = name;
		setUnlocalizedName(name);
		setRegistryName(name);
	}

	public void registerItemModel() {
		ModCaptureTheFlag.proxy.registerItemRenderer(this, 0, name);
	}

	@Override
	public ItemBase setCreativeTab(CreativeTabs tab) {
		super.setCreativeTab(tab);
		return this;
	}

	@Override
	public ItemBase setMaxStackSize(int size) {
		super.setMaxStackSize(size);
		return this;
	}
	
	
	public ItemBase setPlacesAsBlock(IBlockState blockState) {
		this.placesAsBlock = blockState;
		return this;
	}
	
	public String getName() {
		return name;
	}

    /**
     * Called when a Block is right-clicked with this Item
     */
	@Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
		if (this.placesAsBlock != null) {
			return placeAsBlock(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
		}
		else return EnumActionResult.PASS;
    }
    
    public EnumActionResult placeAsBlock(
    		EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

        boolean targetBlockReplaceable = worldIn.getBlockState(pos).getBlock().isReplaceable(worldIn, pos);
        BlockPos blockpos = targetBlockReplaceable ? pos : pos.offset(facing);
        ItemStack itemstack = player.getHeldItem(hand);

        if (
        		player.canPlayerEdit(blockpos, facing, itemstack) &&
        		worldIn.mayPlace(worldIn.getBlockState(blockpos).getBlock(), blockpos, false, facing, (Entity)null) &&
        		this.placesAsBlock.getBlock().canPlaceBlockAt(worldIn, blockpos)
		) {
            itemstack.shrink(1);
            worldIn.setBlockState(blockpos, this.placesAsBlock);
            return EnumActionResult.SUCCESS;
        }
        else
        {
            return EnumActionResult.FAIL;
        }
    }

}