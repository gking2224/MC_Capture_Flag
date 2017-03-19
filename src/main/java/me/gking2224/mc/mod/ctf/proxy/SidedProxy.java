package me.gking2224.mc.mod.ctf.proxy;

import net.minecraft.item.Item;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

public interface SidedProxy {

  void init(FMLInitializationEvent event);

  void registerItemRenderer(Item item, int meta, String id);

  void serverLoad(FMLServerStartingEvent event);

}
