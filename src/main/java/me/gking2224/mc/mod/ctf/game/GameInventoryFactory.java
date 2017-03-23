package me.gking2224.mc.mod.ctf.game;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;

public class GameInventoryFactory {

  private static class DefaultBonusChestInventory extends GameInventoryBase {

    @Override public Set<ItemStack> getGameItems() {
      final Set<ItemStack> set = new HashSet<ItemStack>();
      set.add(new ItemStack(Items.DIAMOND_SWORD, 1));
      set.add(new ItemStack(Items.DIAMOND_AXE, 1));
      set.add(new ItemStack(Items.DIAMOND_PICKAXE, 1));
      set.add(new ItemStack(Items.DIAMOND_SHOVEL, 1));
      set.add(new ItemStack(Items.ARROW, 64));
      set.add(new ItemStack(Items.ARROW, 64));
      set.add(new ItemStack(Items.ARROW, 64));
      set.add(new ItemStack(Items.COOKED_BEEF, 64));
      set.add(new ItemStack(Items.DIAMOND_LEGGINGS, 1));
      set.add(new ItemStack(Items.DIAMOND_CHESTPLATE, 1));
      set.add(new ItemStack(Items.DIAMOND_BOOTS, 1));
      set.add(new ItemStack(Items.DIAMOND_HELMET, 1));
      return set;
    }
  }

  private static class DefaultGameInventory extends GameInventoryBase {

    @Override public Set<ItemStack> getGameItems() {
      final Set<ItemStack> set = new HashSet<ItemStack>();
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

  protected static abstract class GameInventoryBase implements GameInventory {

    @Override public final void placeInChest(TileEntityChest chest) {
      final Iterator<ItemStack> it = this.getGameItems().iterator();
      for (int i = 0; it.hasNext(); i++) {
        chest.setInventorySlotContents(i, it.next());
      }
    }
  }

  public static final String DEFAULT_BONUS_CHEST = "defaultBonusChest";
  public static final String DEFAULT = "default";

  public static GameInventory get(String inventory) {

    switch (inventory) {
    case DEFAULT_BONUS_CHEST:
      return new DefaultBonusChestInventory();
    case DEFAULT:
    default:
      return new DefaultGameInventory();
    }
  }
}
