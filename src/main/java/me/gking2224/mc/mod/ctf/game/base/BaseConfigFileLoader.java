package me.gking2224.mc.mod.ctf.game.base;

import static java.lang.String.format;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import me.gking2224.mc.mod.ctf.blocks.ModBlocks;
import me.gking2224.mc.mod.ctf.game.Bounds;
import me.gking2224.mc.mod.ctf.game.CtfTeam.TeamColour;
import me.gking2224.mc.mod.ctf.game.Game;
import me.gking2224.mc.mod.ctf.game.base.ConfigBaseBuilder.BuildInstruction;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import org.apache.commons.lang3.StringUtils;

public class BaseConfigFileLoader {

  public static class HomeChestBuildInstruction extends BuildInstruction {

    public HomeChestBuildInstruction(BuildInstruction i) {
      super(i.getBounds(), Blocks.CHEST.getDefaultState(), i.getComment(),
              i.getLineNumber());
    }

    public HomeChestBuildInstruction(Bounds bounds, IBlockState blockState,
			String comment, int lineNumber) {
    	super(bounds, blockState, comment, lineNumber);
	}

	public BuildInstruction updateBounds(Bounds newBounds) {
      return new HomeChestBuildInstruction(newBounds, this.getBlockState(),
              this.getComment(), this.getLineNumber());
    }
  }

  public static class OppFlagHolderBuildInstruction extends BuildInstruction {

    public OppFlagHolderBuildInstruction(BuildInstruction i) {
      super(i.getBounds(), ModBlocks.FLAG_HOLDER.getDefaultState(),
              i.getComment(), i.getLineNumber());
    }

    public OppFlagHolderBuildInstruction(Bounds bounds, IBlockState blockState,
			String comment, int lineNumber) {
    	super(bounds, blockState, comment, lineNumber);
	}

	public BuildInstruction updateBounds(Bounds newBounds) {
      return new OppFlagHolderBuildInstruction(newBounds, this.getBlockState(),
              this.getComment(), this.getLineNumber());
    }
  }

  private static final String BASES_DIR_NAME = "bases";
  private static final String SUFFIX = ".dat";
  private static final String TEAM = "team";
  private static final String CHEST = "chest";
  private static final String OPP_FLAG_HOLDER = "oppflag";

  private static final String AMBIENT = "ambient";
  private static final IBlockState TEAM_PLACEHOLDER = new NullBlockState(TEAM);
  private static final IBlockState AMBIENT_PLACEHOLDER = new NullBlockState(
          AMBIENT);
  private static final IBlockState CHEST_PLACEHOLDER = new NullBlockState(
          CHEST);
  private static final IBlockState OPP_FLAG_PLACEHOLDER = new NullBlockState(
          OPP_FLAG_HOLDER);
  private final MinecraftServer server;
  @SuppressWarnings("unused") private final Game game;

  private final String name;

  private final Map<TeamColour, IBlockState> teamColours = new HashMap<TeamColour, IBlockState>();

  private final List<BuildInstruction> config;

  public BaseConfigFileLoader(MinecraftServer server, Game game, String name) {
    this.server = server;
    this.game = game;
    this.name = name;
    this.config = this.load();
  }

  public List<BuildInstruction> getConfig(TeamColour team,
    IBlockState ambientBlock)
  {
    ArrayList<BuildInstruction> copyConfig = new ArrayList<BuildInstruction>(this.config);
	List<BuildInstruction> converted = copyConfig.stream().map(i -> {
      if (i.getBlockState() == TEAM_PLACEHOLDER) {
        return i.updateBlock(this.getTeamBlock(team));
      } else if (i.getBlockState() == AMBIENT_PLACEHOLDER) {
        return i.updateBlock(ambientBlock);
      } else if (i.getBlockState() == CHEST_PLACEHOLDER) {
        return new HomeChestBuildInstruction(i);
      } else if (i.getBlockState() == OPP_FLAG_PLACEHOLDER) {
        return new OppFlagHolderBuildInstruction(i);
      } else {
        return i;
      }
    }).collect(Collectors.toList());
	return converted;
  }

public IBlockState getTeamBlock(TeamColour team) {
    return this.teamColours.get(team);
  }

