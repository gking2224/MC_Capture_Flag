package me.gking2224.mc.mod.ctf.game;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import net.minecraft.server.MinecraftServer;

import org.apache.commons.io.FileExistsException;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class GameFileManager {
	private static Gson gson = null;
	static {
		gson = new Gson();
	}
	
	private static final String GAMEMANAGER_JSON = "game_mgr.json";
	private static final String GAMES_DIR_NAME = "games";
	
	private GameFileManager() {
		
	}

	static GameManager loadGameManager(MinecraftServer server) {

		File file = getGameManagerFile(server);
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

	private static File getGameManagerFileLocation(MinecraftServer server) {
		return new File(server.getDataDirectory(), GAMEMANAGER_JSON);
	}

	private static File getGameManagerFile(MinecraftServer server) {

		File file = getGameManagerFileLocation(server);
		try {
			if (!file.exists()) {
				initGameManagerFile(server, file);
			}
			return file;
		} catch (JsonIOException | IOException e) {
			throw new GameInitialisationException(e);
		}
	}

	private static void initGameManagerFile(MinecraftServer server, File file) throws JsonIOException, IOException {
		File gamesDirectory = getGamesDir(server);
		if (gamesDirectory.exists() && !gamesDirectory.isDirectory()) {
			throw new GameInitialisationException(new FileExistsException(gamesDirectory));
		}
		else if (!gamesDirectory.exists()) {
			gamesDirectory.mkdirs();
		}
		writeGameManagerToFile(server, GameManager.defaultGameManager(server));
	}

	private static File getGamesDir(MinecraftServer server) {
		return new File(server.getDataDirectory(), GAMES_DIR_NAME);
	}

	static void writeGameToFile(MinecraftServer server, Game game) {
		try {
			File file = new File(getGamesDir(server), game.getName()+".json");
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

	static void writeGameManagerToFile(MinecraftServer server, GameManager gameManager) {
		File file = getGameManagerFileLocation(server);
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
}
