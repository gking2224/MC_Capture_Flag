package me.gking2224.mc.mod.ctf.proxy;

import me.gking2224.mc.mod.ctf.event.EventHandlerCommon;
import me.gking2224.mc.mod.ctf.game.GameManager;
import me.gking2224.mc.mod.ctf.net.CtfNetworkHandler;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;


public class CommonProxy implements SidedProxy {

	private EventHandlerCommon handler = new EventHandlerCommon();

	@Override
	public void registerItemRenderer(Item item, int meta, String id) {
		
	}

	@Override
	public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(handler);
	}

	@Override
	public void serverLoad(FMLServerStartingEvent event) {
		GameManager.initialise(event.getServer());
		GameManager.get().getGameCommands().forEach((cmd) -> event.registerServerCommand(cmd));
		CtfNetworkHandler.registerMessages();
	}
}
