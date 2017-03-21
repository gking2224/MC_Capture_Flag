package me.gking2224.mc.mod.ctf.game;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;

public class CtfTeam {

  public static enum TeamColour {
    RED("red"), BLUE("blue");

    public static TeamColour fromString(String s) {
      if ("red".equals(s)) {
        return RED;
      } else if ("blue".equals(s)) {
        return BLUE;
      } else {
        throw new IllegalArgumentException("Unknown colour");
      }
    }

    private String colour;

    private TeamColour(String colour) {
      this.colour = colour;
    }

    public String getColour() {
      return this.colour;
    }

    @Override public String toString() {
      return this.getColour();
    }
  }

  public static CtfTeam readFromNBT(NBTTagCompound nbt, String prefix) {
    final TeamColour colour = TeamColour
            .fromString(nbt.getString(prefix + "colour"));
    final int numPlayers = nbt.getInteger(prefix + "numPlayers");
    final Set<String> players = new HashSet<String>();
    for (int i = 0; i < numPlayers; i++) {
      players.add(nbt.getString(prefix + "player" + i));
    }
    return new CtfTeam(colour, players);
  }

  public static void writeToNBT(NBTTagCompound nbt, String prefix,
    CtfTeam team)
  {
    nbt.setString(prefix + "colour", team.getColour().getColour());
    nbt.setInteger(prefix + "numPlayers", team.getPlayers().size());
    int i = 0;
    for (final String p : team.getPlayers()) {
      nbt.setString(prefix + "player" + i, p);
      i++;
    }
  }

  private TeamColour colour;

  private Set<String> players;

  public CtfTeam(TeamColour colour) {
    this(colour, new HashSet<String>());
  }

  public CtfTeam(TeamColour colour, Set<String> players) {
    this.colour = colour;
    this.players = players;
  }

  public void addPlayer(String player) {
    this.players.add(player);
  }

  public boolean containsPlayer(String player) {
    return this.getPlayers().contains(player);
  }

  public TeamColour getColour() {
    return this.colour;
  }

  public Set<String> getPlayers() {
    return new HashSet<String>(this.players);
  }

  public int numPlayers() {
    return this.getPlayers().size();
  }

  public void removePlayer(String playerName) {
    this.players.remove(playerName);
  }

  public void setColour(TeamColour colour) {
    this.colour = colour;
  }

  public void setPlayers(Set<String> players) {
    this.players = new HashSet<String>(players);
  }

  @Override public String toString() {
    return String.format("CtfTeam[colour=%s; players=%s]", this.colour,
            this.players);
  }
}
