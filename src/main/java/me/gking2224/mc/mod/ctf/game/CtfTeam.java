package me.gking2224.mc.mod.ctf.game;

import java.util.HashSet;
import java.util.Set;

public class CtfTeam {
	
	public static final String RED = "red";
	public static final String BLUE = "blue";

	private String colour;
	private Set<String> players = new HashSet<String>();
	
	public CtfTeam(String colour) {
		this.colour = colour;
	}

	public String getColour() {
		return colour;
	}
	public void setColour(String colour) {
		this.colour = colour;
	}
	public Set<String> getPlayers() {
		return new HashSet<String>(players);
	}
	public void setPlayers(Set<String> players) {
		this.players = new HashSet<String>(players);
	}
	public void addPlayer(String player) {
		this.players.add(player);
	}
	public boolean containsPlayer(String player) {
		return getPlayers().contains(player);
	}
	public int numPlayers() {
		return getPlayers().size();
	}
	public void removePlayer(String playerName) {
		players.remove(playerName);
	}

	public String toString() {
		return String.format("CtfTeam[colour=%s; players=%s]", colour, players);
	}
}
