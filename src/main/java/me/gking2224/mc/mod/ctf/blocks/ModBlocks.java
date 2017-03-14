package me.gking2224.mc.mod.ctf.blocks;

import me.gking2224.mc.mod.ctf.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModBlocks {

	public static PlacedFlag FLAG;

	public static void init() {
		FLAG = (PlacedFlag)register(
				new PlacedFlag("placed_flag")
				.setCreativeTab(CreativeTabs.MATERIALS)
				.setIsOpaque(false));
	}

	private static <T extends Block> T register(T block, ItemBlock itemBlock) {
		GameRegistry.register(block);
		GameRegistry.register(itemBlock);

		if (block instanceof BlockBase) {
			((BlockBase)block).registerItemModel(itemBlock);
		}

		return block;
	}

	private static <T extends Block> T register(T block) {
		ItemBlock itemBlock = new ItemBlock(block);
		itemBlock.setRegistryName(block.getRegistryName());
		return register(block, itemBlock);
	}

	public static void postItemsInit() {
		FLAG.setItemDropped(ModItems.RED_FLAG);
	}

}