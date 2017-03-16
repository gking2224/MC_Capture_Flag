package me.gking2224.mc.mod.ctf.game;

import static java.lang.String.format;
import static me.gking2224.mc.mod.ctf.util.StringUtils.toITextComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;

import me.gking2224.mc.mod.ctf.command.BackToBase;
import me.gking2224.mc.mod.ctf.command.CurrentGame;
import me.gking2224.mc.mod.ctf.command.JoinCtfGame;
import me.gking2224.mc.mod.ctf.command.NewCtfGame;
import me.gking2224.mc.mod.ctf.game.event.GameEventManager;
import me.gking2224.mc.mod.ctf.game.event.GameResetEvent;
import me.gking2224.mc.mod.ctf.game.event.NewGameEvent;
import net.minecraft.command.ICommand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class GameManager {
	
	private static GameManager instance = null;
	private transient MinecraftServer server;
	private transient World world;
	
	private GameManager() {
		this.games = new HashMap<String, Game>();
	}

	public static void initialise(MinecraftServer server) {
		GameFileManager.init(server);
		GameWorldManager.init(server);
		GameEventManager.init(server);
		if (instance != null) throw new IllegalStateException();
		instance = GameFileManager.get().readGameManagerFromFile().setServer(server);
	}
	
	private GameManager setServer(MinecraftServer server) {
		this.server = server;
		this.world = server.getEntityWorld();
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

	public Game newGame(String n, EntityPlayer owner)
			throws GameCreationException {
		checkOwnerPermissions(owner);
		checkOwnerLimit(owner);
		String name = checkGameNameUnique(n != null ? n : generateGameName());
		
		Game game = new Game(name, owner, getNewGameBounds());

		MinecraftForge.TERRAIN_GEN_BUS.post(new NewGameEvent(game));
		game.save();
		addGame(game);
		save();
		
		return game;
	}

	private Bounds getNewGameBounds() {

		World world = server.getEntityWorld();
		boolean suitable = false;
		Bounds bounds = null;
		int gameChunksX = 1;
		int gameChunksZ = 1;
		
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

	private String checkGameNameUnique(final String name) throws GameCreationException {
		String rv = name;
		int idx = 0;
		while (gameNames.contains(rv)) rv = name +"-"+(++idx);
		return rv;
	}

	private String generateGameName() {
		return "Game"+(gameNames.size()+1);
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

	public Optional<Game> getGame(String name) {
		if (gameNames.contains(name)) {
			if (!games.containsKey(name)) {
				games.put(name, GameFileManager.get().readGameFromFile(name));
			}
		}
		return Optional.ofNullable(games.get(name));
	}

	public Optional<Game> getPlayerActiveGame(String name) {
		return Optional.ofNullable(games.values().stream().filter(g -> g.containsPlayer(name)).findFirst().orElse(null));
	}

	public void broadcastToAllPlayers(Game game, String msg) {
		game.getAllPlayers().forEach( (player) -> broadCastMessageToPlayer(player, toITextComponent(msg)));
	}

	public void broadCastMessageToPlayer(String player, ITextComponent msg) {
		world.getPlayerEntityByName(player).sendMessage(msg);
	}

	public void broadcastToTeamPlayers(Game game, String team, String msg) {
		game.getTeamPlayers(team).forEach( (player) -> broadCastMessageToPlayer(player, toITextComponent(msg)));
	}

	public List<ICommand> getGameCommands() {
		List<ICommand> rv = new Vector<ICommand>();
		rv.add(new BackToBase());
		rv.add(new NewCtfGame());
		rv.add(new JoinCtfGame());
		rv.add(new CurrentGame());
		return rv;
	}

	public void flagCaptureComplete(Game game, String player, String team, String flagColour) {
		broadcastToAllPlayers(
				game, format("Player %s (team %s) has successfully recovered %s team's flag!", player, team, flagColour));
		game.incrementScore(team);
		broadcastScore(game);
		GameEventManager.get().schedule(() -> MinecraftForge.TERRAIN_GEN_BUS.post(new GameResetEvent(game)), 1000, "Reset game");
	}

	private void broadcastScore(Game game) {
		int redScore = game.getScore().get(CtfTeam.RED);
		int blueScore = game.getScore().get(CtfTeam.BLUE);
		String message = String.format("Score: RED (%d) (%d) BLUE", redScore, blueScore);
		broadcastToAllPlayers(game, message);
		
	}

	public void playerLeaveAllGames(String playerName) {
		games.values().stream().filter(g -> g.getAllPlayers().contains(playerName))
				.forEach(g -> g.removePlayer(playerName));
	}

	public void log(String msg) {
		server.sendMessage(toITextComponent(msg));
	}

}
 