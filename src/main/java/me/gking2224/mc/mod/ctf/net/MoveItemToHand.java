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

	private ItemStack itemStack;

	public MoveItemToHand() {
	}

	public MoveItemToHand(ItemStack stack) {
		this.itemStack = stack;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		int id = Item.getIdFromItem(this.itemStack.getItem());
		buf.writeInt(id);
		int quantity = this.itemStack.getCount();
		buf.writeInt(quantity);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		int id = buf.readInt();
		int quantity = buf.readInt();
		
		this.itemStack = new ItemStack(Item.getItemById(id), quantity);
	}

	public ItemStack getItemStack() {
		return this.itemStack;
	}
	
	public static class MoveItemToHandMessageHandler implements IMessageHandler<MoveItemToHand, IMessage> {
		@Override
		public IMessage onMessage(MoveItemToHand msg, MessageContext ctx) {
			EntityPlayer player = Minecraft.getMinecraft().player;
			new Thread(() -> {
				try { Thread.sleep(500); } catch (Exception e) {}
				int slot = player.inventory.getSlotFor(msg.getItemStack());
				System.out.printf("%s in slot %d\n", msg.getItemStack().getDisplayName(), slot);
				//if (slot != -1) player.inventory.pickItem(slot);
			}).start();
			return null;
		}
	}
}
