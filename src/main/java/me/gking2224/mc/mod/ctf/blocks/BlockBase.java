package me.gking2224.mc.mod.ctf.blocks;

import java.util.Random;

import me.gking2224.mc.mod.ctf.ModCaptureTheFlag;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

public class BlockBase extends Block {

  protected String name;
  protected Item itemDropped;
  protected boolean isOpaque = true;

  public BlockBase(Material material, String name) {
    super(material);

    this.name = name;

    setUnlocalizedName(name);
    setRegistryName(name);
  }

  public void registerItemModel(ItemBlock itemBlock) {
    ModCaptureTheFlag.proxy.registerItemRenderer(itemBlock, 0, name);
  }

  @Override public BlockBase setCreativeTab(CreativeTabs tab) {
    super.setCreativeTab(tab);
    return this;
  }

  public BlockBase setIsOpaque(boolean isOpaque) {
    this.isOpaque = isOpaque;
    return this;
  }

  @Override public boolean isOpaqueCube(IBlockState state) {
    return isOpaque;
  }

  @Override public Item getItemDropped(IBlockState state, Random rand,
    int fortune)
  {
    if (itemDropped != null) return itemDropped;
    else return super.getItemDropped(state, rand, fortune);
  }

  public BlockBase setItemDropped(Item item) {
    this.itemDropped = item;
    return this;
  }

}
