package me.gking2224.mc.mod.ctf.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import me.gking2224.mc.mod.ctf.net.CtfNetworkHandler;
import me.gking2224.mc.mod.ctf.net.MoveItemToHand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.NonNullList;

public class InventoryUtils {

  public static void addItemsToChest(Set<ItemStack> gameItems,
    TileEntityChest chest)
  {
    final Iterator<ItemStack> it = gameItems.iterator();
    for (int i = 0; it.hasNext(); i++) {
      chest.setInventorySlotContents(i, it.next());
    }
  }

  public static void addItemsToPlayerInventory(EntityPlayer player,
    Set<ItemStack> items)
  {
    items.forEach((i) -> player.inventory.addItemStackToInventory(i));
  }

  public static void clearPlayerInventory(EntityPlayer p) {
    for (int i = 0; i < p.inventory.getSizeInventory(); i++) {
      p.inventory.removeStackFromSlot(i);
    }
  }

  public static void moveItemFromInventoryToPlayerHand(EntityPlayer player,
    Item item)
  {

    final ItemStack fromHand = player.getHeldItemMainhand();
    System.out.println(String.format("%s has %s in his hand\n",
            player.getName(), fromHand.getDisplayName()));
    addItemsToPlayerInventory(player, Collections.singleton(fromHand));

    CtfNetworkHandler.INSTANCE.sendTo(new MoveItemToHand(new ItemStack(item)),
            (EntityPlayerMP) player);
  }

  public static ItemStack removeAllSimilarItemsFromPlayerInventory(
    EntityPlayer player, Item item)
  {
    int count = 0;
    final InventoryPlayer inventory = player.inventory;
    final int itemId = Item.getIdFromItem(item);
    final NonNullList<ItemStack> inv = inventory.mainInventory;
    for (int i = 0; i < inv.size(); ++i) {
      final ItemStack slotItem = inv.get(i);
      if (Item.getIdFromItem(slotItem.getItem()) == itemId) {
        inv.set(i, ItemStack.EMPTY);
        count++;
      }
    }
    return new ItemStack(item, count);
  }

  public static void setPlayerInventory(EntityPlayer p,
    Set<ItemStack> gameItems)
  {
    clearPlayerInventory(p);
    gameItems.forEach((s) -> p.inventory.addItemStackToInventory(s));
  }
}
