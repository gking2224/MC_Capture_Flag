package me.gking2224.mc.mod.ctf.command;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import me.gking2224.mc.mod.ctf.game.CtfTeam;
import me.gking2224.mc.mod.ctf.game.CtfTeam.TeamColour;
import me.gking2224.mc.mod.ctf.game.Game;
import me.gking2224.mc.mod.ctf.game.GameManager;
import me.gking2224.mc.mod.ctf.util.StringUtils;
import me.gking2224.mc.mod.ctf.util.WorldUtils;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class BaseDirections extends CommandBase {

  public enum Direction {

    NORTH("north", 0), SOUTH("south", 180), EAST("east", 90), WEST("west",
            270), NORTH_WEST("north-west", 315), NORTH_EAST("north-east",
                    45), SOUTH_EAST("south-east", 135), SOUTH_WEST("south-west",
                            215);

    public static Set<Direction> all() {
      return new HashSet<Direction>(Arrays.asList(NORTH, SOUTH, EAST, WEST,
              NORTH_EAST, NORTH_WEST, SOUTH_EAST, SOUTH_WEST));
    }

    private final int heading;
    private final String name;

    Direction(String name, int heading) {
      this.heading = heading;
      this.name = name;
    }

    @Override public String toString() {
      return this.name;
    }

  }

  private static final double HEADING_MIDPOINT = 45 / 2;

  private String directionsToBaseRelativeToPlayer(TeamColour team,
    BlockPos basePosition, EntityPlayer player)
  {
    final BlockPos playerPosition = player.getPosition();
    System.out.println("player position: " + playerPosition);
    final BlockPos delta = WorldUtils.getDelta(basePosition, playerPosition,
            false);

    System.out.println(String.format("%s delta: %s", team, delta));

    final int x = delta.getX();
    final int z = delta.getZ();
    final double oppOverAdj = (z == 0) ? 0 : (double) x / (double) z;
    final double atan = Math.atan(oppOverAdj);
    double a = atan * 180 / Math.PI;
    System.out.println("angle: " + a);
    if (x > 0) {
      a += 180;
      if (z > 0) {
        a += 90;
      }
    } else if (z < 0) {
      a += 90;
    }
    System.out.println("adjusted angle: " + a);

    final double distance = Math.sqrt(Math.pow(x, 2) + Math.pow(z, 2));
    final Direction d = this.getDirectionFromAngle((int) a);

    return String.format("%s base: %d(%s) %d", team, a, d, (int) distance);

  }

  @Override protected void doExecute(MinecraftServer server,
    ICommandSender sender, String[] args)
      throws CommandException
  {

    final EntityPlayer player = (EntityPlayer) sender.getCommandSenderEntity();
    final Game g = GameManager.get().getPlayerActiveGame(player.getName())
            .orElseThrow(super.gameNotFoundForPlayerException(player));

    // TeamColour.all().forEach(colour -> {
    final CtfTeam team = g.getTeamForPlayer(player.getName())
            .orElseThrow(super.playerNotOnTeamException(player, g));
    final TeamColour colour = team.getColour();
    final BlockPos pos = g.getBaseLocation(colour);

    sender.sendMessage(StringUtils.toIText(
            this.directionsToBaseRelativeToPlayer(colour, pos, player)));
    // });

  }

  @Override protected String[] getArgNames() {
    return new String[] {
        "base"
    };
  }

  private Direction getDirectionFromAngle(int a) {
    // final int aa = (a < 0) ? a + 360 : a;
    return Direction.all().stream().filter(d -> {
      final int heading = d.heading;
      final int deltaFromHeading = Math.abs(a - heading);
      return deltaFromHeading <= HEADING_MIDPOINT;
    }).findFirst().orElseThrow(() -> new IllegalStateException(
            "Could not find direction for angle " + a));
  }

  @Override protected boolean[] getMandatoryArgs() {
    return new boolean[] {
        false
    };
  }

  @Override public String getName() {
    return "base_directions";
  }

}
