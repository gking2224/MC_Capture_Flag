package me.gking2224.mc.mod.ctf.net;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class CtfNetworkHandler {
	public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("testmod1");

	private static int discriminator = 0;
	public static void registerMessages() {
		INSTANCE.registerMessage(
				MoveItemToHand.MoveItemToHandMessageHandler.class,
				MoveItemToHand.class,
				discriminator++,
				Side.CLIENT); 
	}
}
