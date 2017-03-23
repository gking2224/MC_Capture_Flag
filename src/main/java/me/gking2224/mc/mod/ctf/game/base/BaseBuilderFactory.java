package me.gking2224.mc.mod.ctf.game.base;

import static net.minecraft.block.Block.getIdFromBlock;

import me.gking2224.mc.mod.ctf.game.Bounds;
import me.gking2224.mc.mod.ctf.game.CtfTeam.TeamColour;
import me.gking2224.mc.mod.ctf.game.Game;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BaseBuilderFactory {

  static class DefaultBaseBuilder implements BaseBuilder {

    private static final int WOOL_BLUE_ID = 11;
    private static final int WOOL_RED_ID = 14;

    private static Block getPrimaryBaseBlock() {
      return Blocks.WOOL;
    }

    private final World world;

    public DefaultBaseBuilder(MinecraftServer server) {
      this.world = server.getEntityWorld();
    }

    @Override public BaseDescription buildBase(BlockPos refPos,
      TeamColour colour, IBlockState ambientBlock, boolean invertZ)
    {
      this.world.setBlockState(refPos, this.getPrimaryMaterial(colour));
      return new BaseDescription(new Bounds(refPos, refPos));
    }

    private int getPrimaryBaseStateIdModifier(TeamColour colour) {
      return (colour == TeamColour.BLUE) ? WOOL_BLUE_ID : WOOL_RED_ID;
    }

    @Override public IBlockState getPrimaryMaterial(TeamColour colour) {
      return Block.getStateById(getIdFromBlock(getPrimaryBaseBlock())
              + (this.getPrimaryBaseStateIdModifier(colour) << 12));
    }
  }

  private static BaseBuilder defaultBaseBuilder = null;

  private static final Object DEFAULT = "default";

  public static BaseBuilder defaultBaseBuilder(MinecraftServer server) {
    if (defaultBaseBuilder == null) {
      defaultBaseBuilder = new DefaultBaseBuilder(server);
    }
    return defaultBaseBuilder;
  }

  public static BaseBuilder getBaseBuilder(MinecraftServer server, Game game) {
    final String baseType = game.getOptions().getString("base")
            .orElse("default");
    if (DEFAULT.equals(baseType)) {
      return defaultBaseBuilder(server);
    } else {
      return new ConfigBaseBuilder(server, game, baseType);
    }
  }

}
