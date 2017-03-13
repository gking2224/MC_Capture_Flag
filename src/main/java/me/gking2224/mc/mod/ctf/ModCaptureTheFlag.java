package me.gking2224.mc.mod.ctf;

import me.gking2224.mc.mod.ctf.blocks.ModBlocks;
import me.gking2224.mc.mod.ctf.blocks.TM1Blocks;
import me.gking2224.mc.mod.ctf.command.BackToBase;
import me.gking2224.mc.mod.ctf.item.ItemBase;
import me.gking2224.mc.mod.ctf.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemPickupEvent;

@Mod(modid = ModCaptureTheFlag.modId, name = ModCaptureTheFlag.name, version = ModCaptureTheFlag.version, acceptedMinecraftVersions = "[1.11.2]")
public class ModCaptureTheFlag {

	public static final String modId = "ctf";
	public static final String name = "Capture The Flag";
	public static final String version = "1.0.0";

	@Mod.Instance(modId)
	public static ModCaptureTheFlag instance;

	@SidedProxy(serverSide = "me.gking2224.mc.mod.ctf.proxy.ServerProxy", clientSide = "me.gking2224.mc.mod.ctf.proxy.ClientProxy")
	public static me.gking2224.mc.mod.ctf.proxy.SidedProxy proxy;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		ModBlocks.init();
		ModItems.init();
		ModBlocks.postItemsInit();
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
	}

	@Mod.EventHandler
	public void serverLoad(FMLServerStartingEvent event) {

		event.registerServerCommand(new BackToBase());
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {

	}

	@SubscribeEvent
	public void registerBlocks(RegistryEvent.Register<Block> event) {
		event.getRegistry().registerAll(TM1Blocks.getBlocks());
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
			if ("flag".equals(item.getName())) {
				System.out.printf("Player %s picked up flag!", player.getName());
			}
		}
	}

}