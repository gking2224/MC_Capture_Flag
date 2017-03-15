package me.gking2224.mc.mod.ctf.game;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Game {

	private String owner;
	private String name;
	private Bounds bounds;
	private Map<String, CtfTeam> teams = new HashMap<String, CtfTeam>();
	private Map<String, Integer> score = new HashMap<String, Integer>();
	private Map<String, BlockPos> baseLocations = new HashMap<String, BlockPos>();
	private Map<String, BlockPos> flagLocations = new HashMap<String, BlockPos>();
	private Map<String, String> playerHoldingFlag = new HashMap<String, String>();

	Game(String name, EntityPlayer owner, Bounds bounds) {
		setName(name);
		setOwner(owner.getName());
		setBounds(bounds);
		teams.put(CtfTeam.RED, new CtfTeam(CtfTeam.RED));
		teams.put(CtfTeam.BLUE, new CtfTeam(CtfTeam.BLUE));
		score.put(CtfTeam.RED, 0);
		score.put(CtfTeam.BLUE, 0);
	}
	
	public String addPlayer(String playerName) {
		String nextTeam = nextTeam();
		teams.get(nextTeam).addPlayer(playerName);
		save();
		return nextTeam;
	}
	
	private String nextTeam() {
		String nextTeam = null;
		if (teamNumPlayers(CtfTeam.RED) > teamNumPlayers(CtfTeam.BLUE)) {
			nextTeam = CtfTeam.BLUE;
		}
		else if (teamNumPlayers(CtfTeam.BLUE) > teamNumPlayers(CtfTeam.RED)) {
			nextTeam = CtfTeam.RED;
		}
		else nextTeam = chooseRandomTeam();
		return nextTeam;
	}

	public void sendPlayerToBase(World world, String playerName) {
		String team = getTeamForPlayer(playerName);
		EntityPlayer player = world.getPlayerEntityByName(playerName);
		BlockPos baseLocation = getBaseLocation(team);
		System.out.printf("Sending player %s to %s team base %s\n", playerName, team, baseLocation);
		int x = baseLocation.getX() + 2, z = baseLocation.getZ() + 2;
		int y = GameWorldManager.get().getWorldHeight(x, z) + 1;
		player.setPosition(x, y, z);
		
	}

	void save() {
		GameFileManager.get().writeGameToFile(this);
	}

	private String chooseRandomTeam() {
		return (new java.util.Random().nextBoolean()) ? CtfTeam.RED : CtfTeam.BLUE;
	}

	protected int teamNumPlayers(String team) {
		return teams.get(team).getPlayers().size();
	}

	public boolean containsPlayer(String player) {
		return teams.get(CtfTeam.RED).containsPlayer(player) ||
				teams.get(CtfTeam.BLUE).containsPlayer(player);
	}

	public String getTeamForPlayer(String player) {
		if (teams.get(CtfTeam.RED).containsPlayer(player)) return CtfTeam.RED;
		else if (teams.get(CtfTeam.BLUE).containsPlayer(player)) return CtfTeam.BLUE;
		else return null;
	}

	public void setBaseLocation(String team, BlockPos refPos) {
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

	public Map<String, CtfTeam> getTeams() {
		return teams;
	}

	public void setTeams(Map<String, CtfTeam> teams) {
		this.teams = teams;
	}

	public Map<String, Integer> getScore() {
		return score;
	}

	public void setScore(Map<String, Integer> score) {
		this.score = score;
	}

	public Map<String, BlockPos> getBaseLocations() {
		return baseLocations;
	}

	public void setBaseLocations(Map<String, BlockPos> baseLocations) {
		this.baseLocations = baseLocations;
	}

	public BlockPos getBaseLocation(String team) {
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

	public void incrementScore(String team) {
		getScore().put(team, getScore().get(team).intValue() + 1);
		save();
	}

	public void setFlagBlockPosition(String flagColour, BlockPos blockPos) {
		flagLocations.put(flagColour, blockPos);
		playerHoldingFlag.remove(flagColour);
		save();
	}

	public BlockPos getFlagPosition(String flagColour) {
		return flagLocations.get(flagColour);
	}

	public void setPlayerHoldingFlag(String flagColour, String player) {
		playerHoldingFlag.put(flagColour, player);
		flagLocations.remove(flagColour);
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
	
}
