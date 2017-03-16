package me.gking2224.mc.mod.ctf.event;

import java.util.Optional;

import me.gking2224.mc.mod.ctf.blocks.TM1Blocks;
import me.gking2224.mc.mod.ctf.game.event.GameEventManager;
import me.gking2224.mc.mod.ctf.item.Flag;
import me.gking2224.mc.mod.ctf.item.ItemBase;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventHandlerCommon {

	@SubscribeEvent
	public void registerBlocks(RegistryEvent.Register<Block> event) {
		System.out.printf("block registered: %s", event.getGenericType().getTypeName());
		event.getRegistry().registerAll(TM1Blocks.getBlocks());
	}

	@SubscribeEvent
	public void serverChat(ServerChatEvent event) {
		EntityPlayer player = event.getPlayer();
		System.out.printf("Got message %s from player %s", event.getMessage(), player.getName());
	}

	@SubscribeEvent
	public void itemPickup(EntityItemPickupEvent event) {
		Optional<ItemBase> f = Flag.toFlag(event.getItem().getEntityItem());
		f.ifPresent(flag -> GameEventManager.get().playerPickedUpFlag(event.getEntityPlayer().getName(), flag));
	}
}
