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

  private static final int MIN_WIDTH_CHUNKS = 2;
  private static final int MIN_LENGTH_CHUNKS = 3;

  @SuppressWarnings("unused") private static Logger LOGGER = Logger
          .getLogger(GameManager.class.getName());

  private static GameManager instance = null;

  public static GameManager get() {
    return instance;
  }

  public static void initialise(MinecraftServer server) {
    if (instance != null) { throw new IllegalStateException(); }
    instance = new GameManager(server);
    GameWorldManager.init(server);
    GameEventManager.init(server);

  }

  private transient MinecraftServer server;

  private transient World world;

  private final GameList gameList;

  private Map<String, Game> games = null;

  private GameManager(MinecraftServer server) {
    this.server = server;
    this.world = server.getEntityWorld();
    this.gameList = GameList.get(this.world);
    this.games = new HashMap<String, Game>();
  }

  private void addGame(Game game) {
    this.gameList.add(game.getName());
    this.games.put(game.getName(), game);
    this.save();
  }

  private boolean boundariesOverlap(Bounds bounds1, Bounds bounds2) {
    final BlockPos f1 = bounds1.getFrom();
    final BlockPos to1 = bounds1.getTo();
    final BlockPos f2 = bounds2.getFrom();
    final BlockPos to2 = bounds2.getTo();

    final boolean fullyAbove = f2.getZ() > to1.getZ()
            && to2.getZ() > to1.getZ();
    final boolean fullyBelow = f2.getZ() < f1.getZ() && to2.getZ() < f1.getZ();

    final boolean fullyRight = f2.getX() > to1.getX()
            && to2.getX() > to1.getX();
    final boolean fullyLeft = f2.getX() < f1.getX() && to2.getX() < f1.getX();

    return !((fullyRight || fullyLeft) && (fullyAbove || fullyBelow));
  }

  private boolean boundaryClashes(Bounds bounds) {
    for (final Game game : this.games.values()) {
      boolean clash = false;
      if (this.boundariesOverlap(game.getBounds(), bounds)) {
        clash = true;
      } else if (this.boundariesOverlap(bounds, game.getBounds())) {
        clash = true;
      }
      if (clash) {
        System.out.printf(
                "Game boundary %s not suitable as clashes "
                        + "with game %s boundary (%s)\n",
                bounds, game.getName(), game.getBounds());
        return true;
      }
    }
    return false;
  }

  public void broadCastMessageToPlayer(String playerName, ITextComponent msg) {
    System.out.printf("Sending message %s to player %s\n", msg, playerName);
    final Optional<EntityPlayer> player = this.getPlayerByName(playerName);
    player.ifPresent(p -> p.sendMessage(msg));
  }

  private void broadcastScore(Game game) {
    this.broadcastToAllPlayers(game, game.getFormattedScore());
  }

  public void broadcastToAllPlayers(Game game, String msg) {
    game.getAllPlayers().forEach(
            (player) -> this.broadCastMessageToPlayer(player, toIText(msg)));
  }

  public void broadcastToTeamPlayers(Game game, TeamColour colour, String msg) {
    game.getTeamPlayers(colour).forEach(
            (player) -> this.broadCastMessageToPlayer(player, toIText(msg)));
  }

  private String checkGameNameUnique(final String name)
    throws GameCreationException
  {
    String rv = name;
    int idx = 0;
    while (this.gameList.contains(rv)) {
      rv = name + "-" + (++idx);
    }
    return rv;
  }

  private void checkOwnerLimit(EntityPlayer owner) {
    // TODO Auto-generated method stub
  }

  private void checkOwnerPermissions(EntityPlayer owner) {
    // TODO Auto-generated method stub
  }

  public void gameRoundWon(Game game, String player, CtfTeam team,
    TeamColour capturedFlagColour)
  {
    this.broadcastToAllPlayers(game,
            format("Player %s won the round for team %s!", player,
                    team.getColour(), capturedFlagColour));
    game.incrementScore(team.getColour()); // to new method
    this.broadcastScore(game);
    this.resetGame(game);
  }

  private String generateGameName() {
    return "Game" + (this.gameList.numGames() + 1);
  }

  public Set<String> getAllGames() {
    return new TreeSet<String>(this.gameList.getGames());
  }

  public Set<Game> getAllGamesWithPlayer(String playerName) {
    return this.games.values().stream()
            .filter((g) -> g.containsPlayer(playerName))
            .collect(Collectors.toSet());
  }

  public Optional<Game> getGame(String name) {
    Game game = null;
    if (this.games.containsKey(name)) {
      game = this.games.get(name);
      System.out.printf("Got cached game: %s\n", game);
    } else if (this.gameList.contains(name)) {
      game = Game.load(this.world, name).orElse(null);
      this.games.put(name, game);
      System.out.printf("Loaded game: %s\n", game);
    }
    return Optional.ofNullable(game);
  }

  public List<ICommand> getGameCommands() {
    final List<ICommand> rv = new Vector<ICommand>();
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

  private Bounds getNewGameBounds(GameOptions options) {

    final World world = this.server.getEntityWorld();
    boolean suitable = false;
    Bounds bounds = null;

    final int size = options.getInteger("size").orElse(1);
    final int gameChunksX = MIN_WIDTH_CHUNKS * size;
    final int gameChunksZ = MIN_LENGTH_CHUNKS * size;

    while (!suitable) {
      final int worldsize = this.server.getMaxWorldSize();
      System.out.printf("max world size: %d\n", worldsize);
      final int xBound = 100000;// server.getMaxWorldSize() - (gameChunksX*16);
      final int startX = world.rand.nextInt(xBound) * 16;
      final int zBound = 100000;// server.getMaxWorldSize() - (gameChunksZ*16);
      final int startZ = world.rand.nextInt(zBound) * 16;

      bounds = new Bounds(new BlockPos(startX, 0, startZ), new BlockPos(
              startX + (gameChunksX * 16), 0, startZ + (gameChunksZ * 16)));

      final GameWorldManager gwm = GameWorldManager.get();
      suitable = !this.boundaryClashes(bounds) && gwm.isSuitableForGame(bounds);

    }
    return bounds;
  }

  public Optional<Game> getPlayerActiveGame(String name) {
    return Optional.ofNullable(this.games.values().stream()
            .filter(g -> g.containsPlayer(name)).findFirst().orElse(null));
  }

  public Optional<EntityPlayer> getPlayerByName(String playerName) {
    return Optional.ofNullable(this.world.getPlayerEntityByName(playerName));
  }

  public void log(String msg) {
    this.server.sendMessage(toIText(msg));
  }

  public Game newGame(EntityPlayer owner, GameOptions options)
    throws GameCreationException
  {
    this.checkOwnerPermissions(owner);
    this.checkOwnerLimit(owner);
    final String name = this.checkGameNameUnique(this.generateGameName());

    final Bounds newGameBounds = this.getNewGameBounds(options);
    System.out.printf("NEw game bounds: %s\n", newGameBounds);
    final Game game = new Game(this.world, name, owner, newGameBounds, options);

    MinecraftForge.TERRAIN_GEN_BUS.post(new NewGameEvent(game));
    game.save();
    this.addGame(game);
    this.save();

    return game;
  }

  public void playerLeaveAllGames(String playerName) {
    this.games.values().stream()
            .filter(g -> g.getAllPlayers().contains(playerName))
            .forEach(g -> g.removePlayer(playerName));
  }

  private void resetGame(Game game) {
    this.server.addScheduledTask(() -> MinecraftForge.TERRAIN_GEN_BUS
            .post(new GameResetEvent(game)));
    game.getAllPlayers().forEach(playerName -> {
      final Optional<EntityPlayer> ep = this.getPlayerByName(playerName);
      ep.ifPresent(player -> {
        this.toolUpPlayer(player);
        this.sendPlayerToBase(game, player);
      });
    });
  }

  public void save() {
    this.gameList.setDirty(true);
  }

  public void sendPlayerToBase(Game game, EntityPlayer player) {
    final Optional<CtfTeam> t = game.getTeamForPlayer(player.getName());
    t.ifPresent(team -> {
      final BlockPos baseLocation = game.getBaseLocation(team.getColour());
      final int x = baseLocation.getX() + 2, z = baseLocation.getZ() + 2;
      final int y = GameWorldManager.get().getWorldHeight(x, z) + 1;
      player.setPosition(x, y, z);
    });

  }

  public void toolUpPlayer(EntityPlayer p) {
    InventoryUtils.setPlayerInventory(p,
            GameInventoryFactory.getDefault().getGameItems());
  }
}
