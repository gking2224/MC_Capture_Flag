package me.gking2224.mc.mod.ctf.proxy;

import java.util.HashMap;
import java.util.Map;

import me.gking2224.mc.mod.ctf.event.EventHandlerCommon;
import me.gking2224.mc.mod.ctf.event.EventHandlerServer;
import me.gking2224.mc.mod.ctf.game.GameManager;
import me.gking2224.mc.mod.ctf.net.Response;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.relauncher.Side;

public class ServerProxy extends CommonProxy {

  private static final Map<Class<? extends IMessage>, Class<? extends IMessageHandler<? extends IMessage, ? extends IMessage>>> handledMessages = new HashMap<Class<? extends IMessage>, Class<? extends IMessageHandler<? extends IMessage, ? extends IMessage>>>();

  private static final EventHandlerServer handler = new EventHandlerServer();

  static {
    handledMessages.put(Response.class, Response.Handler.class);
  }

  @Override protected EventHandlerCommon getEventHandler() {
    return handler;
  }

  @Override public Map<Class<? extends IMessage>, Class<? extends IMessageHandler<? extends IMessage, ? extends IMessage>>> getHandledMessages() {
    return handledMessages;
  }

  @Override protected Side getSide() {
    return Side.SERVER;
  }

  @Override public void init(FMLInitializationEvent event) {
    super.init(event);
    MinecraftForge.TERRAIN_GEN_BUS.register(handler);
  }

  @Override public void serverLoad(FMLServerStartingEvent event) {
    GameManager.initialise(event.getServer());
    GameManager.get().getGameCommands()
            .forEach((cmd) -> event.registerServerCommand(cmd));
  }
}
