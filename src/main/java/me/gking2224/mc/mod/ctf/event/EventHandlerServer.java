package me.gking2224.mc.mod.ctf.event;

import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventHandlerServer {

	@SubscribeEvent
	public void event(InitMapGenEvent event) {
		System.out.println("InitMapGenEvent");
	}
}
