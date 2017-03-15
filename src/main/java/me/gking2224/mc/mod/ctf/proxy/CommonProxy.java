package me.gking2224.mc.mod.ctf.proxy;

import me.gking2224.mc.mod.ctf.command.BackToBase;
import me.gking2224.mc.mod.ctf.command.CurrentGame;
import me.gking2224.mc.mod.ctf.command.JoinCtfGame;
import me.gking2224.mc.mod.ctf.command.NewCtfGame;
import me.gking2224.mc.mod.ctf.event.EventHandlerCommon;
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
		event.registerServerCommand(new BackToBase());
		event.registerServerCommand(new NewCtfGame());
		event.registerServerCommand(new JoinCtfGame());
		event.registerServerCommand(new CurrentGame());
		System.out.println("server load");
	}
}
