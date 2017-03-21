package me.gking2224.mc.mod.ctf.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class CanMovePlayerToPosition implements IMessage {

  public static class Handler
          implements IMessageHandler<CanMovePlayerToPosition, Response>
  {
    @Override public Response onMessage(CanMovePlayerToPosition msg,
      MessageContext ctx)
    {
      final ChunkProviderClient chunkProvider = Minecraft.getMinecraft().world
              .getChunkProvider();
      if (chunkProvider.isChunkGeneratedAt(msg.x, msg.z)) {
        try {
          chunkProvider.loadChunk(msg.x, msg.z);
        } catch (final Exception e) {
          return new Response(msg.x, msg.y, msg.z, false, e.getMessage());
        }
      }
      return new Response(msg.x, msg.y, msg.z, true);
    }
  }

  private int x;
  private int y;
  private int z;

  public CanMovePlayerToPosition() {}

  public CanMovePlayerToPosition(int x, int y, int z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  @Override public void fromBytes(ByteBuf buf) {
    this.x = buf.readInt();
    this.z = buf.readInt();
  }

  @Override public void toBytes(ByteBuf buf) {
    buf.writeInt(this.x);
    buf.writeInt(this.z);
  }
}
