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

public class CurrentGame extends CommandBase {
  private static final String[] NO_ARGS = new String[0];
  private static final boolean[] NO_ARGS_MANDATORY = new boolean[0];

  private final List<String> aliases;

  protected String fullEntityName;
  protected Entity conjuredEntity;

  public CurrentGame() {

    aliases = new ArrayList<String>();
    aliases.add("cg");
  }

  @Override public int compareTo(ICommand o) {
    return 0;
  }

  @Override public String getName() {
    return "current_game";
  }

  @Override public List<String> getAliases() {
    return this.aliases;
  }

  @Override protected void doExecute(MinecraftServer server,
    ICommandSender sender, String[] args)
      throws CommandException
  {
    Entity e = sender.getCommandSenderEntity();

    if (e == null) return;
    if (e instanceof EntityPlayer) {
      EntityPlayer player = (EntityPlayer) e;
      String playerName = player.getName();
      Optional<Game> g = GameManager.get().getPlayerActiveGame(playerName);
      g.orElseThrow(() -> new CommandException(
              "You are not currently in a game. Use /new_ctf_game or /join_ctf_game"));
      g.ifPresent((game) -> {
        Optional<CtfTeam> t = game.getTeamForPlayer(playerName);
        t.ifPresent(team -> sender.sendMessage(StringUtils
                .toIText(format("You are currently in game %s, in team %s",
                        game.getName(), team.getColour()))));
      });
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
    return NO_ARGS;
  }

  @Override protected boolean[] getMandatoryArgs() {
    return NO_ARGS_MANDATORY;
  }

}
