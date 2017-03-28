package me.gking2224.mc.mod.ctf.game.data;

import static java.lang.String.format;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;

public class GameConfigList extends WorldSavedData {

  private static final String DATA_NAME = "CTF_GAME_CONFIGS";
  private static final String NUM_CONFIGS = "num";
  private static final String CONFIG = "config";

  public static GameConfigList get(World world) {
    final MapStorage storage = world.getPerWorldStorage();
    GameConfigList instance = (GameConfigList) storage.getOrLoadData(GameConfigList.class,
            DATA_NAME);
    if (instance == null) {
      instance = new GameConfigList();
      storage.setData(DATA_NAME, instance);
      System.out.println(String.format("Stored new GameList: %s\n",  instance));
    } else {
      System.out.println(String.format("Loaded GameList: %s\n",  instance));
    }
    return instance;
  }

  private Set<String> configs;

  public GameConfigList() {
    this(DATA_NAME);
  }

  public GameConfigList(String dataName) {
    super(DATA_NAME);
    this.configs = new HashSet<String>();
  }

  public void add(String name) {
    this.configs.add(name);
  }

  public boolean contains(String rv) {
    return this.configs.contains(rv);
  }

  public Set<String> getGames() {
    return new HashSet<String>(this.configs);
  }

  public int numGames() {
    return this.configs.size();
  }

  @Override public void readFromNBT(NBTTagCompound nbt) {
    final Set<String> games = new HashSet<String>();
    final int numGames = nbt.getInteger(NUM_CONFIGS);
    for (int i = 0; i < numGames; i++) {
      games.add(nbt.getString(CONFIG + i));
    }
    this.configs = games;
  }

  @Override public String toString() {
    return format("GameConfigList[%s]\n", this.configs);
  }

  @Override public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
    nbt.setInteger(NUM_CONFIGS, this.configs.size());
    int i = 0;
    for (final String game : this.configs) {
      nbt.setString(CONFIG + (i++), game);
    }
    return nbt;
  }
}