  public List<BuildInstruction> load() {
    final File f = this.validateFile();

    final List<BuildInstruction> rv = new ArrayList<BuildInstruction>();
    BufferedReader br = null;
    try {
      br = new BufferedReader(new FileReader(f));
      String line = null;
      while ((line = br.readLine()) != null) {
        final Optional<BuildInstruction> pl = this.processLine(line.trim(),
                rv.size() + 1);
        pl.ifPresent(l -> rv.add(l));
      }
      try {
        br.close();
      } catch (final IOException e) {}
    } catch (final IOException e) {
      throw new BaseBuilderException(e);
    }
    return rv;
  }

  private BuildInstruction parseLine(String line, int lineNumber) {
    BuildInstruction rv;
    final CommentedLine commentedLine = CommentedLine.parse(line);
    final List<String> tokens = Arrays
            .asList(StringUtils.split(commentedLine.getContent()));
    final int[] coords = tokens.subList(0, 6).stream()
            .mapToInt(Integer::parseInt).toArray();
    final BlockPos from = new BlockPos(coords[0], coords[2], coords[4]);
    final BlockPos to = new BlockPos(coords[1], coords[3], coords[5]);
    final IBlockState state = this
            .readBlockState(tokens.subList(6, tokens.size()));
    rv = new BuildInstruction(new Bounds(from, to), state,
            commentedLine.getComment(), lineNumber);
    return rv;
  }

  private Optional<BuildInstruction> processLine(String line, int lineNumber) {
    BuildInstruction rv = null;
    System.out.println(String.format("Reading base config file, line %d: %s\n",
            lineNumber, line));
    if (line.startsWith(TEAM)) {
      this.processTeamColour(line);
    } else if (!line.startsWith("#")) {
      rv = this.parseLine(line, lineNumber);
    }
    return Optional.ofNullable(rv);
  }

  private void processTeamColour(String line) {
    final List<String> tokens = Arrays.asList(StringUtils.split(line));
    final String team = tokens.get(1);
    final IBlockState teamBlock = Block
            .getStateById(Integer.parseInt(tokens.get(2)));
    TeamColour colour = TeamColour.fromString(team).orElseThrow(invalidTeamException(team));
	this.teamColours.put(colour, teamBlock);
  }

  private Supplier<IllegalArgumentException> invalidTeamException(String team) {
    return () -> new IllegalArgumentException(String.format("Invalid team: %s", team));
  }

  private IBlockState readBlockState(List<String> subList) {
    final String name = subList.get(0);
    if (name.matches("[0-9]*")) {

      final int stateId = Integer.parseInt(name);
      return Block.getStateById(stateId);
    } else if (AMBIENT.equals(name)) {
      return AMBIENT_PLACEHOLDER;
    } else if (TEAM.equals(name)) {
      return TEAM_PLACEHOLDER;
    } else if (CHEST.equals(name)) {
      return CHEST_PLACEHOLDER;
    } else if (OPP_FLAG_HOLDER.equals(name)) {
      return OPP_FLAG_PLACEHOLDER;
    } else {
      return Blocks.WOOL.getDefaultState();
    }
  }

  private File validateFile() {
    final File basesDir = new File(this.server.getDataDirectory(),
            BASES_DIR_NAME);
    if (!basesDir.exists()) { throw new BaseBuilderException(
            new NoSuchFileException(basesDir.getAbsolutePath())); }
    if (!basesDir.isDirectory()) { throw new BaseBuilderException(
            format("%s is not a directory", basesDir.getAbsolutePath())); }
    final File baseConfigFile = new File(basesDir, this.name + SUFFIX);
    if (!baseConfigFile.exists()) { throw new BaseBuilderException(
            new NoSuchFileException(baseConfigFile.getAbsolutePath())); }
    if (!baseConfigFile.isFile()) { throw new BaseBuilderException(
            format("%s is not a file", baseConfigFile.getAbsolutePath())); }
    return baseConfigFile;
  }

}
