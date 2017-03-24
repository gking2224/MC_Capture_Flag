package me.gking2224.mc.mod.ctf.game.base;

import static java.lang.String.format;
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
import me.gking2224.mc.mod.ctf.game.base.BaseConfigFileLoader.HomeChestBuildInstruction;
import me.gking2224.mc.mod.ctf.game.base.BaseConfigFileLoader.OppFlagHolderBuildInstruction;
import me.gking2224.mc.mod.ctf.util.WorldUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ConfigBaseBuilder implements BaseBuilder {

  private static class BaseCollector implements
          Collector<BuildInstruction, MutableBounds, BaseDescription>
  {

    private final BlockPos refPos;
    private final World world;
    private BlockPos chestPos;
    private BlockPos oppFlagPos;

    public BaseCollector(World world, BlockPos refPos) {
      this.refPos = refPos;
      this.world = world;
      this.chestPos = null;
      this.oppFlagPos = null;
    }

    @Override public BiConsumer<MutableBounds, BuildInstruction> accumulator() {
      return (MutableBounds cb, BuildInstruction bi) -> this
              .buildFromInstruction(bi);
    }

    private void buildFromInstruction(BuildInstruction bi) {
      final IBlockState blockState = bi.getBlockState();
      final Bounds bounds = bi.getBounds();
      placeBlocks(this.world, offset(this.refPos, bounds), blockState);
      this.captureInstruction(this.world, bi);
    }

    private void captureInstruction(World world, BuildInstruction bi) {

      if (bi instanceof HomeChestBuildInstruction) {

        this.chestPos = bi.getBounds().getFrom();
      } else if (bi instanceof OppFlagHolderBuildInstruction) {

        this.oppFlagPos = bi.getBounds().getFrom();
      }
    }

    @Override public Set<java.util.stream.Collector.Characteristics> characteristics() {
      return Collections.emptySet();
    }

    @Override public BinaryOperator<MutableBounds> combiner() {

      return (b1, b2) -> new MutableBounds(maximumBounds(b1, b2));
    }

    @Override public Function<MutableBounds, BaseDescription> finisher() {
      return i -> {
        final BlockPos chestPos = this.chestPos == null ? null
                : offset(this.refPos, this.chestPos);
        final BlockPos oppFlagPos = this.oppFlagPos == null ? null
                : offset(this.refPos, this.oppFlagPos);
        return new BaseDescription(i.toImmutable(), chestPos, oppFlagPos);
      };
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
      return String.format("%s: {%s: %s}", this.getClass().getName(), this.bounds, this.state);
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

  private final BaseConfigFileLoader loader;

  private final World world;

  public ConfigBaseBuilder(MinecraftServer server, Game game, String name) {
    this.loader = new BaseConfigFileLoader(server, game, name);
    this.world = server.getEntityWorld();
  }

  @Override public BaseDescription buildBase(BlockPos refPos, TeamColour team,
    IBlockState ambientBlock, boolean invertZ)
  {
    System.out.println(format("Build base for %s at %s\n", team, refPos));
    List<BuildInstruction> rawInstructions = this.loader
            .getConfig(team, ambientBlock);
	final List<BuildInstruction> inverted = rawInstructions.stream()
            .map(i -> (invertZ) ? this.invertZ(i) : i).collect(Collectors.toList());
    System.out.println(String.format("Use build instructions: %s\n", inverted));
    return inverted.stream().collect(new BaseCollector(this.world, refPos));
  }

  @Override public IBlockState getPrimaryMaterial(TeamColour team) {
    return this.loader.getTeamBlock(team);
  }

  private BuildInstruction invertZ(BuildInstruction i) {
	  return i.updateBounds(WorldUtils.invertZ(i.getBounds()));
  }
}
