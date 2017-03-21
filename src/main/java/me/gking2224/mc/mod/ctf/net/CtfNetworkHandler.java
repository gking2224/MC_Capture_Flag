package me.gking2224.mc.mod.ctf.net;

import me.gking2224.mc.mod.ctf.ModCaptureTheFlag;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class CtfNetworkHandler {

  public static class DummyHandler<REQ extends IMessage>
          implements IMessageHandler<REQ, IMessage>
  {
    @Override public IMessage onMessage(REQ message, MessageContext ctx) {
      return null;
    }
  }

  private static int discriminator = 0;

  public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE
          .newSimpleChannel(ModCaptureTheFlag.modId);

  @SuppressWarnings("unchecked") public static Class<? extends IMessageHandler<? extends IMessage, ? extends IMessage>> dummyClass(
    final Class<? extends IMessage> clazz)
  {
    @SuppressWarnings("rawtypes") final IMessageHandler<? extends IMessage, ? extends IMessage> h = new DummyHandler();
    final Class<IMessageHandler<? extends IMessage, ? extends IMessage>> c = (Class<IMessageHandler<? extends IMessage, ? extends IMessage>>) h
            .getClass();
    return c;
  }

  // @SuppressWarnings("unused") private static <T extends IMessage> Class<?
  // extends IMessageHandler<? extends IMessage, ? extends IMessage>>
  // dummyIfNotClient(
  // Side side, Class<T> clazz,
  // Class<? extends IMessageHandler<? extends IMessage, ? extends IMessage>>
  // handlerClass)
  // {
  // return (side == Side.CLIENT) ? handlerClass : dummyClass(clazz);
  // }
  //
  // @SuppressWarnings("unused") private static <T extends IMessage> Class<?
  // extends IMessageHandler<? extends IMessage, ? extends IMessage>>
  // dummyIfNotServer(
  // Side side, Class<T> clazz,
  // Class<? extends IMessageHandler<? extends IMessage, ? extends IMessage>>
  // handlerClass)
  // {
  // return dummyIfNotSide(side, Side.SERVER, clazz, handlerClass);
  // }

  @SuppressWarnings("unchecked") private static <REQ extends IMessage, RES extends IMessage> Class<? extends IMessageHandler<REQ, RES>> dummyIfNotSide(
    Side side1, Side side2, Class<REQ> clazz,
    Class<? extends IMessageHandler<REQ, RES>> handlerClass)
  {
    return (side1 == side2) ? handlerClass
            : (Class<? extends IMessageHandler<REQ, RES>>) dummyClass(clazz);
  }

  // public static void registerClientMessages() {
  // INSTANCE.registerMessage(MoveItemToHand.Handler.class,
  // MoveItemToHand.class,
  // 0, Side.CLIENT);
  // INSTANCE.registerMessage(CanMovePlayerToPosition.Handler.class,
  // CanMovePlayerToPosition.class, 1, Side.CLIENT);
  // }
  //
  // public static void registerDummyClientHandlers() {
  // INSTANCE.registerMessage(dummyClass(Response.class), Response.class, 2,
  // Side.CLIENT);
  // }
  //
  // public static void registerDummyServerHandlers() {
  // INSTANCE.registerMessage(DummyHandler.class, MoveItemToHand.class, 0,
  // Side.SERVER);
  // INSTANCE.registerMessage(DummyHandler.class, CanMovePlayerToPosition.class,
  // 1, Side.SERVER);
  // }

  public static <REQ extends IMessage, RES extends IMessage> void registerForSide(
    Class<? extends IMessageHandler<REQ, RES>> handlerClass,
    Class<REQ> messageClass, Side sideOn, Side sideToRegister)
  {
    // final Class<? extends IMessageHandler<REQ, RES>> handlerOrDummy =
    // dummyIfNotSide(
    // sideOn, sideToRegister, messageClass, handlerClass);
    System.out.printf("Registering %s to handle %s\n", handlerClass.getName(),
            messageClass.getName());

    INSTANCE.registerMessage(handlerClass, messageClass, discriminator++,
            sideOn);
  }

  // public static void registerMessages() {}
  //
  // public static void registerServerMessages() {
  // INSTANCE.registerMessage(Response.Handler.class, Response.class, 2,
  // Side.SERVER);
  // }
}
