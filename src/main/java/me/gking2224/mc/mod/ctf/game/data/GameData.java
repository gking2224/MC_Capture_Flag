package me.gking2224.mc.mod.ctf.game.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import me.gking2224.mc.mod.ctf.game.Bounds;
import me.gking2224.mc.mod.ctf.game.CtfTeam;
import me.gking2224.mc.mod.ctf.game.CtfTeam.TeamColour;
import me.gking2224.mc.mod.ctf.game.GameOptions;
import me.gking2224.mc.mod.ctf.util.NBTUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;

public class GameData extends WorldSavedData {

  private static final String HANDICAP = "handicap";
  private static final String FLAG_LOC = "flagLoc";
  private static final String BASE_LOC = "baseLoc";
  private static final String FLAG_HOLDER = "flagHolder";
  private static final String SCORE = "score";
  private static final String TEAM = "team";
  private static final String NUM_TEAMS = "num_teams";
  private static final String BOUNDS = "bounds";
  private static final String NAME = "name";
  private static final String OWNER = "owner";
  private static final String FLAG_HELD = "flagHeld";
  private static final String DATA_NAME = "CTF_GAME_DATA_";
  private static final String OPTIONS = "options";

  public static GameData create(World world, String name, GameOptions options) {

    final MapStorage storage = world.getPerWorldStorage();
    final GameData instance = new GameData(name, options);
    storage.setData(getDataIdentifier(name), instance);
    System.out.println(String.format("Stored GameData: %s\n",  instance));
    return instance;
  }

  public static Optional<GameData> get(World world, String name) {
    final MapStorage storage = world.getPerWorldStorage();
    final GameData instance = (GameData) storage.getOrLoadData(GameData.class,
            getDataIdentifier(name));
    System.out.println(String.format("Loaded GameData: %s\n",  instance));
    return Optional.ofNullable(instance);
  }

  private static String getDataIdentifier(String name) {
    return DATA_NAME + name;
  }

  private String owner;
  private String name;
  private Bounds bounds;
  private Map<TeamColour, CtfTeam> teams = new HashMap<TeamColour, CtfTeam>();
  private Map<TeamColour, Integer> score = new HashMap<TeamColour, Integer>();
  private Map<TeamColour, BlockPos> baseLocations = new HashMap<TeamColour, BlockPos>();
  private Map<TeamColour, BlockPos> flagLocations = new HashMap<TeamColour, BlockPos>();

  private Map<TeamColour, String> playerHoldingFlag = new HashMap<TeamColour, String>();

  private GameOptions options;

  private final Map<String, Integer> handicaps = new HashMap<String, Integer>();

  public GameData() {
    this("unknown", GameOptions.getDefault());
  }

  public GameData(String name) {
    this(getDataIdentifier(name), null);
  }

  public GameData(String name, GameOptions options) {
    super(getDataIdentifier(name));
    this.name = name;
    this.options = options;
  }

  public Map<TeamColour, BlockPos> getBaseLocations() {
    return this.baseLocations;
  }

  public Bounds getBounds() {
    return this.bounds;
  }

  public Map<TeamColour, BlockPos> getFlagLocations() {
    return this.flagLocations;
  }

  public String getName() {
    return this.name;
  }

  public GameOptions getOptions() {
    return this.options;
  }

  public String getOwner() {
    return this.owner;
  }

  public Optional<Integer> getPlayerHandicap(String p) {
    return Optional.ofNullable(this.handicaps.get(p));
  }

  public Map<TeamColour, String> getPlayerHoldingFlag() {
    return this.playerHoldingFlag;
  }

  public Map<TeamColour, Integer> getScore() {
    return this.score;
  }

  public Map<TeamColour, CtfTeam> getTeams() {
    return this.teams;
  }

