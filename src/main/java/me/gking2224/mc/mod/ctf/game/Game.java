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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Game {

	private GameData gameData;

	Game(World world, String name, EntityPlayer owner, Bounds bounds, GameOptions options) {
		
		gameData = GameData.create(world, name, options);
		
		gameData.setOwner(owner.getName());
		gameData.setBounds(bounds);
		addTeam(TeamColour.RED);
		addTeam(TeamColour.BLUE);
		gameData.getScore().put(TeamColour.RED, 0);
		gameData.getScore().put(TeamColour.BLUE, 0);
		save();
	}
	
	Game(GameData gameData) {
		this.gameData = gameData;
	}
	
	private void addTeam(TeamColour colour) {;
		gameData.getTeams().put(colour, new CtfTeam(colour));
	}

	public CtfTeam addPlayer(String playerName) {
		CtfTeam team = nextTeam();
		team.addPlayer(playerName);
		save();
		return team;
	}
	
	private CtfTeam nextTeam() {
		Comparator<? super CtfTeam> comp = (a, b) -> a.numPlayers() - b.numPlayers();
		CtfTeam nextTeam = Collections.min(gameData.getTeams().values(), comp);
		// need to chose random here?
		return nextTeam;
	}

	void save() {
		gameData.setDirty(true);
	}

	private TeamColour chooseRandomTeam() {
		return (new java.util.Random().nextBoolean()) ? TeamColour.RED : TeamColour.BLUE;
	}

	protected int getTeamNumPlayers(String team) {
		return gameData.getTeams().get(team).getPlayers().size();
	}

	public boolean containsPlayer(String player) {
		
		return gameData.getTeams().get(TeamColour.RED).containsPlayer(player) ||
				gameData.getTeams().get(TeamColour.BLUE).containsPlayer(player);
	}

	public Optional<CtfTeam> getTeamForPlayer(String player) {
		return gameData.getTeams().values().stream().filter(t -> t.getPlayers().contains(player)).findAny();
	}

	public void setBaseLocation(TeamColour team, BlockPos refPos) {
		gameData.getBaseLocations().put(team, refPos);
		save();
	}
	
	public BlockPos getBaseLocation(TeamColour team) {
		return gameData.getBaseLocations().get(team);
	}
	
	public int getTotalNumPlayers() {
		Collector<CtfTeam, Integer, Integer> c = Collector.of(() -> 0, (Integer tot, CtfTeam team) -> team.numPlayers(), (t1, t2) -> t1 + t2);
		return gameData.getTeams().values().stream().collect(c);
	}

	public Set<String> getAllPlayers() {
		Set<String> rv = new HashSet<String>(getTotalNumPlayers());
		gameData.getTeams().values().forEach( t -> rv.addAll(t.getPlayers()));
		return rv;
	}

	public Set<String> getTeamPlayers(TeamColour colour) {
		return gameData.getTeams().get(colour).getPlayers();
	}

	public void incrementScore(TeamColour team) {
		Map<TeamColour, Integer> score = gameData.getScore();
		score.put(team, score.get(team).intValue() + 1);
		save();
	}

	public void setFlagBlockPosition(TeamColour colour, BlockPos blockPos) {
		gameData.getFlagLocations().put(colour, blockPos);
		gameData.getPlayerHoldingFlag().remove(colour);
		save();
	}

	public BlockPos getFlagPosition(TeamColour colour) {
		return gameData.getFlagLocations().get(colour);
	}

	public void setPlayerHoldingFlag(TeamColour colour, String player) {
		gameData.getPlayerHoldingFlag().put(colour, player);
		gameData.getFlagLocations().remove(colour);
		save();
	}

	public String getPlayerHoldingFlag(TeamColour colour) {
		return gameData.getPlayerHoldingFlag().get(colour);
	}

	public void removePlayer(String playerName) {
		gameData.getTeams().values().forEach(t -> t.removePlayer(playerName));
		GameWorldManager gameWorldManager = GameWorldManager.get();
		gameData.getPlayerHoldingFlag().entrySet().stream().filter(e -> e.getValue().equals(playerName)).forEach( e->
				gameWorldManager.resetFlag(this, e.getKey()));
		GameManager gameManager = GameManager.get();
		gameManager.broadcastToAllPlayers(this, String.format("Player %s leaving game", playerName));
		save();
	}
	
	public Set<CtfTeam> getTeams() {
		return new HashSet<CtfTeam>(gameData.getTeams().values());
	}
	
	public Set<TeamColour> getTeamColours() {
		return new HashSet<TeamColour>(gameData.getTeams().keySet());
	}

	public Map<TeamColour, Integer> getScore() {
		return new HashMap<TeamColour, Integer>(gameData.getScore());
	}

	public Bounds getBounds() {
		return gameData.getBounds();
	}

	public String getName() {
		return gameData.getName();
	}

	public static Optional<Game> load(World world, String name) {
		Optional<GameData> gameData = GameData.get(world, name);
		return Optional.ofNullable(
				gameData.isPresent() ? new Game(gameData.get()) : null);
	}
	
	public GameOptions getOptions() {
		return gameData.getOptions();
	}
	
	public void setPlayerHandicap(String p, int h) {
		gameData.setPlayerHandicap(p, h);
		save();
	}
	
	public int getPlayerHandicap(String p) {
		Optional<Integer>h  = gameData.getPlayerHandicap(p);
		return h.orElse(0);
	}
	
	public String toString() {
		return format(
				"Game[name=%s, players=[RED: %s, BLUE:%s], score=%s, redBase=%s, blueBase=%s, options=%s]",
				getName(),
				getTeamPlayers(TeamColour.RED),
				getTeamPlayers(TeamColour.BLUE),
				getScore(),
				WorldUtils.toChunkLocation(getBaseLocation(TeamColour.RED)),
				WorldUtils.toChunkLocation(getBaseLocation(TeamColour.BLUE)),
				getOptions());
	}

	public String getFormattedScore() {
		int redScore = getScore().get(TeamColour.RED);
		int blueScore = getScore().get(TeamColour.BLUE);
		return format("RED (%d) :: (%d) BLUE", redScore, blueScore);
	}

}
