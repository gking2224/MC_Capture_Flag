package me.gking2224.mc.mod.ctf.game;

import static java.lang.String.format;
import static me.gking2224.mc.mod.ctf.util.StringUtils.toIText;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import me.gking2224.mc.mod.ctf.command.BackToBase;
import me.gking2224.mc.mod.ctf.command.CurrentGame;
import me.gking2224.mc.mod.ctf.command.GameInfo;
import me.gking2224.mc.mod.ctf.command.GetScore;
import me.gking2224.mc.mod.ctf.command.JoinCtfGame;
import me.gking2224.mc.mod.ctf.command.NewCtfGame;
import me.gking2224.mc.mod.ctf.command.ToolUp;
import me.gking2224.mc.mod.ctf.game.CtfTeam.TeamColour;
import me.gking2224.mc.mod.ctf.game.data.GameList;
import me.gking2224.mc.mod.ctf.game.event.GameEventManager;
import me.gking2224.mc.mod.ctf.game.event.GameResetEvent;
import me.gking2224.mc.mod.ctf.game.event.NewGameEvent;
import me.gking2224.mc.mod.ctf.util.InventoryUtils;
import net.minecraft.command.ICommand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class GameManager {
	
	private static Logger LOGGER = Logger.getLogger(GameManager.class.getName()); 
	
	private static GameManager instance = null;
	private transient MinecraftServer server;
	private transient World world;
	
	private GameList gameList;

	private Map<String, Game> games = null;
	
	private GameManager(MinecraftServer server) {
		this.server = server;
		this.world = server.getEntityWorld();
		gameList = GameList.get(this.world);
		games = new HashMap<String, Game>();
	}

	public static void initialise(MinecraftServer server) {
		if (instance != null) throw new IllegalStateException();
		instance = new GameManager(server);
		GameWorldManager.init(server);
		GameEventManager.init(server);
		
	}

	public static GameManager get() {
		return instance;
	}

	public Game newGame(EntityPlayer owner, GameOptions options)
			throws GameCreationException {
		checkOwnerPermissions(owner);
		checkOwnerLimit(owner);
		String name = checkGameNameUnique(generateGameName());
		
		Bounds newGameBounds = getNewGameBounds(options);
		System.out.printf("NEw game bounds: %s\n", newGameBounds);
		Game game = new Game(world, name, owner, newGameBounds, options);

		MinecraftForge.TERRAIN_GEN_BUS.post(new NewGameEvent(game));
		game.save();
		addGame(game);
		save();
		
		return game;
	}

	private Bounds getNewGameBounds(GameOptions options) {

		World world = server.getEntityWorld();
		boolean suitable = false;
		Bounds bounds = null;
		
		int size = options.getInteger("size").orElse(1);
		int gameChunksX = 1 * size;
		int gameChunksZ = 2 * size;
		
		while (!suitable) {
			int xBound = 10000;//server.getMaxWorldSize() - (gameChunksX*16);
			int startX = world.rand.nextInt(xBound) *16;
			int zBound = 10000;//server.getMaxWorldSize() - (gameChunksZ*16);
			int startZ = world.rand.nextInt(zBound) * 16;
					
  			bounds = new Bounds(new BlockPos(startX, 0, startZ), new BlockPos(startX + (gameChunksX*16), 0, startZ + (gameChunksZ * 16)));
			
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
		BlockPos from1 = bounds1.getFrom();
		BlockPos to1 = bounds1.getTo();
		BlockPos from2 = bounds2.getFrom();
		BlockPos to2 = bounds2.getTo();
		
		boolean fullyAbove = from2.getZ() > to1.getZ() && to2.getZ() > to1.getZ();
		boolean fullyBelow = from2.getZ() < from1.getZ() && to2.getZ() < from1.getZ();
		
		boolean fullyRight = from2.getX() > to1.getX() && to2.getX() > to1.getX();
		boolean fullyLeft = from2.getX() < from1.getX() && to2.getX() < from1.getX();
		
		return !((fullyRight || fullyLeft) && (fullyAbove || fullyBelow));   
	}

	private String checkGameNameUnique(final String name) throws GameCreationException {
		String rv = name;
		int idx = 0;
		while (gameList.contains(rv)) rv = name +"-"+(++idx);
		return rv;
	}

	private String generateGameName() {
		return "Game"+(gameList.numGames()+1);
	}

	private void checkOwnerLimit(EntityPlayer owner) {
		// TODO Auto-generated method stub
	}

	private void checkOwnerPermissions(EntityPlayer owner) {
		// TODO Auto-generated method stub
	}

	private void addGame(Game game) {
		gameList.add(game.getName());
		this.games.put(game.getName(), game);
		save();
	}

	public void save() {
		gameList.setDirty(true);
	}

	public Optional<Game> getGame(String name) {
		Game game = null;
		if (games.containsKey(name)) {
			game = games.get(name);
			System.out.printf("Got cached game: %s\n", game);
		}
		else if (gameList.contains(name)) {
			game = Game.load(world, name).orElse(null);
			games.put(name, game);
			System.out.printf("Loaded game: %s\n", game);
		}
		return Optional.ofNullable(game);
	}

	public Optional<Game> getPlayerActiveGame(String name) {
		return Optional.ofNullable(games.values().stream().filter(g -> g.containsPlayer(name)).findFirst().orElse(null));
	}

	public void broadcastToAllPlayers(Game game, String msg) {
		game.getAllPlayers().forEach( (player) -> broadCastMessageToPlayer(player, toIText(msg)));
	}

	public void broadCastMessageToPlayer(String playerName, ITextComponent msg) {
		System.out.printf("Sending message %s to player %s\n", msg, playerName);
		Optional<EntityPlayer> player = getPlayerByName(playerName);
		player.ifPresent(p->p.sendMessage(msg));
	}

	public void broadcastToTeamPlayers(Game game, TeamColour colour, String msg) {
		game.getTeamPlayers( colour).forEach( (player) -> broadCastMessageToPlayer(player, toIText(msg)));
	}

	public List<ICommand> getGameCommands() {
		List<ICommand> rv = new Vector<ICommand>();
		rv.add(new BackToBase());
		rv.add(new NewCtfGame());
		rv.add(new JoinCtfGame());
		rv.add(new CurrentGame());
		rv.add(new me.gking2224.mc.mod.ctf.command.GameList());
		rv.add(new GameInfo());
		rv.add(new ToolUp());
		rv.add(new GetScore());
		return rv;
	}

	public void gameRoundWon(Game game, String player, CtfTeam team, TeamColour capturedFlagColour) {
		broadcastToAllPlayers(
				game, format("Player %s won the round for team %s!", player, team.getColour(), capturedFlagColour));
		game.incrementScore(team.getColour()); // to new method
		broadcastScore(game);
		resetGame(game);
	}

	private void resetGame(Game game) {
		server.addScheduledTask(() -> MinecraftForge.TERRAIN_GEN_BUS.post(new GameResetEvent(game)));
		game.getAllPlayers().forEach( playerName -> {
			Optional<EntityPlayer> ep = getPlayerByName(playerName);
			ep.ifPresent( player -> {
				toolUpPlayer(player);
				sendPlayerToBase(game, player);
			});
		});
	}

	public void sendPlayerToBase(Game game, EntityPlayer player) {
		Optional<CtfTeam> t = game.getTeamForPlayer(player.getName());
		t.ifPresent(team -> {
			BlockPos baseLocation = game.getBaseLocation(team.getColour());
			int x = baseLocation.getX() + 2, z = baseLocation.getZ() + 2;
			int y = GameWorldManager.get().getWorldHeight(x, z) + 1;
			player.setPosition(x, y, z);
		});
		
	}
	
	public void toolUpPlayer(EntityPlayer p) {
		InventoryUtils.setPlayerInventory(p, GameInventoryFactory.getDefault().getGameItems());
	}

	private void broadcastScore(Game game) {
		broadcastToAllPlayers(game, game.getFormattedScore());
	}

	public void playerLeaveAllGames(String playerName) {
		games.values().stream().filter(g -> g.getAllPlayers().contains(playerName))
				.forEach(g -> g.removePlayer(playerName));
	}

	public void log(String msg) {
		server.sendMessage(toIText(msg));
	}

	public Set<Game> getAllGamesWithPlayer(String playerName) {
		return games.values().stream().filter((g) -> g.containsPlayer(playerName)).collect(Collectors.toSet());
	}

	public Set<String> getAllGames() {
		return new TreeSet<String>(gameList.getGames());
	}
	
	public Optional<EntityPlayer> getPlayerByName(String playerName) {
		return Optional.ofNullable(world.getPlayerEntityByName(playerName));
	}
}
 