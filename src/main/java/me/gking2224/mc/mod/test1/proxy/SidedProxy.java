package me.gking2224.mc.mod.test1.proxy;

import net.minecraft.item.Item;

public interface SidedProxy {

	void registerItemRenderer(Item item, int meta, String id);

}