  @Override public void readFromNBT(NBTTagCompound nbt) {
    this.owner = nbt.getString(OWNER);
    this.name = nbt.getString(NAME);
    this.bounds = Bounds.readFromNBT(nbt, BOUNDS);
    final int numTeams = nbt.getInteger(NUM_TEAMS);
    for (int i = 0; i < numTeams; i++) {
      final CtfTeam team = CtfTeam.readFromNBT(nbt, TEAM + i);
      this.teams.put(team.getColour(), team);
      this.score.put(team.getColour(), nbt.getInteger(SCORE + i));
      this.baseLocations.put(team.getColour(),
              NBTUtils.getBlockPos(nbt, BASE_LOC + i));
      this.flagLocations.put(team.getColour(),
              NBTUtils.getBlockPos(nbt, FLAG_LOC + i));
      final boolean flagHeld = nbt.getBoolean(FLAG_HELD + i);
      if (flagHeld) {
        this.playerHoldingFlag.put(team.getColour(),
                nbt.getString(FLAG_HOLDER + i));
      }
    }

    final String optionsStr = nbt.getString(OPTIONS);
    System.out.println(String.format("read game options: %s\n",  optionsStr));
    this.options = (optionsStr != null) ? new GameOptions(optionsStr)
            : GameOptions.getDefault();

    this.teams.values().forEach(t -> t.getPlayers()
            .forEach(p -> this.handicaps.put(p, nbt.getInteger(HANDICAP + p))));

    System.out.println(String.format("Read game from NBT: %s\n",  this));
  }

  public void setBaseLocations(Map<TeamColour, BlockPos> baseLocations) {
    this.baseLocations = baseLocations;
  }

  public void setBounds(Bounds bounds) {
    this.bounds = bounds;
  }

  public void setFlagLocations(Map<TeamColour, BlockPos> flagLocations) {
    this.flagLocations = flagLocations;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public void setPlayerHandicap(String p, int h) {
    this.handicaps.put(p, h);
  }

  public void setPlayerHoldingFlag(Map<TeamColour, String> playerHoldingFlag) {
    this.playerHoldingFlag = playerHoldingFlag;
  }

  public void setScore(Map<TeamColour, Integer> score) {
    this.score = score;
  }

  public void setTeams(Map<TeamColour, CtfTeam> teams) {
    this.teams = teams;
  }

  @Override public String toString() {
    return String.format(
            "GameData:[owner=%s; name=%s; bounds=%s; teams=%s; score=%s; baseLocations=%s; flagLocations=%s; playerHoldingFlag=%s]",
            this.owner, this.name, this.bounds, this.teams, this.score,
            this.baseLocations, this.flagLocations, this.playerHoldingFlag);
  }

  @Override public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
    nbt.setString(OWNER, this.owner);
    nbt.setString(NAME, this.name);
    Bounds.writeToNBT(nbt, BOUNDS, this.bounds);
    nbt.setInteger(NUM_TEAMS, this.teams.size());
    int i = 0;
    for (final CtfTeam t : this.teams.values()) {

      CtfTeam.writeToNBT(nbt, TEAM + i, t);
      final TeamColour colour = t.getColour();
      nbt.setInteger(SCORE + i, this.score.get(colour));
      NBTUtils.setBlockPos(nbt, BASE_LOC + i, this.baseLocations.get(colour));
      NBTUtils.setBlockPos(nbt, FLAG_LOC + i, this.flagLocations.get(colour));
      final String flagHolder = this.playerHoldingFlag.get(colour);
      nbt.setBoolean(FLAG_HELD + i, flagHolder != null);
      if (flagHolder != null) {
        nbt.setString(FLAG_HOLDER + i, flagHolder);
      }
      i++;
    }
    final String optionsStr = this.options.toString();
    nbt.setString(OPTIONS, optionsStr);

    this.handicaps.forEach((p, h) -> nbt.setInteger(HANDICAP + p, h));

    System.out.println(String.format("Wrote game to NBT: %s\n",  this));
    return nbt;
  }
}
