package me.gking2224.mc.mod.ctf.game;

import static java.lang.String.format;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;

import me.gking2224.mc.mod.ctf.game.CtfTeam.TeamColour;
import me.gking2224.mc.mod.ctf.game.data.GameData;
import me.gking2224.mc.mod.ctf.util.WorldUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Game {

  public static Optional<Game> load(World world, String name) {
    final Optional<GameData> gameData = GameData.get(world, name);
    return Optional
            .ofNullable(gameData.isPresent() ? new Game(gameData.get()) : null);
  }

  private final GameData gameData;

  Game(GameData gameData) {
    this.gameData = gameData;
  }

  Game(World world, String name, EntityPlayer owner, Bounds bounds,
          GameOptions options)
  {

    this.gameData = GameData.create(world, name, options);

    this.gameData.setOwner(owner.getName());
    this.gameData.setBounds(bounds);
    this.addTeam(TeamColour.RED);
    this.addTeam(TeamColour.BLUE);
    this.gameData.getScore().put(TeamColour.RED, 0);
    this.gameData.getScore().put(TeamColour.BLUE, 0);
    this.save();
  }

  public CtfTeam addPlayer(String playerName) {
    final CtfTeam team = this.nextTeam();
    team.addPlayer(playerName);
    this.save();
    return team;
  }

  private void addTeam(TeamColour colour) {
    ;
    this.gameData.getTeams().put(colour, new CtfTeam(colour));
  }

  @SuppressWarnings("unused") private TeamColour chooseRandomTeam() {
    return (new java.util.Random().nextBoolean()) ? TeamColour.RED
            : TeamColour.BLUE;
  }

  public boolean containsPlayer(String player) {

    return this.gameData.getTeams().get(TeamColour.RED).containsPlayer(player)
            || this.gameData.getTeams().get(TeamColour.BLUE)
                    .containsPlayer(player);
  }

  public Set<String> getAllPlayers() {
    final Set<String> rv = new HashSet<String>(this.getTotalNumPlayers());
    this.gameData.getTeams().values().forEach(t -> rv.addAll(t.getPlayers()));
    return rv;
  }

  public Optional<TileEntityChest> getBaseChest(World world,
    TeamColour colour)
  {
    TileEntityChest chest = null;
    final BlockPos chestLoc = this.gameData.getChestLocations().get(colour);
    if (chestLoc != null) {
      chest = (TileEntityChest) world.getTileEntity(chestLoc);
    }
    return Optional.ofNullable(chest);
  }

  public BlockPos getBaseLocation(TeamColour team) {
    return this.gameData.getBaseLocations().get(team);
  }

  public Bounds getBounds() {
    return this.gameData.getBounds();
  }

  public BlockPos getFlagPosition(TeamColour colour) {
    return this.gameData.getFlagLocations().get(colour);
  }

  public String getFormattedScore() {
    final int redScore = this.getScore().get(TeamColour.RED);
    final int blueScore = this.getScore().get(TeamColour.BLUE);
    return format("%s (%d) :: (%d) %s", TeamColour.RED, redScore, blueScore,
            TeamColour.BLUE);
  }

  public String getName() {
    return this.gameData.getName();
  }

  public Optional<BlockPos> getOppFlagLocation(TeamColour team) {
    return Optional.ofNullable(this.gameData.getOppFlagLocations().get(team));
  }

  public GameOptions getOptions() {
    return this.gameData.getOptions();
  }

  public int getPlayerHandicap(String p) {
    final Optional<Integer> h = this.gameData.getPlayerHandicap(p);
    return h.orElse(0);
  }

  public String getPlayerHoldingFlag(TeamColour colour) {
    return this.gameData.getPlayerHoldingFlag().get(colour);
  }

  public Map<TeamColour, Integer> getScore() {
    return new HashMap<TeamColour, Integer>(this.gameData.getScore());
  }

  public Set<TeamColour> getTeamColours() {
    return new HashSet<TeamColour>(this.gameData.getTeams().keySet());
  }

  public Optional<CtfTeam> getTeamForPlayer(String player) {
    return this.gameData.getTeams().values().stream()
            .filter(t -> t.getPlayers().contains(player)).findAny();
  }

  protected int getTeamNumPlayers(String team) {
    return this.gameData.getTeams().get(team).getPlayers().size();
  }

  public Set<String> getTeamPlayers(TeamColour colour) {
    return this.gameData.getTeams().get(colour).getPlayers();
  }

  public Set<CtfTeam> getTeams() {
    return new HashSet<CtfTeam>(this.gameData.getTeams().values());
  }

  public int getTotalNumPlayers() {
    final Collector<CtfTeam, Integer, Integer> c = Collector.of(() -> 0,
            (Integer tot, CtfTeam team) -> team.numPlayers(),
            (t1, t2) -> t1 + t2);
    return this.gameData.getTeams().values().stream().collect(c);
  }

  public void incrementScore(TeamColour team) {
    final Map<TeamColour, Integer> score = this.gameData.getScore();
    score.put(team, score.get(team).intValue() + 1);
    this.save();
  }

  private CtfTeam nextTeam() {
    final Comparator<? super CtfTeam> comp = (a, b) -> a.numPlayers()
            - b.numPlayers();
    final CtfTeam nextTeam = Collections.min(this.gameData.getTeams().values(),
            comp);
    // need to chose random here?
    return nextTeam;
  }

  public void removePlayer(String playerName) {
    this.gameData.getTeams().values().forEach(t -> t.removePlayer(playerName));
    final GameWorldManager gameWorldManager = GameWorldManager.get();
    this.gameData.getPlayerHoldingFlag().entrySet().stream()
            .filter(e -> e.getValue().equals(playerName))
            .forEach(e -> gameWorldManager.resetFlag(this, e.getKey()));
    final GameManager gameManager = GameManager.get();
    gameManager.broadcastToAllPlayers(this,
            String.format("Player %s leaving game", playerName));
    this.save();
  }

  void save() {
    this.gameData.setDirty(true);
  }

  public void setBaseLocation(TeamColour team, BlockPos refPos) {
    this.gameData.getBaseLocations().put(team, refPos);
    this.save();
  }

  public void setChestLocation(TeamColour team, BlockPos chestLocation) {
    this.gameData.getChestLocations().put(team, chestLocation);
    this.save();
  }

  public void setOppFlagLocation(TeamColour team, BlockPos oppFlagLocation) {
    this.gameData.getOppFlagLocations().put(team, oppFlagLocation);
    this.save();
  }

  public void setPlayerHandicap(String p, int h) {
    this.gameData.setPlayerHandicap(p, h);
    this.save();
  }

  public void setPlayerHoldingFlag(TeamColour colour, String player) {
    this.gameData.getPlayerHoldingFlag().put(colour, player);
    this.gameData.getFlagLocations().remove(colour);
    this.save();
  }

  @Override public String toString() {
    return format(
            "Game[" + "name=%s, players=[RED: %s, BLUE:%s], "
                    + "score=%s, redBase=%s, blueBase=%s, options=%s]",
            this.getName(), this.getTeamPlayers(TeamColour.RED),
            this.getTeamPlayers(TeamColour.BLUE), this.getScore(),
            WorldUtils.toChunkLocation(this.getBaseLocation(TeamColour.RED)),
            WorldUtils.toChunkLocation(this.getBaseLocation(TeamColour.BLUE)),
            this.getOptions());
  }

  public void updateFlagBlockPosition(TeamColour colour, BlockPos blockPos) {
    this.gameData.getFlagLocations().put(colour, blockPos);
    this.gameData.getPlayerHoldingFlag().remove(colour);
    this.save();
  }

  public void addPlayerToTeam(String name, TeamColour colour) {
    this.gameData.getTeams().get(colour).addPlayer(name);
    this.save();
  }

  public String getOwner() {
	return gameData.getOwner();
  }
}
