package me.gking2224.mc.mod.ctf.game.base;

import static me.gking2224.mc.mod.ctf.util.WorldUtils.maximumBounds;
import static me.gking2224.mc.mod.ctf.util.WorldUtils.offset;
import static me.gking2224.mc.mod.ctf.util.WorldUtils.placeBlocks;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import me.gking2224.mc.mod.ctf.game.Bounds;
import me.gking2224.mc.mod.ctf.game.CtfTeam.TeamColour;
import me.gking2224.mc.mod.ctf.game.Game;
import me.gking2224.mc.mod.ctf.game.MutableBounds;
import me.gking2224.mc.mod.ctf.util.WorldUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ConfigBaseBuilder implements BaseBuilder {

  private static class BaseCollector
          implements Collector<BuildInstruction, MutableBounds, Bounds>
  {

    private final BlockPos refPos;
    private final World world;

    public BaseCollector(World world, BlockPos refPos) {
      this.refPos = refPos;
      this.world = world;
    }

    @Override public BiConsumer<MutableBounds, BuildInstruction> accumulator() {
      return (MutableBounds cb, BuildInstruction bi) -> placeBlocks(this.world,
              offset(this.refPos, bi.getBounds()), bi.getBlockState());
    }

    @Override public Set<java.util.stream.Collector.Characteristics> characteristics() {
      return Collections.emptySet();
    }

    @Override public BinaryOperator<MutableBounds> combiner() {

      return (b1, b2) -> new MutableBounds(maximumBounds(b1, b2));
    }

    @Override public Function<MutableBounds, Bounds> finisher() {
      return i -> i.toImmutable();
    }

    @Override public Supplier<MutableBounds> supplier() {
      return () -> new MutableBounds(this.refPos, this.refPos);
    }
  }

  public static class BuildInstruction {

    private final Bounds bounds;
    private final IBlockState state;
    private final String comment;
    private final int lineNumber;

    public BuildInstruction(Bounds bounds, IBlockState state, String comment,
            int lineNumber)
    {
      this.bounds = bounds;
      this.state = state;
      this.comment = comment;
      this.lineNumber = lineNumber;
    }

    public IBlockState getBlockState() {
      return this.state;
    }

    public Bounds getBounds() {
      return this.bounds;
    }

    public String getComment() {
      return this.comment;
    }

    public int getLineNumber() {
      return this.lineNumber;
    }

    @Override public String toString() {
      return String.format("{%s: %s}", this.bounds, this.state);
    }

    public BuildInstruction updateBlock(IBlockState newBlock) {
      return new BuildInstruction(this.getBounds(), newBlock, this.getComment(),
              this.getLineNumber());
    }

    public BuildInstruction updateBounds(Bounds newBounds) {
      return new BuildInstruction(newBounds, this.getBlockState(),
              this.getComment(), this.getLineNumber());
    }

  }

  private final BuildConfigFileLoader loader;

  private final World world;

  public ConfigBaseBuilder(MinecraftServer server, Game game, String name) {
    this.loader = new BuildConfigFileLoader(server, game, name);
    this.world = server.getEntityWorld();
  }

  @Override public Bounds buildBase(BlockPos refPos, TeamColour team,
    IBlockState ambientBlock, boolean invertZ)
  {
    System.out.println(String.format("Build base for %s at %s\n", team,  refPos));
    final List<BuildInstruction> config = this.loader
            .getConfig(team, ambientBlock).stream()
            .map(i -> this.invertZ(i, invertZ)).collect(Collectors.toList());
    System.out.println(String.format("Use build instructions: %s\n",  config));
    return config.stream().collect(new BaseCollector(this.world, refPos));
  }

  @Override public IBlockState getPrimaryMaterial(TeamColour team) {
    return this.loader.getTeamBlock(team);
  }

  private BuildInstruction invertZ(BuildInstruction i, boolean invertZ) {
    if (invertZ) {
      return i.updateBounds(WorldUtils.invertZ(i.getBounds()));
    } else {
      return i;
    }
  }
}
