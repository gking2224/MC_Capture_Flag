package me.gking2224.mc.mod.ctf.command;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import me.gking2224.mc.mod.ctf.game.CtfTeam;
import me.gking2224.mc.mod.ctf.game.Game;
import me.gking2224.mc.mod.ctf.game.GameManager;
import me.gking2224.mc.mod.ctf.util.StringUtils;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class JoinCtfGame extends CommandBase {
  private final List<String> aliases;

  protected String fullEntityName;
  protected Entity conjuredEntity;

  public JoinCtfGame() {

    aliases = new ArrayList<String>();
    aliases.add("jg");
  }

  @Override public int compareTo(ICommand o) {
    return 0;
  }

  @Override public String getName() {
    return "join_ctf_game";
  }

  @Override public List<String> getAliases() {
    return this.aliases;
  }

  @Override protected void doExecute(MinecraftServer server,
    ICommandSender sender, String[] args)
      throws CommandException
  {

    Entity e = sender.getCommandSenderEntity();
    String gameArg = (args.length != 0) ? args[0] : null;

    if (e == null) return;
    if (e instanceof EntityPlayer) {
      EntityPlayer player = (EntityPlayer) e;
      Game game = getGame(player, gameArg);
      String gameName = game.getName();

      String playerName = player.getName();
      GameManager gameManager = GameManager.get();
      gameManager.playerLeaveAllGames(playerName);
      Optional<CtfTeam> t = game.getTeamForPlayer(playerName);
      t.ifPresent((team) -> {
        sender.sendMessage(StringUtils.toIText(format(
                "Already in game %s on team %s", gameName, team.getColour())));
        // gameManager.sendPlayerToBase(game, player);
      });
      if (!t.isPresent()) {
        CtfTeam team = game.addPlayer(playerName);
        gameManager.save();
        sender.sendMessage(StringUtils.toIText(format(
                "Joined game %s on team %s", gameName, team.getColour())));
        gameManager.sendPlayerToBase(game, player);
      }
    }
  }

  @Override public boolean checkPermission(MinecraftServer server,
    ICommandSender sender)
  {
    return true;
  }

  @Override public List<String> getTabCompletions(MinecraftServer server,
    ICommandSender sender, String[] args, BlockPos targetPos)
  {
    return null;
  }

  @Override public boolean isUsernameIndex(String[] args, int index) {
    return false;
  }

  @Override protected String[] getArgNames() {
    return new String[] {
        "name"
    };
  }

  @Override protected boolean[] getMandatoryArgs() {
    return new boolean[] {
        true
    };
  }
}
