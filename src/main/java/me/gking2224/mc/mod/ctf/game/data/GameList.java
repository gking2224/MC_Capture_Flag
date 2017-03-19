package me.gking2224.mc.mod.ctf.game.data;

import static java.lang.String.format;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;

public class GameList extends WorldSavedData {

  private static final String GAME = "game";
  private static final String NUM_GAMES = "num";
  private static final String DATA_NAME = "CTF_GAMES";

  public static GameList get(World world) {
    final MapStorage storage = world.getPerWorldStorage();
    GameList instance = (GameList) storage.getOrLoadData(GameList.class,
            DATA_NAME);
    if (instance == null) {
      instance = new GameList();
      storage.setData(DATA_NAME, instance);
      System.out.printf("Stored new GameList: %s\n", instance);
    } else {
      System.out.printf("Loaded GameList: %s\n", instance);
    }
    return instance;
  }

  private Set<String> games;

  public GameList() {
    this(DATA_NAME);
  }

  public GameList(String dataName) {
    super(DATA_NAME);
    this.games = new HashSet<String>();
  }

  public void add(String name) {
    this.games.add(name);
  }

  public boolean contains(String rv) {
    return this.games.contains(rv);
  }

  public Set<String> getGames() {
    return new HashSet<String>(this.games);
  }

  public int numGames() {
    return this.games.size();
  }

  @Override public void readFromNBT(NBTTagCompound nbt) {
    final Set<String> games = new HashSet<String>();
    final int numGames = nbt.getInteger(NUM_GAMES);
    for (int i = 0; i < numGames; i++) {
      games.add(nbt.getString(GAME + i));
    }
    this.games = games;
  }

  @Override public String toString() {
    return format("GameList[%s]\n", this.games);
  }

  @Override public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
    nbt.setInteger(NUM_GAMES, this.games.size());
    int i = 0;
    for (final String game : this.games) {
      nbt.setString(GAME + (i++), game);
    }
    return nbt;
  }
}
