package me.gking2224.mc.mod.ctf.proxy;

import net.minecraft.item.Item;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

public interface SidedProxy {

	void registerItemRenderer(Item item, int meta, String id);

	void init(FMLInitializationEvent event);

	void serverLoad(FMLServerStartingEvent event);

}
