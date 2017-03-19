package me.gking2224.mc.mod.ctf.game.base;

import static java.lang.String.format;
import static me.gking2224.mc.mod.ctf.util.WorldUtils.maximumBounds;
import static me.gking2224.mc.mod.ctf.util.WorldUtils.offsetBounds;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import me.gking2224.mc.mod.ctf.game.Bounds;
import me.gking2224.mc.mod.ctf.game.CtfTeam.TeamColour;
import me.gking2224.mc.mod.ctf.game.Game;
import me.gking2224.mc.mod.ctf.util.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.apache.commons.lang3.StringUtils;

public class ConfigBaseBuilder implements BaseBuilder {

	public static class BuildConfigFileLoader {

		private static final String BASES_DIR_NAME = "bases";
		private static final String SUFFIX = ".dat";

		public static List<BuildInstruction> load(MinecraftServer server,
				Game game, String name) {
			List<BuildInstruction> rv = new ArrayList<BuildInstruction>();
			File f = validateFile(server, name);
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(f));
				String line = null;
				while ((line = br.readLine()) != null) {
					
					Optional<BuildInstruction> pl = processLine(line);
					pl.ifPresent(l->rv.add(l));
				}
				try { br.close(); } catch (IOException e) {}
			} catch (IOException e) {
				throw new BaseBuilderException(e);
			}
			return rv;
		}

		private static Optional<BuildInstruction> processLine(String line) {
			BuildInstruction rv = null;
			if (line.startsWith("#")) {
				CommentedLine commentedLine = CommentedLine.parse(line);
				List<String> tokens = Arrays.asList(StringUtils.split(commentedLine.getContent()));
				BlockPos from = readBlockPos(tokens.subList(0, 3).stream()
						.mapToInt(Integer::parseInt).toArray());
				BlockPos to = readBlockPos(tokens.subList(3, 6).stream()
						.mapToInt(Integer::parseInt).toArray());
				IBlockState state = readBlockState(tokens.subList(6, tokens.size()));
				rv = new BuildInstruction(new Bounds(from, to), state);
			}
			return Optional.ofNullable(rv);
		}

		private static IBlockState readBlockState(List<String> subList) {
			String name = subList.get(0);
			return Block.getStateById(Integer.parseInt(name));
		}

		private static BlockPos readBlockPos(int[] array) {
			return new BlockPos(array[0], array[1], array[2]);
		}

		private static File validateFile(MinecraftServer server, String name) {
			File basesDir = new File(server.getDataDirectory(), BASES_DIR_NAME);
			if (!basesDir.exists()) {
				throw new BaseBuilderException(new NoSuchFileException(
						basesDir.getAbsolutePath()));
			}
			if (!basesDir.isDirectory()) {
				throw new BaseBuilderException(format("%s is not a directory",
						basesDir.getAbsolutePath()));
			}
			File baseConfigFile = new File(basesDir, name + SUFFIX);
			if (!baseConfigFile.exists()) {
				throw new BaseBuilderException(new NoSuchFileException(
						basesDir.getAbsolutePath()));
			}
			if (!baseConfigFile.isFile()) {
				throw new BaseBuilderException(format("%s is not a file",
						baseConfigFile.getAbsolutePath()));
			}
			return baseConfigFile;
		}
		
		private static class CommentedLine {

			private String content;
			private String comment;

			public CommentedLine(String content, String comment) {
				this.content = content;
				this.comment = comment;
			}

			public static CommentedLine parse(String line) {
				int idx = line.indexOf("#");
				String comment = null;
				String content = line;
				if (idx != 1) {
					comment = line.substring(idx);
					content = line.substring(0, idx-1);
				}
				return new CommentedLine(content, comment);
			}

			public String getContent() {
				return this.content;
			}

			@SuppressWarnings("unused") public String getComment() {
				return this.comment;
			}
		}

	}

	private List<BuildInstruction> config;

	public ConfigBaseBuilder(MinecraftServer server, Game game, String name) {
		config = BuildConfigFileLoader.load(server, game, name);
	}

	public static class BuildInstruction {

		private Bounds bounds;
		private IBlockState state;

		public BuildInstruction(Bounds bounds, IBlockState state) {
			this.bounds = bounds;
			this.state = state;
		}

		public Bounds getBounds() {
			return this.bounds;
		}

		public IBlockState getBlockState() {
			return this.state;
		}

	}

	@Override
	public IBlockState getPrimaryMaterial(TeamColour team) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bounds buildBase(World world, BlockPos refPos, TeamColour colour) {
		
		return config.stream().collect(new BoundsCollector(world, refPos));
	}

	private static class BoundsCollector implements Collector<BuildInstruction, Bounds, Bounds> {
		
		private BlockPos refPos;
		private World world;

		public BoundsCollector(World world, BlockPos refPos) {
			this.refPos = refPos;
			this.world = world;
		}
		
		@Override
		public Supplier<Bounds> supplier() {
			return () -> new Bounds(this.refPos, this.refPos);
		}

		@Override
		public BiConsumer<Bounds, BuildInstruction> accumulator() {
			return (Bounds cb, BuildInstruction bi) -> {
				WorldUtils.placeBlocks(this.world, offsetBounds(this.refPos, bi.getBounds()), bi.getBlockState());
			};
		}

		@Override
		public BinaryOperator<Bounds> combiner() {
			
			return (b1, b2) -> maximumBounds(b1, b2);
		}

		@Override
		public Function<Bounds, Bounds> finisher() {
			return i -> i;
		}

		@Override
		public Set<java.util.stream.Collector.Characteristics> characteristics() {
			return Collections.emptySet();
		}
	}
}
