package me.gking2224.mc.mod.ctf.net;

import me.gking2224.mc.mod.ctf.game.GameManager;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class Response extends ConfirmationResponse {

  public static class Handler implements IMessageHandler<Response, IMessage> {

    @Override public IMessage onMessage(Response msg, MessageContext ctx) {
      if (msg.isConfirmed()) {
        GameManager.get().completeMovePlayerToPosition(
                ctx.getServerHandler().playerEntity, msg.x, msg.y, msg.z);
      }

      return null;
    }
  }

  private final int x;
  private final int y;
  private final int z;

  public Response(int x, int y, int z, boolean b) {
    this(x, y, z, b, null);
  }

  public Response(int x, int y, int z, boolean b, String message) {
    super(b, message);
    this.x = x;
    this.y = y;
    this.z = z;
  }
}
