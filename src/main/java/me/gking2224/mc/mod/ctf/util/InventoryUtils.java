package me.gking2224.mc.mod.ctf.util;

import java.util.Set;

import me.gking2224.mc.mod.ctf.item.ItemBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class InventoryUtils {

	public static void removeAllSimilarItemsFromPlayerInventory(
			EntityPlayer player, Item item) {

		InventoryPlayer inventory = player.inventory;
		int itemId = Item.getIdFromItem(item);
		NonNullList<ItemStack> inv = inventory.mainInventory;
		for (int i = 0; i < inv.size(); ++i) {
			if (Item.getIdFromItem(inv.get(i).getItem()) == itemId) {
				inv.set(i, ItemStack.EMPTY);
			}
		}
	}

	public static void addItemsToPlayerInventory(EntityPlayer player,
			Set<ItemStack> items) {
		items.forEach((i) -> player.inventory.addItemStackToInventory(i));
	}

}
