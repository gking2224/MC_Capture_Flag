package me.gking2224.mc.mod.ctf.game.data;

import java.util.Optional;

import me.gking2224.mc.mod.ctf.game.GameOptions;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;

public class GameConfigData extends WorldSavedData {

  private static final String DATA_NAME = "CTF_GAME_CONFIG_DATA_";
  private static final String OPTIONS = "options";

  public static GameConfigData create(World world, String name,
    GameOptions options)
  {
    final MapStorage storage = world.getPerWorldStorage();
    final GameConfigData instance = new GameConfigData(name, options);
    storage.setData(getDataIdentifier(name), instance);
    return instance;
  }

  public static Optional<GameConfigData> get(World world, String name) {
    final MapStorage storage = world.getPerWorldStorage();
    final GameConfigData instance = (GameConfigData) storage
            .getOrLoadData(GameConfigData.class, getDataIdentifier(name));
    return Optional.ofNullable(instance);
  }

  private static String getDataIdentifier(String name) {
    return DATA_NAME + name;
  }

  private GameOptions options;
  // private final String name;

  public GameConfigData() {
    this("unknown", null);
  }

  public GameConfigData(String name) {
    this(getDataIdentifier(name), null);
  }

  public GameConfigData(String name, GameOptions options) {
    super(getDataIdentifier(name));
    // this.name = name;
    this.options = options;
  }

  public GameOptions getOptions() {
    return this.options;
  }

  @Override public void readFromNBT(NBTTagCompound nbt) {
    this.options = new GameOptions(nbt.getString(OPTIONS));
  }

  @Override public String toString() {
    return this.options.toString();
  }

  public void update(GameOptions gameOptions) {
    this.options = gameOptions;
    this.setDirty(true);
  }

  @Override public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
    nbt.setString(OPTIONS, this.options.toString());
    return nbt;
  }
}
