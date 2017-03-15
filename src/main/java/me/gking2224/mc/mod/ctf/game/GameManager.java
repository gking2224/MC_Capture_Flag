package me.gking2224.mc.mod.ctf.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

public class GameManager {
	
	private static GameManager instance = null;
	private transient MinecraftServer server;
	
	private GameManager() {
		this.games = new HashMap<String, Game>();
	}

	public static void initialise(MinecraftServer server) {
		GameFileManager.init(server);
		GameWorldManager.init(server);
		if (instance != null) throw new IllegalStateException();
		instance = GameFileManager.get().readGameManagerFromFile().setServer(server);
	}
	
	private GameManager setServer(MinecraftServer server) {
		this.server = server;
		return this;
	}

	private transient Map<String, Game> games = null;
	
	private List<String> gameNames = new ArrayList<String>();

	static GameManager defaultGameManager(MinecraftServer server) {
		return new GameManager().setServer(server);
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
		GameWorldManager.get().createGameBases(game);
		GameFileManager.get().writeGameToFile(game);
		addGame(game);
		save();
		
		return game;
	}

	private Bounds getNewGameBounds() {

		World world = server.getEntityWorld();
		boolean suitable = false;
		Bounds bounds = null;
		int gameChunksX = 2;
		int gameChunksZ = 3;
		
		while (!suitable) {
			int xBound = 10000;//(server.getMaxWorldSize() / 16) - gameChunksX;
			int startX = world.rand.nextInt(xBound);
			int zBound = 100000;//(server.getMaxWorldSize() / 16) - gameChunksZ;
			int startZ = world.rand.nextInt(zBound);
					
			ChunkLocation from = new ChunkLocation(startX, startZ);
			ChunkLocation to = new ChunkLocation(startX + gameChunksX, startZ + gameChunksZ);
			
  			bounds = new Bounds(from, to);
			
			suitable = !boundaryClashes(bounds) && GameWorldManager.get().isSuitableForGame(bounds);
			
		}
		return bounds;
	}

	private boolean boundaryClashes(Bounds bounds) {
		for (Game game : games.values()) {
			boolean clash = false;
   			if (boundariesOverlap(game.getBounds(), bounds)) clash = true;
			else if (boundariesOverlap(bounds, game.getBounds())) clash = true;
			if (clash) {
				System.out.printf("Game boundary %s not suitable as clashes with game %s boundary (%s)\n", bounds, game.getName(), game.getBounds());
				return true;
			}
		}
		return false;
	}

	private boolean boundariesOverlap(Bounds bounds1, Bounds bounds2) {
		ChunkLocation from1 = bounds1.getFrom();
		ChunkLocation to1 = bounds1.getTo();
		ChunkLocation from2 = bounds2.getFrom();
		ChunkLocation to2 = bounds2.getTo();
		
		boolean fullyAbove = from2.getZ() > to1.getZ() && to2.getZ() > to1.getZ();
		boolean fullyBelow = from2.getZ() < from1.getZ() && to2.getZ() < from1.getZ();
		
		boolean fullyRight = from2.getX() > to1.getX() && to2.getX() > to1.getX();
		boolean fullyLeft = from2.getX() < from1.getX() && to2.getX() < from1.getX();
		
		return !((fullyRight || fullyLeft) && (fullyAbove || fullyBelow));   
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
		gameNames.add(game.getName());
	}

	public void save() {
		GameFileManager.get().writeGameManagerToFile(this);
	}

	public Game getGame(String name) {
		if (gameNames.contains(name)) {
			if (!games.containsKey(name)) {
				games.put(name, GameFileManager.get().readGameFromFile(name));
			}
		}
		return games.get(name);
	}

	public void saveGame(Game game) {
		GameFileManager.get().writeGameToFile(game);
	}

	public Game getPlayerActiveGame(String name) {
		return games.values().stream().filter(g -> g.containsPlayer(name)).findFirst().orElse(null);
	}

}
 