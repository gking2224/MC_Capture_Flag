package me.gking2224.mc.mod.ctf.game.event;

import static java.lang.String.format;
import static me.gking2224.mc.mod.ctf.util.InventoryUtils.moveItemFromInventoryToPlayerHand;

import java.util.Optional;

import me.gking2224.mc.mod.ctf.game.CtfTeam;
import me.gking2224.mc.mod.ctf.game.CtfTeam.TeamColour;
import me.gking2224.mc.mod.ctf.game.Game;
import me.gking2224.mc.mod.ctf.game.GameManager;
import me.gking2224.mc.mod.ctf.game.GameWorldManager;
import me.gking2224.mc.mod.ctf.item.Flag;
import me.gking2224.mc.mod.ctf.item.ItemBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GameEventManager {

  private static GameEventManager instance = null;

  public final static GameEventManager get() {
    return instance;
  }

  public static void init(MinecraftServer server) {
    if (instance != null) { throw new IllegalStateException(); }
    instance = new GameEventManager(server);
  }

  @SuppressWarnings("unused") private final MinecraftServer server;
  @SuppressWarnings("unused") private final World world;

  private GameEventManager(MinecraftServer server) {
    this.server = server;
    this.world = server.getEntityWorld();
  }

  private void broadcastTeamCapturedFlag(Game game, String player,
    TeamColour teamColour)
  {
    GameManager.get().broadcastToAllPlayers(game,
            format("Player %s has got %s team's flag!", player, teamColour));
  }

  public void flagPlaced(String player, ItemBase flag, BlockPos blockPos) {
    final Optional<Game> g = GameManager.get().getPlayerActiveGame(player);
    g.ifPresent((game) -> {
      final TeamColour flagColour = Flag.getFlagColour(flag);
      game.setFlagBlockPosition(flagColour, blockPos);
      System.out.printf("%s: %s flag position updated as %s\n",
              Thread.currentThread().getName(), flagColour, blockPos);
      final Optional<CtfTeam> t = game.getTeamForPlayer(player);
      t.ifPresent(team -> {
        if (!Flag.isOwnTeamFlag(flag, team)) {
          if (GameWorldManager.get().isInHomeBase(game, team.getColour(),
                  blockPos))
          {
            GameManager.get().gameRoundWon(game, player, team, flagColour);
          }
        } else {
          GameManager.get().broadcastToAllPlayers(game,
                  format("Player %s has placed his team's flag!", player));
        }
      });
    });
  }

  public void playerDied(Entity entity) {
    // GameManager.get().broadcastToAllPlayers(game, format("Player %s has
    // placed his team's flag!", player));
  }

  public void playerPickedUpFlag(String playerName, ItemBase item) {
    final Optional<Game> g = GameManager.get().getPlayerActiveGame(playerName);
    g.ifPresent((game) -> {
      final Optional<EntityPlayer> p = GameManager.get()
              .getPlayerByName(playerName);
      p.ifPresent(player -> {
        final Optional<CtfTeam> t = game.getTeamForPlayer(playerName);
        t.ifPresent((team) -> {
          final TeamColour flagColour = Flag.getFlagColour(item);
          if (flagColour != team.getColour()) {
            this.broadcastTeamCapturedFlag(game, playerName, flagColour);
            this.schedule(() -> moveItemFromInventoryToPlayerHand(player, item),
                    0);
            game.setPlayerHoldingFlag(flagColour, playerName);
          } else {
            GameManager.get().broadcastToAllPlayers(game, format(
                    "Player %s tried to pick up his own flag!", playerName));
            GameWorldManager.get().resetFlag(game, flagColour);
          }
        });
      });
    });
  }

  public void schedule(Runnable r) {
    this.schedule(r, -1);
  }

  public void schedule(Runnable r, int delay) {
    if (delay != -1) {
      try {
        Thread.sleep(delay);
      } catch (final Exception e) {}
    }
    new Thread(r).start();
  }
}
