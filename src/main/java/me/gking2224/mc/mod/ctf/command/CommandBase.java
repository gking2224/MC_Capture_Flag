package me.gking2224.mc.mod.ctf.command;

import java.util.Optional;
import java.util.function.Supplier;

import me.gking2224.mc.mod.ctf.game.Game;
import me.gking2224.mc.mod.ctf.game.GameManager;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public abstract class CommandBase extends net.minecraft.command.CommandBase {

  protected GameManager gm;
  
  public CommandBase() {
	this.gm = GameManager.get();
  }

  protected abstract void doExecute(MinecraftServer server,
    ICommandSender sender, String[] args)
      throws CommandException;

  @Override public final void execute(MinecraftServer server,
    ICommandSender sender, String[] args)
      throws CommandException
  {
    this.validateArgs(server, sender, args);
    this.doExecute(server, sender, args);
  }

  public Supplier<CommandException> gameNotFoundForPlayerException(
    EntityPlayer player)
  {
    return () -> new CommandException("No game found for player %s",
            player.getName());
  }

  protected String[] getArgNames() {
    return new String[0];
  }

  private String getArgUsageString() {
    final StringBuilder sb = new StringBuilder();
    this.getArgUsageString(sb, 0);
    return sb.toString();
  }

  private void getArgUsageString(StringBuilder sb, int index) {
    if (this.getArgNames().length <= index) { return; }
    final String argName = this.getArgNames()[index];
    final boolean mandatory = this.getMandatoryArgs()[index];
    sb.append(String.format(" <%s>%s", argName, mandatory ? "" : "?"));
    this.getArgUsageString(sb, ++index);
  }

  protected Game getGame(EntityPlayer player, String gameArg)
    throws CommandException
  {
    final GameManager gameManager = GameManager.get();
    Game game = null;
    if (gameArg != null) {
      final Optional<Game> g = gameManager.getGame(gameArg);
      game = g.orElseThrow(
              () -> new CommandException("Game %s not found", gameArg));
    } else {
      final Optional<Game> g = gameManager
              .getPlayerActiveGame(player.getName());
      game = g.orElseThrow(() -> new CommandException(
              "No active game and no game specified"));
    }
    return game;
  }

  protected boolean[] getMandatoryArgs() {
    return new boolean[0];
  }

  @Override public final String getUsage(ICommandSender sender) {
    return this.getName() + this.getArgUsageString();
  }

  public Supplier<CommandException> playerNotOnTeamException(
    EntityPlayer player, Game game)
  {
	  return playerNotOnTeamException(player.getName(), game);
  }

  protected void validateArgs(MinecraftServer server, ICommandSender sender,
    String[] args)
      throws CommandException
  {

  }

	public Supplier<CommandException> playerNotOnTeamException(String playerName, Game game) {
	    return () -> new CommandException("No team found for player %s in game %s",
	    		playerName, game.getName());
	}
}
