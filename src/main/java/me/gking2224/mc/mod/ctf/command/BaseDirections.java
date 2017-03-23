package me.gking2224.mc.mod.ctf.command;

import static java.lang.String.format;
import static me.gking2224.mc.mod.ctf.game.CtfTeam.TeamColour.BLUE;
import static me.gking2224.mc.mod.ctf.game.CtfTeam.TeamColour.RED;
import static me.gking2224.mc.mod.ctf.game.GameOption.OPPONENT_BASE_DIRECTIONS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import me.gking2224.mc.mod.ctf.game.CtfTeam.TeamColour;
import me.gking2224.mc.mod.ctf.game.Game;
import me.gking2224.mc.mod.ctf.game.GameManager;
import me.gking2224.mc.mod.ctf.util.StringUtils;
import me.gking2224.mc.mod.ctf.util.WorldUtils;
import me.gking2224.mc.mod.ctf.util.WorldUtils.DistanceAndHeading;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class BaseDirections extends CommandBase {

  /**
   * Check if the given ICommandSender has permission to execute this command
   * 
   * @param server
   *          The server instance
   * @param sender
   *          The ICommandSender to check permissions on
   */
  @Override public boolean checkPermission(MinecraftServer server,
    ICommandSender sender)
  {
    return true;
  }

  @Override protected void doExecute(MinecraftServer server,
    ICommandSender sender, String[] args)
      throws CommandException
  {

    final EntityPlayer player = (EntityPlayer) sender.getCommandSenderEntity();
    final String name = player.getName();
    final Game g = GameManager.get().getPlayerActiveGame(name)
            .orElseThrow(this.gameNotFoundForPlayerException(player));

    final List<TeamColour> colours = new ArrayList<TeamColour>();
    final TeamColour playerTeam = g.getTeamForPlayer(name)
            .orElseThrow(this.playerNotOnTeamException(player, g)).getColour();

    colours.add(playerTeam);
    if (g.getOptions().getBoolean(OPPONENT_BASE_DIRECTIONS).orElse(true)) {
      colours.add(playerTeam == RED ? BLUE : RED);
    }
    colours.forEach(colour -> {
      final BlockPos pos = g.getBaseLocation(colour);

      final int nearLimit = g.getOptions().getInteger("near").orElse(40);
      final Random rand = server.getEntityWorld().rand;
      final int randomDistance = rand.nextInt(4) - 2;
      sender.sendMessage(
              StringUtils.toIText(this.getDirectionsToBaseRelativeToPlayer(
                      colour, pos, player, nearLimit, randomDistance)));
    });

  }

  @Override public List<String> getAliases() {
    return Collections.singletonList("bd");
  }

  private String getDirectionsToBaseRelativeToPlayer(TeamColour team,
    BlockPos basePosition, EntityPlayer player, int nearLimit, int random)
  {
    final BlockPos playerPosition = player.getPosition();

    final DistanceAndHeading toFlag = WorldUtils
            .getDistanceAndHeading(playerPosition, basePosition);
    String details = null;
    if (toFlag.getDistance() < nearLimit) {
      details = "near by!";
    } else {

      final String distanceStr = this.getDistanceToDisplay(toFlag.getDistance(),
              random);

      details = String.format("%s %s", toFlag.getDirection(), distanceStr);
    }
    return String.format("%s base: %s", team, details);
  }

  private String getDistanceToDisplay(double distanceInBlocks, int random) {
    final int chunks = (int) Math.floor(distanceInBlocks / 16);
    final int rounded = (chunks / 2) * 2;
    return format("approx. %s chunks", rounded + random);
  }

  @Override public String getName() {
    return "base_directions";
  }
}
