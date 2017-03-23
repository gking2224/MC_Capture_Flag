package me.gking2224.mc.mod.ctf.game;

import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;

public interface GameInventory {

  Set<ItemStack> getGameItems();

  void placeInChest(TileEntityChest chest);

}
