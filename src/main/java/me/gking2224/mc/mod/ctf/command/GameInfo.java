package me.gking2224.mc.mod.ctf.command;

import static me.gking2224.mc.mod.ctf.util.StringUtils.toIText;

import java.util.ArrayList;
import java.util.List;

import me.gking2224.mc.mod.ctf.game.Game;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class GameInfo extends CommandBase {
  private final List<String> aliases;

  public GameInfo() {

    this.aliases = new ArrayList<String>();
    this.aliases.add("gi");
  }

  @Override public boolean checkPermission(MinecraftServer server,
    ICommandSender sender)
  {
    return true;
  }

  @Override public int compareTo(ICommand o) {
    return 0;
  }

  @Override protected void doExecute(MinecraftServer server,
    ICommandSender sender, String[] args)
      throws CommandException
  {

    final Entity e = sender.getCommandSenderEntity();
    final String gameArg = (args.length != 0) ? args[0] : null;

    if (e == null) { return; }
    if (e instanceof EntityPlayer) {
      final EntityPlayer player = (EntityPlayer) e;
      final Game game = this.getGame(player, gameArg);
      sender.sendMessage(toIText(game.toString()));
    }
  }

  @Override public List<String> getAliases() {
    return this.aliases;
  }

  @Override protected String[] getArgNames() {
    return new String[] {
        "game"
    };
  }

  @Override protected boolean[] getMandatoryArgs() {
    return new boolean[] {
        true
    };
  }

  @Override public String getName() {
    return "ctf:game_info";
  }

  @Override public List<String> getTabCompletions(MinecraftServer server,
    ICommandSender sender, String[] args, BlockPos targetPos)
  {
    return null;
  }

  @Override public boolean isUsernameIndex(String[] args, int index) {
    return false;
  }
}
