package me.gking2224.mc.mod.ctf.command;

import java.util.Optional;

import me.gking2224.mc.mod.ctf.game.Game;
import me.gking2224.mc.mod.ctf.game.GameManager;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public abstract class CommandBase extends net.minecraft.command.CommandBase {

  protected abstract String[] getArgNames();

  protected abstract boolean[] getMandatoryArgs();

  @Override public final String getUsage(ICommandSender sender) {
    return getName() + getArgUsageString();
  }

  private String getArgUsageString() {
    StringBuilder sb = new StringBuilder();
    getArgUsageString(sb, 0);
    return sb.toString();
  }

  private void getArgUsageString(StringBuilder sb, int index) {
    if (getArgNames().length <= index) return;
    String argName = getArgNames()[index];
    boolean mandatory = getMandatoryArgs()[index];
    sb.append(String.format(" <%s>%s", argName, mandatory ? "" : "?"));
    getArgUsageString(sb, ++index);
  }

  @Override public final void execute(MinecraftServer server,
    ICommandSender sender, String[] args)
      throws CommandException
  {
    validateArgs(server, sender, args);
    doExecute(server, sender, args);
  }

  protected abstract void doExecute(MinecraftServer server,
    ICommandSender sender, String[] args)
      throws CommandException;

  protected void validateArgs(MinecraftServer server, ICommandSender sender,
    String[] args)
      throws CommandException
  {

  }

  protected Game getGame(EntityPlayer player, String gameArg)
    throws CommandException
  {
    GameManager gameManager = GameManager.get();
    Game game = null;
    if (gameArg != null) {
      Optional<Game> g = gameManager.getGame(gameArg);
      game = g.orElseThrow(
              () -> new CommandException("Game %s not found", gameArg));
    } else {
      Optional<Game> g = gameManager.getPlayerActiveGame(player.getName());
      game = g.orElseThrow(() -> new CommandException(
              "No active game and no game specified"));
    }
    return game;
  }
}
