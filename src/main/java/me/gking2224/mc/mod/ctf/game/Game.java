package me.gking2224.mc.mod.ctf.game;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;

import me.gking2224.mc.mod.ctf.game.CtfTeam.TeamColour;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

public class Game {

	private String owner;
	private String name;
	private Bounds bounds;
	private Map<TeamColour, CtfTeam> teams = new HashMap<TeamColour, CtfTeam>();
	private Map<TeamColour, Integer> score = new HashMap<TeamColour, Integer>();
	private Map<TeamColour, BlockPos> baseLocations = new HashMap<TeamColour, BlockPos>();
	private Map<TeamColour, BlockPos> flagLocations = new HashMap<TeamColour, BlockPos>();
	private Map<TeamColour, String> playerHoldingFlag = new HashMap<TeamColour, String>();

	Game(String name, EntityPlayer owner, Bounds bounds) {
		setName(name);
		setOwner(owner.getName());
		setBounds(bounds);
		addTeam(TeamColour.RED);
		addTeam(TeamColour.BLUE);
		score.put(TeamColour.RED, 0);
		score.put(TeamColour.BLUE, 0);
	}
	
	private void addTeam(TeamColour colour) {;
		teams.put(colour, new CtfTeam(colour));
	}

	public CtfTeam addPlayer(String playerName) {
		CtfTeam team = nextTeam();
		team.addPlayer(playerName);
		save();
		return team;
	}
	
	private CtfTeam nextTeam() {
		Comparator<? super CtfTeam> comp = (a, b) -> a.numPlayers() - b.numPlayers();
		CtfTeam nextTeam = Collections.min(teams.values(), comp);
		// need to chose random here?
		return nextTeam;
	}

	void save() {
		GameFileManager.get().writeGameToFile(this);
	}

	private TeamColour chooseRandomTeam() {
		return (new java.util.Random().nextBoolean()) ? TeamColour.RED : TeamColour.BLUE;
	}

	protected int teamNumPlayers(String team) {
		return teams.get(team).getPlayers().size();
	}

	public boolean containsPlayer(String player) {
		
		return teams.get(TeamColour.RED).containsPlayer(player) ||
				teams.get(TeamColour.BLUE).containsPlayer(player);
	}

	public Optional<CtfTeam> getTeamForPlayer(String player) {
		return teams.values().stream().filter(t -> t.getPlayers().contains(player)).findAny();
	}

	public void setBaseLocation(TeamColour team, BlockPos refPos) {
		baseLocations.put(team, refPos);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getOwner() {
		return owner;
	}

	public Bounds getBounds() {
		return bounds;
	}

	public void setBounds(Bounds bounds) {
		this.bounds = bounds;
	}
	
	public BlockPos getBaseLocation(TeamColour team) {
		return this.baseLocations.get(team);
	}
	
	public int totalNumPlayers() {
		Collector<CtfTeam, Integer, Integer> c = Collector.of(() -> 0, (Integer tot, CtfTeam team) -> team.numPlayers(), (t1, t2) -> t1 + t2);
		return teams.values().stream().collect(c);
	}

	public Set<String> getAllPlayers() {
		Set<String> rv = new HashSet<String>(totalNumPlayers());
		teams.values().forEach( t -> rv.addAll(t.getPlayers()));
		return rv;
	}

	public Set<String> getTeamPlayers(String team) {
		return teams.get(team).getPlayers();
	}

	public void incrementScore(TeamColour team) {
		score.put(team, score.get(team).intValue() + 1);
		save();
	}

	public void setFlagBlockPosition(TeamColour colour, BlockPos blockPos) {
		flagLocations.put(colour, blockPos);
		playerHoldingFlag.remove(colour);
		save();
	}

	public BlockPos getFlagPosition(TeamColour colour) {
		return flagLocations.get(colour);
	}

	public void setPlayerHoldingFlag(TeamColour colour, String player) {
		playerHoldingFlag.put(colour, player);
		flagLocations.remove(colour);
		save();
	}

	public String getPlayerHoldingFlag(String flagColour) {
		return playerHoldingFlag.get(flagColour);
	}

	public void removePlayer(String playerName) {
		teams.values().forEach(t -> t.removePlayer(playerName));
		playerHoldingFlag.entrySet().stream().filter(e -> e.getValue().equals(playerName)).forEach( e->
				GameWorldManager.get().resetFlag(this, e.getKey()));
		GameManager.get().broadcastToAllPlayers(this, String.format("Player %s leaving game", playerName));
		save();
	}
	
	public Set<CtfTeam> getTeams() {
		return new HashSet<CtfTeam>(this.teams.values());
	}
	
	public Set<TeamColour> getTeamColours() {
		return new HashSet<TeamColour>(this.teams.keySet());
	}

	public Map<TeamColour, Integer> getScore() {
		return new HashMap<TeamColour, Integer>(this.score);
	}

}
