package me.gking2224.mc.mod.ctf.game.event;

import static java.lang.String.format;
import static me.gking2224.mc.mod.ctf.util.StringUtils.toIText;

import java.util.Optional;

import me.gking2224.mc.mod.ctf.game.CtfTeam;
import me.gking2224.mc.mod.ctf.game.CtfTeam.TeamColour;
import me.gking2224.mc.mod.ctf.game.Game;
import me.gking2224.mc.mod.ctf.game.GameManager;
import me.gking2224.mc.mod.ctf.game.GameOption;
import me.gking2224.mc.mod.ctf.game.GameWorldManager;
import me.gking2224.mc.mod.ctf.item.Flag;
import me.gking2224.mc.mod.ctf.item.ItemBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GameEventManager {

  private static GameEventManager instance = null;

  public final static GameEventManager get() {
    return instance;
  }

  public static GameEventManager init(MinecraftServer server, GameManager gm) {
    if (instance != null) { throw new IllegalStateException(); }
    instance = new GameEventManager(server, gm);
    return instance;
  }

  @SuppressWarnings("unused") private final MinecraftServer server;
  @SuppressWarnings("unused") private final World world;
  private final GameManager gm;

  private GameEventManager(MinecraftServer server, GameManager gm) {
    this.server = server;
    this.world = server.getEntityWorld();
    this.gm = gm;
  }

  public void flagPlaced(String player, ItemBase flag, BlockPos blockPos) {
    final Optional<Game> g = this.gm.getPlayerActiveGame(player);
    g.ifPresent((game) -> {
      final TeamColour flagColour = Flag.getFlagColour(flag);
      game.updateFlagBlockPosition(flagColour, blockPos);
      System.out.printf("%s: %s flag position updated as %s\n",
              Thread.currentThread().getName(), flagColour, blockPos);
      final Optional<CtfTeam> t = game.getTeamForPlayer(player);
      t.ifPresent(team -> {
        if (!Flag.isOwnTeamFlag(flag, team)) {
          if (GameWorldManager.get().isInHomeBase(game, team.getColour(),
                  blockPos))
          {
            this.gm.gameRoundWon(game, player, team, flagColour);
          }
        } else {
          this.gm.playerPlacedOwnFlag(player, game);
        }
      });
    });
  }

  public void playerPickedUpFlag(String playerName, ItemBase item) {
    final Optional<Game> g = this.gm.getPlayerActiveGame(playerName);
    g.ifPresent((game) -> {
      final Optional<EntityPlayer> p = this.gm.getPlayerByName(playerName);
      p.ifPresent(player -> {
        final Optional<CtfTeam> t = game.getTeamForPlayer(playerName);
        t.ifPresent((team) -> {
          final TeamColour flagColour = Flag.getFlagColour(item);
          if (flagColour != team.getColour()) {
            this.gm.playerPickedUpOpponentFlag(playerName, item, game, player,
                    team, flagColour);
          } else {
            this.gm.broadcastToAllPlayers(game, format(
                    "Player %s tried to pick up his own flag!", playerName));
            GameWorldManager.get().resetFlag(game, flagColour);
          }
        });
      });
    });
  }

  public void playerRequestedJoinGame(EntityPlayer player,
    ICommandSender sender, Game game)
  {

    final String playerName = player.getName();
    this.gm.playerLeaveAllGamesExcept(playerName, game.getName());
    final Optional<CtfTeam> t = game.getTeamForPlayer(playerName);
    final String gameName = game.getName();
    t.ifPresent((team) -> {
      sender.sendMessage(toIText(format("Already in game %s on team %s",
              gameName, team.getColour())));
    });
    if (!t.isPresent()) {
      if (this.gm.isPlayerFrozen(playerName)) {
        sender.sendMessage(toIText("You can't rejoin the game yet"));
      } else {
        final CtfTeam team = game.addPlayer(playerName);
        sender.sendMessage(toIText(format("Joined game %s on team %s", gameName,
                team.getColour())));
        this.gm.sendPlayerToBase(game, player);
      }
    }
  }

  public void playerRespawned(EntityPlayer player, Game game) {
    final String playerName = player.getName();
    final Optional<CtfTeam> t = game.getTeamForPlayer(playerName);
    t.ifPresent(team -> {
      final int respawnDelay = game.getOptions()
              .getInteger(GameOption.RESPAWN_DELAY).orElse(10);
      this.gm.freezePlayerOut(player);
      this.gm.broadCastMessageToPlayer(playerName,
              toIText(format("Going back to %s base in 10 seconds",
                      team.getColour().toString())));
      this.schedule(() -> {
        this.gm.playerRejoinGame(player, game);
      } , respawnDelay);
    });
  }

  public void schedule(Runnable r) {
    this.schedule(r, -1);
  }

  public void schedule(Runnable r, int delaySecs) {
    new Thread(() -> {
      if (delaySecs != -1) {
        try {
          Thread.sleep(delaySecs * 1000);
        } catch (final Exception e) {
          e.printStackTrace();
        }
      }
      r.run();
    }).start();
  }
}
