package me.gking2224.mc.mod.ctf.command;

import java.util.Collections;
import java.util.List;

import me.gking2224.mc.mod.ctf.game.CtfTeam.TeamColour;
import me.gking2224.mc.mod.ctf.game.Game;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class SetScore extends CommandBase {

  @Override
  public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
    return true;
  }

  @Override
  protected void doExecute(MinecraftServer server, ICommandSender sender,
      String[] args) throws CommandException {

    final String teamName = args[0];
    final String newScore = args[1];

    final Entity e = sender.getCommandSenderEntity();
    if (e instanceof EntityPlayer) {
      final TeamColour team = TeamColour.fromString(teamName).orElseThrow(
          () -> new CommandException("Team not known: ", teamName));
      final EntityPlayer senderEntity = (EntityPlayer) e;
      final String senderName = senderEntity.getName();
      final Game game = this.gm.getPlayerActiveGame(senderName).orElseThrow(
          super.gameNotFoundForPlayerException(senderEntity));
      if (!senderName.equals(game.getOwner())) {
        throw new CommandException("Only the game owner can change scores");
      }
      String oldScore = game.getFormattedScore();

      game.setScore(team, getScore(newScore));
      this.gm.broadcastToTeamPlayers(game, team, "Score updated from:");
      this.gm.broadcastToTeamPlayers(game, team, "  %s", oldScore);
      this.gm.broadcastToTeamPlayers(game, team, "to:");
      this.gm.broadcastToTeamPlayers(game, team, "  %s",
          game.getFormattedScore());
    }
  }

  private Integer getScore(String newScore) throws CommandException {
    try {
      return Integer.parseInt(newScore);
    } catch (NumberFormatException e) {
      throw new CommandException("Could not parse score '%s'", newScore);
    }
  }

  @Override
  public List<String> getAliases() {
    return Collections.singletonList("ss");
  }

  @Override
  protected String[] getArgNames() {
    return new String[] { "team", "score" };
  }

  @Override
  protected boolean[] getMandatoryArgs() {
    return new boolean[] { true, true };
  }

  @Override
  public String getName() {
    return "set_score";
  }

  @Override
  public List<String> getTabCompletions(MinecraftServer server,
      ICommandSender sender, String[] args, BlockPos targetPos) {
    return null;
  }

  @Override
  public boolean isUsernameIndex(String[] args, int index) {
    return (index == 0);
  }

}
