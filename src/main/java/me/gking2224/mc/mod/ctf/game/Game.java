package me.gking2224.mc.mod.ctf.game;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;

public class Game {

	private String owner;
	private String name;
	private Bounds bounds;
	private Map<String, CtfTeam> teams = new HashMap<String, CtfTeam>();
	private Map<String, Integer> score = new HashMap<String, Integer>();

	Game(String name, EntityPlayer owner, Bounds bounds) {
		setName(name);
		setOwner(owner.getName());
		setBounds(bounds);
		teams.put(CtfTeam.RED, new CtfTeam(CtfTeam.RED));
		teams.put(CtfTeam.BLUE, new CtfTeam(CtfTeam.BLUE));
		score.put(CtfTeam.RED, 0);
		score.put(CtfTeam.BLUE, 0);
	}
	
	public String addPlayer(String name) {
		String joiningTeam = null;
		if (teamNumPlayers(CtfTeam.RED) > teamNumPlayers(CtfTeam.BLUE)) {
			joiningTeam = CtfTeam.BLUE;
		}
		else if (teamNumPlayers(CtfTeam.BLUE) > teamNumPlayers(CtfTeam.RED)) {
			joiningTeam = CtfTeam.RED;
		}
		else joiningTeam = chooseRandomTeam();
		teams.get(joiningTeam).getPlayers().add(name);
		save();
		return joiningTeam;
	}
	
	private void save() {
		GameManager.get().saveGame(this);
	}

	private String chooseRandomTeam() {
		return (new java.util.Random().nextBoolean()) ? CtfTeam.RED : CtfTeam.BLUE;
	}

	protected int teamNumPlayers(String team) {
		return teams.get(team).getPlayers().size();
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

	public boolean containsPlayer(String player) {
		return teams.get(CtfTeam.RED).containsPlayer(player) ||
				teams.get(CtfTeam.BLUE).containsPlayer(player);
	}

	public String getTeamForPlayer(String player) {
		if (teams.get(CtfTeam.RED).containsPlayer(player)) return CtfTeam.RED;
		else if (teams.get(CtfTeam.BLUE).containsPlayer(player)) return CtfTeam.BLUE;
		else return null;
	}
	
	
}
