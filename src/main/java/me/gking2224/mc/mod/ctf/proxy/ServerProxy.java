package me.gking2224.mc.mod.ctf.proxy;

import me.gking2224.mc.mod.ctf.event.EventHandlerServer;
import me.gking2224.mc.mod.ctf.game.GameManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;


public class ServerProxy extends CommonProxy {

	@Override
	public void init(FMLInitializationEvent event) {
		super.init(event);
		MinecraftForge.TERRAIN_GEN_BUS.register(new EventHandlerServer());
	}

	@Override
	public void serverLoad(FMLServerStartingEvent event) {
		super.serverLoad(event);
		GameManager.initialise(event.getServer());
		return;
	}
}
