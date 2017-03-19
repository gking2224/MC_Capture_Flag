package me.gking2224.mc.mod.ctf.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MoveItemToHand implements IMessage {

  public static class MoveItemToHandMessageHandler
          implements IMessageHandler<MoveItemToHand, IMessage>
  {
    @Override public IMessage onMessage(MoveItemToHand msg,
      MessageContext ctx)
    {
      final EntityPlayer player = Minecraft.getMinecraft().player;
      new Thread(() -> {
        try {
          Thread.sleep(500);
        } catch (final Exception e) {}
        final int slot = player.inventory.getSlotFor(msg.getItemStack());
        System.out.printf("%s in slot %d\n",
                msg.getItemStack().getDisplayName(), slot);
        // if (slot != -1) player.inventory.pickItem(slot);
      }).start();
      return null;
    }
  }

  private ItemStack itemStack;

  public MoveItemToHand() {}

  public MoveItemToHand(ItemStack stack) {
    this.itemStack = stack;
  }

  @Override public void fromBytes(ByteBuf buf) {
    final int id = buf.readInt();
    final int quantity = buf.readInt();

    this.itemStack = new ItemStack(Item.getItemById(id), quantity);
  }

  public ItemStack getItemStack() {
    return this.itemStack;
  }

  @Override public void toBytes(ByteBuf buf) {
    final int id = Item.getIdFromItem(this.itemStack.getItem());
    buf.writeInt(id);
    final int quantity = this.itemStack.getCount();
    buf.writeInt(quantity);
  }
}
