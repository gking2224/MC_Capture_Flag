package me.gking2224.mc.mod.ctf.item;

import static java.util.Arrays.asList;

import java.util.HashSet;
import java.util.Set;

import me.gking2224.mc.mod.ctf.blocks.ModBlocks;
import me.gking2224.mc.mod.ctf.blocks.PlacedFlag;
import me.gking2224.mc.mod.ctf.game.CtfTeam.TeamColour;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModItems {

  public static ItemBase RED_FLAG;
  public static ItemBase BLUE_FLAG;

  public static Set<ItemBase> ALL_FLAGS = new HashSet<ItemBase>(
          asList(RED_FLAG, BLUE_FLAG));

  public static void init() {
    RED_FLAG = register(
            new ItemBase(Flag.FLAG_PREFIX + TeamColour.RED.getColour())
                    .setCreativeTab(CreativeTabs.MISC).setMaxStackSize(1)
                    .setPlacesAsBlock(ModBlocks.PLACED_FLAG.getDefaultState()
                            .withProperty(PlacedFlag.COLOUR,
                                    PlacedFlag.EnumFlagColour.RED)));
    BLUE_FLAG = register(
            new ItemBase(Flag.FLAG_PREFIX + TeamColour.BLUE.getColour())
                    .setCreativeTab(CreativeTabs.MISC).setMaxStackSize(1)
                    .setPlacesAsBlock(ModBlocks.PLACED_FLAG.getDefaultState()
                            .withProperty(PlacedFlag.COLOUR,
                                    PlacedFlag.EnumFlagColour.BLUE)));
  }

  private static <T extends Item> T register(T item) {
    GameRegistry.register(item);

    if (item instanceof ItemBase) {
      ((ItemBase) item).registerItemModel();
    }

    return item;
  }

}
