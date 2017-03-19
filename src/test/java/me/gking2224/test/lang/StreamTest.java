package me.gking2224.test.lang;

import static me.gking2224.mc.mod.ctf.util.WorldUtils.maximumBounds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import me.gking2224.mc.mod.ctf.game.Bounds;
import me.gking2224.mc.mod.ctf.game.base.ConfigBaseBuilder.BuildInstruction;
import me.gking2224.mc.mod.ctf.util.WorldUtils;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

import org.junit.Test;


public class StreamTest {

	@Test
	public void testCollect() {
		
//		Bounds b = new Bounds(new BlockPos(0, 0, 0), new BlockPos(0, 0, 0));
//		BuildInstruction bi = new BuildInstruction(new Bounds(new BlockPos(-1, 0, -1), new BlockPos(1, 0, 1)), Blocks.WOOL.getDefaultState());
//		List<BuildInstruction> config = Collections.singletonList(bi);
		
		int c = 11 << 12;
		int i = 35 + c;
		System.out.println(i);
//		Bounds b2 = config.stream().collect(new MyCollector());
	}
	
	private static class MyCollector implements Collector<BuildInstruction, Bounds, Bounds> {
		
		private Bounds refPos;

		public MyCollector(Bounds refPos) {
			this.refPos = refPos;
		}
		
		@Override
		public Supplier<Bounds> supplier() {
			return () -> null;
		}

		@Override
		public BiConsumer<Bounds, BuildInstruction> accumulator() {
			return (Bounds b, BuildInstruction bi) -> {};
		}

		@Override
		public BinaryOperator<Bounds> combiner() {
			
			return (Bounds b1, Bounds b2) -> maximumBounds(b1, b2);
		}

		@Override
		public Function<Bounds, Bounds> finisher() {
			return (Bounds i) -> i; 
		}

		@Override
		public Set<java.util.stream.Collector.Characteristics> characteristics() {
			return null;
		}
		
	}

}
