package me.gking2224.mc.mod.ctf.game;

import java.util.ArrayList;
import java.util.List;

public class CtfTeam {

	private String colour;
	private List<String> players = new ArrayList<String>();
	
	public CtfTeam(String colour) {
		this.colour = colour;
	}
	public static final String RED = "red";
	public static final String BLUE = "blue";

	public String getColour() {
		return colour;
	}
	public void setColour(String colour) {
		this.colour = colour;
	}
	public List<String> getPlayers() {
		return players;
	}
	public void setPlayers(List<String> players) {
		this.players = players;
	}
	public boolean containsPlayer(String player) {
		return getPlayers().contains(player);
	}
	public int numPlayers() {
		return getPlayers().size();
	}

}
