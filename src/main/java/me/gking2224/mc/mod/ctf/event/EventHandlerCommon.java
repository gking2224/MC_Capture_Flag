package me.gking2224.mc.mod.ctf.event;

import com.mojang.realmsclient.dto.RealmsServer.WorldType;

import me.gking2224.mc.mod.ctf.blocks.TM1Blocks;
import me.gking2224.mc.mod.ctf.item.ItemBase;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemPickupEvent;

public class EventHandlerCommon {

	@SubscribeEvent
	public void registerBlocks(RegistryEvent.Register<Block> event) {
		System.out.printf("block registered: %s", event.getGenericType().getTypeName());
		event.getRegistry().registerAll(TM1Blocks.getBlocks());
	}

	@SubscribeEvent
	public void worldLoaded(Load load) {
		net.minecraft.world.WorldType worldType = load.getWorld().getWorldType();
		System.out.println("world loaded");
	}
	
	@SubscribeEvent
	public void serverChat(ServerChatEvent event) {
		EntityPlayer player = event.getPlayer();
		System.out.printf("Got message %s from player %s", event.getMessage(), player.getName());
	}

	@SubscribeEvent
	public void playerJoined(EntityJoinWorldEvent event) {
		
	}

	@SubscribeEvent
	public void itemPickup(ItemPickupEvent event) {
		EntityPlayer player = event.player;
		Item pickedUpItem = event.pickedUp.getEntityItem().getItem();
		if (ItemBase.class.isAssignableFrom(pickedUpItem.getClass())) {
			ItemBase item = (ItemBase)pickedUpItem;
			if (item.getName().startsWith("flag")) {
				System.out.printf("Player %s picked up flag!", player.getName());
			}
		}
	}
}
