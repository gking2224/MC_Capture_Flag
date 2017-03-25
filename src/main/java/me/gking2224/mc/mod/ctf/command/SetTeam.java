package me.gking2224.mc.mod.ctf.command;

import static me.gking2224.mc.mod.ctf.util.StringUtils.toIText;

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

public class SetTeam extends CommandBase {

  @Override public boolean checkPermission(MinecraftServer server,
    ICommandSender sender)
  {
    return true;
  }

  @Override protected void doExecute(MinecraftServer server,
    ICommandSender sender, String[] args)
      throws CommandException
  {

    final String targetPlayerName = args[0];
    final EntityPlayer targetPlayer = server.getEntityWorld()
            .getPlayerEntityByName(targetPlayerName);
    if (targetPlayer == null) { throw new CommandException(
            "Player '%s' not found", targetPlayerName); }
    final String teamName = args[1];
    final TeamColour team = TeamColour.fromString(teamName).orElseThrow(
            () -> new CommandException("Team not known: ", teamName));

    final Entity e = sender.getCommandSenderEntity();
    if (e instanceof EntityPlayer) {
      final EntityPlayer senderEntity = (EntityPlayer) e;
      final String senderName = senderEntity.getName();
      final Game game = this.gm.getPlayerActiveGame(senderName)
              .orElseThrow(super.gameNotFoundForPlayerException(senderEntity));
      if (!senderName.equals(game.getOwner())) { throw new CommandException(
              "Only the game owner can change players' teams"); }
      game.getTeamForPlayer(targetPlayerName)
              .ifPresent(t -> t.removePlayer(targetPlayerName));
      game.addPlayerToTeam(targetPlayerName, team);
      sender.sendMessage(
              toIText("%s added to %s team", targetPlayer, teamName));
      targetPlayer.sendMessage(toIText("You are now on team %s", teamName));
    }
  }

  @Override public List<String> getAliases() {
    return Collections.singletonList("st");
  }

  @Override protected String[] getArgNames() {
    return new String[] {
        "name", "team"
    };
  }

  @Override protected boolean[] getMandatoryArgs() {
    return new boolean[] {
        true, true
    };
  }

  @Override public String getName() {
    return "set_team";
  }

  @Override public List<String> getTabCompletions(MinecraftServer server,
    ICommandSender sender, String[] args, BlockPos targetPos)
  {
    return null;
  }

  @Override public boolean isUsernameIndex(String[] args, int index) {
    return (index == 0);
  }

}
