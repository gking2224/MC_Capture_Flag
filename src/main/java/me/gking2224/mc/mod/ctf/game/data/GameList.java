package me.gking2224.mc.mod.ctf.game.data;

import static java.lang.String.format;

import java.util.Collection;
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

	private Set<String> games;

	public GameList() {
		this(DATA_NAME);
	}

	public GameList(String dataName) {
		super(DATA_NAME);
		this.games = new HashSet<String>();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		Set<String> games = new HashSet<String>();
		int numGames = nbt.getInteger(NUM_GAMES);
		for (int i = 0; i < numGames; i++) {
			games.add(nbt.getString(GAME + i));
		}
		this.games = games;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setInteger(NUM_GAMES, games.size());
		int i = 0;
		for (String game : games) {
			nbt.setString(GAME+(i++), game);
		}
		return nbt;
	}
	
	public String toString() {
		return format("GameList[%s]\n", this.games);
	}

	public static GameList get(World world) {
		MapStorage storage = world.getPerWorldStorage();
		GameList instance = (GameList) storage.getOrLoadData(GameList.class, DATA_NAME);
		if (instance == null) {
			instance = new GameList();
			storage.setData(DATA_NAME, instance);
			System.out.printf("Stored new GameList: %s\n", instance);
		}
		else {
			System.out.printf("Loaded GameList: %s\n", instance);
		}
		return instance;
	}

	public boolean contains(String rv) {
		return games.contains(rv);
	}

	public int numGames() {
		return games.size();
	}

	public void add(String name) {
		games.add(name);
	}

	public Set<String> getGames() {
		return new HashSet<String>(games);
	}
}
