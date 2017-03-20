package me.gking2224.test.lang;

import static me.gking2224.mc.mod.ctf.util.WorldUtils.maximumBounds;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import org.junit.Test;

import me.gking2224.mc.mod.ctf.game.Bounds;
import me.gking2224.mc.mod.ctf.game.base.ConfigBaseBuilder.BuildInstruction;

public class StreamTest {

  private static class MyCollector
          implements Collector<BuildInstruction, Bounds, Bounds>
  {

    private final Bounds refPos;

    public MyCollector(Bounds refPos) {
      this.refPos = refPos;
    }

    @Override public BiConsumer<Bounds, BuildInstruction> accumulator() {
      return (Bounds b, BuildInstruction bi) -> {};
    }

    @Override public Set<java.util.stream.Collector.Characteristics> characteristics() {
      return null;
    }

    @Override public BinaryOperator<Bounds> combiner() {

      return (Bounds b1, Bounds b2) -> maximumBounds(b1, b2);
    }

    @Override public Function<Bounds, Bounds> finisher() {
      return (Bounds i) -> i;
    }

    @Override public Supplier<Bounds> supplier() {
      return () -> null;
    }

  }

  @Test public void testCollect() {

    // Bounds b = new Bounds(new BlockPos(0, 0, 0), new BlockPos(0, 0, 0));
    // BuildInstruction bi = new BuildInstruction(new Bounds(new BlockPos(-1, 0,
    // -1), new BlockPos(1, 0, 1)), Blocks.WOOL.getDefaultState());
    // List<BuildInstruction> config = Collections.singletonList(bi);

    System.out.println(35 + (11 << 12));
    System.out.println(35 + (14 << 12));
    System.out.println(50 + (1 << 12));
    System.out.println(50 + (2 << 12));
    // Bounds b2 = config.stream().collect(new MyCollector());
  }

}
