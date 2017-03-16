package me.gking2224.mc.mod.ctf.util;

import java.util.Collections;
import java.util.Set;

import me.gking2224.mc.mod.ctf.net.CtfNetworkHandler;
import me.gking2224.mc.mod.ctf.net.MoveItemToHand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class InventoryUtils {

	public static ItemStack removeAllSimilarItemsFromPlayerInventory(
			EntityPlayer player, Item item) {

		int count = 0;
		InventoryPlayer inventory = player.inventory;
		int itemId = Item.getIdFromItem(item);
		NonNullList<ItemStack> inv = inventory.mainInventory;
		for (int i = 0; i < inv.size(); ++i) {
			ItemStack slotItem = inv.get(i);
			if (Item.getIdFromItem(slotItem.getItem()) == itemId) {
				inv.set(i, ItemStack.EMPTY);
				count++;
			}
		}
		return new ItemStack(item, count);
	}

	public static void addItemsToPlayerInventory(EntityPlayer player,
			Set<ItemStack> items) {
		items.forEach((i) -> player.inventory.addItemStackToInventory(i));
	}

	public static void moveItemFromInventoryToPlayerHand(EntityPlayer player,
			Item item) {
		
//		ItemStack fromInv = removeAllSimilarItemsFromPlayerInventory(player, item);
		
		ItemStack fromHand = player.getHeldItemMainhand();
		addItemsToPlayerInventory(player, Collections.singleton(fromHand));
		
		//CtfNetworkHandler.INSTANCE.sendTo(new MoveItemToHand(fromInv), (EntityPlayerMP)player);
	}

}
