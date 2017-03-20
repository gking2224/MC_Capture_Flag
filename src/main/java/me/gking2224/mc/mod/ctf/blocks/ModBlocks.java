package me.gking2224.mc.mod.ctf.blocks;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModBlocks {

  public static PlacedFlag PLACED_FLAG;
  public static FlagHolder FLAG_HOLDER;

  public static void init() {
    PLACED_FLAG = (PlacedFlag) register(new PlacedFlag("placed_flag")
            .setCreativeTab(CreativeTabs.MATERIALS).setIsOpaque(false));
    FLAG_HOLDER = (FlagHolder) register(new FlagHolder("flag_holder")
            .setCreativeTab(CreativeTabs.MATERIALS).setIsOpaque(false));
  }

  public static void postItemsInit() {}

  private static <T extends Block> T register(T block) {
    final ItemBlock itemBlock = new ItemBlock(block);
    itemBlock.setRegistryName(block.getRegistryName());
    return register(block, itemBlock);
  }

  /**
   * TODO: change this
   */
  private static <T extends Block> T register(T block, ItemBlock itemBlock) {
    GameRegistry.register(block);
    GameRegistry.register(itemBlock);

    if (block instanceof BlockBase) {
      ((BlockBase) block).registerItemModel(itemBlock);
    }

    return block;
  }

}
