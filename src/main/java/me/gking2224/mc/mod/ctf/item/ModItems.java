package me.gking2224.mc.mod.ctf.item;

import me.gking2224.mc.mod.ctf.blocks.ModBlocks;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModItems {

	public static ItemBase RED_FLAG;
	public static ItemBase BLUE_FLAG;

	public static void init() {
		RED_FLAG = register(new ItemBase("flag_red").setCreativeTab(CreativeTabs.MISC).setMaxStackSize(1).setPlacesAsBlock(ModBlocks.RED_FLAG));
		BLUE_FLAG = register(new ItemBase("flag_blue").setCreativeTab(CreativeTabs.MISC).setMaxStackSize(1).setPlacesAsBlock(ModBlocks.BLUE_FLAG));
	}

	private static <T extends Item> T register(T item) {
		GameRegistry.register(item);

		if (item instanceof ItemBase) {
			((ItemBase)item).registerItemModel();
		}

		return item;
	}

}