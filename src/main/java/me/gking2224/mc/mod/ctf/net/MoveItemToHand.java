package me.gking2224.mc.mod.ctf.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
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
	
	public static class MoveItemToHandMessageHandler implements IMessageHandler<MoveItemToHand, MoveItemToHand> {

		@Override
		public MoveItemToHand onMessage(MoveItemToHand msg, MessageContext ctx) {
			// This is the player the packet was sent to the server from
			EntityPlayerMP player = ctx.getServerHandler().playerEntity;
			Minecraft.getMinecraft().addScheduledTask(() -> player.inventory.setPickedItemStack(msg.getItemStack()));
			
			return null;
		}
	}
}
