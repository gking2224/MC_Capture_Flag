package me.gking2224.mc.mod.ctf.proxy;

import me.gking2224.mc.mod.ctf.event.EventHandlerServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;


public class ServerProxy extends CommonProxy {

	@Override
	public void init(FMLInitializationEvent event) {
		EventHandlerServer handler = new EventHandlerServer();
		MinecraftForge.EVENT_BUS.register(handler);
		MinecraftForge.TERRAIN_GEN_BUS.register(handler);
	}
}
