package me.gking2224.mc.mod.ctf.game;

import static java.lang.String.format;
import static me.gking2224.mc.mod.ctf.util.InventoryUtils.moveItemFromInventoryToPlayerHand;
import static me.gking2224.mc.mod.ctf.util.StringUtils.toIText;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import me.gking2224.mc.mod.ctf.command.BackToBase;
import me.gking2224.mc.mod.ctf.command.BaseDirections;
import me.gking2224.mc.mod.ctf.command.CurrentGame;
import me.gking2224.mc.mod.ctf.command.GameInfo;
import me.gking2224.mc.mod.ctf.command.GetScore;
import me.gking2224.mc.mod.ctf.command.JoinCtfGame;
import me.gking2224.mc.mod.ctf.command.NewCtfGame;
import me.gking2224.mc.mod.ctf.command.SaveGameConfiguration;
import me.gking2224.mc.mod.ctf.command.ToolUp;
import me.gking2224.mc.mod.ctf.game.CtfTeam.TeamColour;
import me.gking2224.mc.mod.ctf.game.data.GameConfigData;
import me.gking2224.mc.mod.ctf.game.data.GameList;
import me.gking2224.mc.mod.ctf.game.event.GameEventManager;
import me.gking2224.mc.mod.ctf.game.event.GameResetEvent;
import me.gking2224.mc.mod.ctf.game.event.NewGameEvent;
import me.gking2224.mc.mod.ctf.item.ItemBase;
import me.gking2224.mc.mod.ctf.net.CanMovePlayerToPosition;
import me.gking2224.mc.mod.ctf.net.CtfNetworkHandler;
import me.gking2224.mc.mod.ctf.util.WorldUtils;
import net.minecraft.command.ICommand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class GameManager {

  private static final int MIN_WIDTH_CHUNKS = 2;
  private static final int MIN_LENGTH_CHUNKS = 3;
  private static final Integer SIZE_XS = 1;
  private static final Integer SIZE_S = 3;
  private static final Integer SIZE_L = 9;
  private static final Integer SIZE_XL = 15;
  private static final Integer SIZE_M = 5;

  @SuppressWarnings("unused") private static Logger LOGGER = Logger
          .getLogger(GameManager.class.getName());

  private static GameManager instance = null;
  private static GameEventManager gem;
  private static GameWorldManager gwm;

  public static GameManager get() {
    return instance;
  }

  public static void initialise(MinecraftServer server) {
    if (instance != null) { throw new IllegalStateException(); }
    instance = new GameManager(server);
    gwm = GameWorldManager.init(server, instance);
    gem = GameEventManager.init(server, instance);
  }

  private transient MinecraftServer server;

  private transient World world;

  private final GameList gameList;

  private Map<String, Game> games = null;
  private final Set<String> frozenPlayers = new HashSet<String>();

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

  private Stream<Game> allGamesWithPlayer(String playerName) {
    return this.games.values().stream().filter(g -> {
      return g.getAllPlayers().contains(playerName);
    });
  }

  private boolean allowAttackWithProbability(double probability) {
    final float randomVal = this.world.rand.nextFloat();
    return randomVal <= (probability / 2);
  }

  public boolean allowPlayerToAttackedPlayer(EntityPlayer attacker,
    EntityPlayer attackee)
  {
    final String attackerName = attacker.getName();
    final Optional<Game> g1 = this.getPlayerActiveGame(attackerName);
    if (!g1.isPresent()) { return false; }
    final String attackeeName = attackee.getName();
    final Optional<Game> g2 = this.getPlayerActiveGame(attackeeName);
    if (!g2.isPresent()) { return false; }
    final Game game = g1.get();

    if (game != g2.get()) { return false; }

    final Optional<CtfTeam> teamAter = game.getTeamForPlayer(attackerName);
    final Optional<CtfTeam> teamAtee = game.getTeamForPlayer(attackeeName);
    if (!teamAter.isPresent() || !teamAtee.isPresent()) { return false; }
    final int sAter = game.getScore().get(teamAter.get().getColour());
    final int sAtee = game.getScore().get(teamAtee.get().getColour());

    return this.allowAttackWithProbability(
            this.getAllowHitProbability(sAter - sAtee));
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
    System.out.println(String.format("Sending message %s to player %s\n", msg,
            playerName));
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

  public void clientChunkGenerated(EntityPlayer playerEntity) {

    // TODO Auto-generated method stub

  }

  public void completeMovePlayerToPosition(EntityPlayer player, final int x,
    final int y, final int z)
  {
    player.setPosition(x, y, z);
  }

  private void createBonusChest(Game game) {
    final BlockPos pos = WorldUtils.randomPointInBounds(this.world,
            game.getBounds());
    final TileEntityChest chest = WorldUtils.placeChest(this.world, pos);
    final GameInventory inventory = GameInventoryFactory
            .get(game.getOptions().getString(GameOption.BONUS_CHEST_INVENTORY)
                    .orElse(GameInventoryFactory.DEFAULT_BONUS_CHEST));
    inventory.placeInChest(chest);
    System.out.println(format("created bonus chest at %s", pos));

  }

  public void freezePlayerOut(EntityPlayer player) {
    this.frozenPlayers.add(player.getName());
  }

  private Supplier<GameCreationException> gameConfigNotFound(String config) {
    return () -> new GameCreationException("Configuration %s not found",
            config);
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

  private double getAllowHitProbability(int sDiff) {
    return Math.min(10, Math.max(0, 10.0d - sDiff)) / 10;
  }

  public Optional<Game> getGame(String name) {
    Game game = null;
    if (this.games.containsKey(name)) {
      game = this.games.get(name);
      System.out.println(String.format("Got cached game: %s\n", game));
    } else if (this.gameList.contains(name)) {
      game = Game.load(this.world, name).orElse(null);
      this.games.put(name, game);
      System.out.println(String.format("Loaded game: %s\n", game));
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
    rv.add(new BaseDirections());
    rv.add(new SaveGameConfiguration());
    return rv;
  }

  private Bounds getNewGameBounds(GameOptions options) {

    final World world = this.server.getEntityWorld();
    boolean suitable = false;
    Bounds bounds = null;

    final int size = this.getSize(options);
    final int gameChunksX = MIN_WIDTH_CHUNKS * size;
    final int gameChunksZ = MIN_LENGTH_CHUNKS * size;

    while (!suitable) {
      final int worldsize = this.server.getMaxWorldSize();
      System.out.println(String.format("max world size: %d\n", worldsize));
      final int xBound = 100000;// server.getMaxWorldSize() - (gameChunksX*16);
      final int startX = world.rand.nextInt(xBound) * 16;
      final int zBound = 100000;// server.getMaxWorldSize() - (gameChunksZ*16);
      final int startZ = world.rand.nextInt(zBound) * 16;

      final BlockPos gameAreaFrom = new BlockPos(startX, 0, startZ);
      final BlockPos gameAreaTo = new BlockPos(startX + (gameChunksX * 16), 0,
              startZ + (gameChunksZ * 16));
      bounds = new Bounds(gameAreaFrom, gameAreaTo);

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

  private Integer getSize(GameOptions options) {
    final String sizeOption = options.getString("size").orElse("m");
    return this.mapSizeToInt(sizeOption);
  }

  public boolean isPlayerFrozen(String playerName) {
    return this.frozenPlayers.contains(playerName);
  }

  public void log(String msg) {
    this.server.sendMessage(toIText(msg));
  }

  private Integer mapSizeToInt(String sizeOption) {
    switch (sizeOption) {
    case "XS":
    case "xs":
      return SIZE_XS;
    case "S":
    case "s":
      return SIZE_S;
    case "L":
    case "l":
      return SIZE_L;
    case "XL":
    case "xl":
      return SIZE_XL;
    case "M":
    case "m":
    default:
      return SIZE_M;
    }
  }

  private void movePlayerToPosition(EntityPlayer player, final int x,
    final int z)
  {
    final BlockPos pos = WorldUtils.getSurfaceBlock(this.world, x, z, true);
    CtfNetworkHandler.INSTANCE.sendTo(new CanMovePlayerToPosition(pos),
            (EntityPlayerMP) player);
    player.setPosition(pos.getX(), pos.getY(), pos.getZ());
  };

  public Game newGame(EntityPlayer owner, String config)
    throws GameCreationException
  {
    this.checkOwnerPermissions(owner);
    this.checkOwnerLimit(owner);
    final String name = this.checkGameNameUnique(this.generateGameName());
    final GameOptions options = this.newGameOptions(config);

    final Bounds newGameBounds = this.getNewGameBounds(options);
    System.out.println(String.format("New game bounds: %s\n", newGameBounds));
    final Game game = new Game(this.world, name, owner, newGameBounds, options);

    MinecraftForge.TERRAIN_GEN_BUS.post(new NewGameEvent(game));
    if (options.getBoolean(GameOption.BONUS_CHEST).orElse(true)) {
      this.createBonusChest(game);
    }
    game.save();
    this.addGame(game);
    this.save();

    return game;
  }

  private GameOptions newGameOptions(String config)
    throws GameCreationException
  {
    return (config == null) ? GameOptions.getDefault()
            : GameConfigData.get(this.world, config)
                    .orElseThrow(this.gameConfigNotFound(config)).getOptions();
  }

  public void playerLeaveAllGames(String playerName) {
    this.allGamesWithPlayer(playerName)
            .forEach(g -> g.removePlayer(playerName));
  }

  public void playerLeaveAllGamesExcept(String playerName, String gameName) {
    this.allGamesWithPlayer(playerName).filter(g -> !gameName.equals(gameName))
            .forEach(g -> g.removePlayer(playerName));
  }

  public void playerPickedUpOpponentFlag(String playerName, ItemBase item,
    Game game, EntityPlayer player, CtfTeam team, final TeamColour flagColour)
  {
    this.broadcastToAllPlayers(game, format("Player %s has got %s team's flag!",
            player, team.getColour()));
    gem.schedule(() -> moveItemFromInventoryToPlayerHand(player, item));
    game.setPlayerHoldingFlag(flagColour, playerName);
  }

  public void playerPlacedOwnFlag(String player, Game game) {
    this.broadcastToAllPlayers(game,
            format("Player %s has placed his team's flag!", player));
  }

  public void playerRejoinGame(EntityPlayer player, Game game) {
    this.broadcastToAllPlayers(game,
            format("%s rejoined game", player.getName()));
    this.sendPlayerToRandomPointInOwnHalf(game, player);
    if (game.getOptions().getBoolean(GameOption.TOOL_UP_ON_RESPAWN)
            .orElse(true))
    {
      this.toolUpPlayer(player);
    }
    this.unFreezePlayerOut(player);
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
      final TeamColour colour = team.getColour();
      final BlockPos baseLocation = game.getBaseLocation(colour);
      final String name = player.getName();
      System.out.println(String.format("Sending %s to %s base at %s\n", name,
              colour, baseLocation));
      final int x = baseLocation.getX() + 2, z = baseLocation.getZ() + 2;

      this.movePlayerToPosition(player, x, z);
    });

  }

  public void sendPlayerToRandomPointInOwnHalf(Game game, EntityPlayer player) {
    final Optional<CtfTeam> t = game.getTeamForPlayer(player.getName());
    t.ifPresent(team -> {
      final Bounds gameBounds = game.getBounds();
      final int gameWidth = gameBounds.getWidth();
      final int gameDepth = gameBounds.getDepth();
      final boolean invertZ = ((game.getBaseLocation(TeamColour.RED)
              .getZ() < game.getBaseLocation(TeamColour.BLUE).getZ())
              && team.getColour() == TeamColour.BLUE);
      final int x = this.world.rand.nextInt(gameWidth);
      final int z = this.world.rand.nextInt(gameDepth / 2);
      final int zz = (invertZ) ? gameDepth - z : z;
      final BlockPos offset = new BlockPos(x, 0, zz);
      final BlockPos pos = WorldUtils.offset(gameBounds.getFrom(), offset);
      this.movePlayerToPosition(player, pos.getX(), pos.getZ());
      this.broadcastToTeamPlayers(game, team.getColour(),
              format("%s rejoined game at %s", player.getName(), pos));
    });
  }

  public void toolUpPlayer(EntityPlayer p) {
    // InventoryUtils.setPlayerInventory(p,
    // GameInventoryFactory.getDefault().getGameItems());
  }

  public void unFreezePlayerOut(EntityPlayer player) {
    this.frozenPlayers.remove(player.getName());
  }
}
