package me.gking2224.mc.mod.ctf.proxy;

import java.util.Map;

import net.minecraft.item.Item;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;

public interface CtfSidedProxy {

  Map<Class<? extends IMessage>, Class<? extends IMessageHandler<? extends IMessage, ? extends IMessage>>> getHandledMessages();

  Class<? extends IMessageHandler<? extends IMessage, ? extends IMessage>> getHandlerClassForMessage(
    Class<? extends IMessage> messageClas);

  void init(FMLInitializationEvent event);

  <REQ extends IMessage, RES extends IMessage> void initNetworkMessages();

  void registerItemRenderer(Item item, int meta, String id);

  void serverLoad(FMLServerStartingEvent event);

}
