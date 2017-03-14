package me.gking2224.mc.mod.ctf.game;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class GameManager {
	
	private static GameManager instance = null;
	private transient MinecraftServer server;
	
	private GameManager(MinecraftServer server) {
		this.server = server;
	}

	public static void initialise(MinecraftServer server) {
		instance = GameFileManager.loadGameManager(server).setServer(server);
	}
	
	private GameManager setServer(MinecraftServer server) {
		this.server = server;
		return this;
	}

	private Map<String, Game> games = new HashMap<String, Game>();

	static GameManager defaultGameManager(MinecraftServer server) {
		return new GameManager(server);
	}

	public static GameManager get() {
		return instance;
	}

	public Game newGame(String name, EntityPlayer owner)
			throws GameCreationException {
		checkOwnerPermissions(owner);
		checkOwnerLimit(owner);
		checkNameUnique(name);
		
		Game game = new Game(name, owner, getNewGameBounds());
		GameWorldManager.get().createGameBases(game, server.getEntityWorld());
		GameFileManager.writeGameToFile(server, game);
		addGame(game);
		save();
		
		return game;
	}

	private Bounds getNewGameBounds() {
		ChunkLocation from = new ChunkLocation(0, 10);
		ChunkLocation to = new ChunkLocation(5, 15);
		return new Bounds(from, to);
	}

	private void checkNameUnique(String name) throws GameCreationException {
		if (games.containsKey(name))
			throw new GameCreationException(String.format("Game %s already exists!", name));
	}

	private void checkOwnerLimit(EntityPlayer owner) {
		// TODO Auto-generated method stub
	}

	private void checkOwnerPermissions(EntityPlayer owner) {
		// TODO Auto-generated method stub
	}

	private void addGame(Game game) {
		this.games.put(game.getName(), game);
	}

	public void save() {
		GameFileManager.writeGameManagerToFile(server, this);
	}

	public Game getGame(String name) {
		return games.get(name);
	}

	public void saveGame(Game game) {
		GameFileManager.writeGameToFile(server, game);
	}

}
