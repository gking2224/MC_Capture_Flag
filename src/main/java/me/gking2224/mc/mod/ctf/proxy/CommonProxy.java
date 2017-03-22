package me.gking2224.mc.mod.ctf.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.gking2224.mc.mod.ctf.event.EventHandlerCommon;
import me.gking2224.mc.mod.ctf.game.GameManager;
import me.gking2224.mc.mod.ctf.net.CanMovePlayerToPosition;
import me.gking2224.mc.mod.ctf.net.CtfNetworkHandler;
import me.gking2224.mc.mod.ctf.net.MoveItemToHand;
import me.gking2224.mc.mod.ctf.net.Response;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.relauncher.Side;

public abstract class CommonProxy implements CtfSidedProxy {

  private static List<Class<? extends IMessage>> messageClasses = new ArrayList<Class<? extends IMessage>>();

  private static Map<Class<? extends IMessage>, Side> messageHandlingSides = new java.util.HashMap<Class<? extends IMessage>, Side>();

  static {
    messageClasses.add(Response.class);
    messageClasses.add(CanMovePlayerToPosition.class);
    messageClasses.add(MoveItemToHand.class);
  }

  protected abstract EventHandlerCommon getEventHandler();

  @Override public Class<? extends IMessageHandler<? extends IMessage, ? extends IMessage>> getHandlerClassForMessage(
    Class<? extends IMessage> messageClas)
  {
    if (this.getHandledMessages().containsKey(messageClas)) {
      return this.getHandledMessages().get(messageClas);
    } else {
      return CtfNetworkHandler.dummyClass(messageClas);
    }
  }

  protected abstract Side getSide();

  @Override public void init(FMLInitializationEvent event) {
    MinecraftForge.EVENT_BUS.register(this.getEventHandler());
    this.initNetworkMessages();
  }

  @SuppressWarnings({
      "unchecked", "rawtypes"
  }) @Override public void initNetworkMessages() {
    messageClasses.forEach(rq -> CtfNetworkHandler.registerForSide(
            (Class) this.getHandlerClassForMessage(rq), rq, this.getSide(),
            messageHandlingSides.get(rq)));
  }

  @Override public void registerItemRenderer(Item item, int meta, String id) {

  }

  @Override public void serverLoad(FMLServerStartingEvent event) {
    GameManager.initialise(event.getServer());
    GameManager.get().getGameCommands()
            .forEach((cmd) -> event.registerServerCommand(cmd));
  }
}
