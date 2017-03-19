package me.gking2224.mc.mod.ctf.game;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class GameInventoryFactory {

  private static GameInventory DEFAULT = new DefaultGameInventory();

  public static GameInventory getDefault() {
    return DEFAULT;
  }

  private static class DefaultGameInventory implements GameInventory {

    @Override public Set<ItemStack> getGameItems() {
      Set<ItemStack> set = new HashSet<ItemStack>();
      set.add(new ItemStack(Items.WOODEN_SWORD, 1));
      set.add(new ItemStack(Items.WOODEN_AXE, 1));
      set.add(new ItemStack(Items.WOODEN_PICKAXE, 1));
      set.add(new ItemStack(Items.WOODEN_SHOVEL, 1));
      set.add(new ItemStack(Items.BOW, 1));
      set.add(new ItemStack(Items.ARROW, 10));
      set.add(new ItemStack(Items.COOKED_BEEF, 10));
      set.add(new ItemStack(Items.BOAT, 1));
      set.add(new ItemStack(Blocks.TORCH, 64));
      set.add(new ItemStack(Items.SADDLE, 1));
      return set;
    }
  }
}
