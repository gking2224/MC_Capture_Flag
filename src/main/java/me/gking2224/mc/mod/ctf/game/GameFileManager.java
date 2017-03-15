package me.gking2224.mc.mod.ctf.game;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FileExistsException;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import net.minecraft.server.MinecraftServer;

public class GameFileManager {
	private static Gson gson = null;
	private static GameFileManager instance = null;
	
	static {
		gson = new Gson();
	}
	
	private static final String GAMEMANAGER_JSON = "game_mgr.json";
	private static final String GAMES_DIR_NAME = "games";
	
	private MinecraftServer server = null;
	
	private GameFileManager(MinecraftServer server) {
		this.server = server;
	}

	public static void init(MinecraftServer server) {
		if (instance != null) throw new IllegalStateException();
		instance = new GameFileManager(server);
	}

	GameManager readGameManagerFromFile() {

		File file = getGameManagerFile();
		try {
			FileReader fr = new FileReader(file);
			GameManager gameMgr = gson.fromJson(fr, GameManager.class);
			try {
				fr.close();
			} catch (IOException e) {
				System.out.println(String.format("WARN: caught error closing file %s: %s", file.getAbsolutePath(), e.toString()));
			}
			return gameMgr;
		} catch (JsonSyntaxException | JsonIOException | FileNotFoundException e) {
			throw new GameInitialisationException(e);
		}
	}

	private File getGameManagerFileLocation() {
		return new File(server.getDataDirectory(), GAMEMANAGER_JSON);
	}

	private File getGameManagerFile() {

		File file = getGameManagerFileLocation();
		try {
			if (!file.exists()) {
				initGameManagerFile(file);
			}
			return file;
		} catch (JsonIOException | IOException e) {
			throw new GameInitialisationException(e);
		}
	}

	private void initGameManagerFile(File file) throws JsonIOException, IOException {
		File gamesDirectory = getGamesDir();
		if (gamesDirectory.exists() && !gamesDirectory.isDirectory()) {
			throw new GameInitialisationException(new FileExistsException(gamesDirectory));
		}
		else if (!gamesDirectory.exists()) {
			gamesDirectory.mkdirs();
		}
		writeGameManagerToFile(GameManager.defaultGameManager(server));
	}

	private File getGamesDir() {
		return new File(server.getDataDirectory(), GAMES_DIR_NAME);
	}

	void writeGameToFile(Game game) {
		try {
			File file = new File(getGamesDir(), game.getName()+".json");
			FileWriter fw = new FileWriter(file, false);
			gson.toJson(game, fw);
			try {
				fw.close();
			} catch (IOException e) {
				System.out.println(String.format("WARN: caught error closing file %s: %s", file.getAbsolutePath(), e.toString()));
			}
		} catch (JsonIOException | IOException e) {
			throw new GameInitialisationException(e);
		}
	}
	Game readGameFromFile(String name) {
		try {
			File file = new File(getGamesDir(), name+".json");
			FileReader fr = new FileReader(file);
			Game game = gson.fromJson(fr, Game.class);
			try {
				fr.close();
			} catch (IOException e) {
				System.out.println(String.format("WARN: caught error closing file %s: %s", file.getAbsolutePath(), e.toString()));
			}
			return game;
		} catch (JsonSyntaxException | JsonIOException | FileNotFoundException e) {
			throw new GameInitialisationException(e);
		}
	}

	void writeGameManagerToFile(GameManager gameManager) {
		File file = getGameManagerFileLocation();
		try {
			FileWriter fw = new FileWriter(file);
			gson.toJson(gameManager, fw);
			try {
				fw.close();
			} catch (IOException e) {
				System.out.println(String.format("WARN: caught error closing file %s: %s", file.getAbsolutePath(), e.toString()));
			}
		} catch (JsonIOException | IOException e) {
			throw new GameManagerPersistenceException(e);
		}
	}

	public static GameFileManager get() {
		return instance;
	}
}
