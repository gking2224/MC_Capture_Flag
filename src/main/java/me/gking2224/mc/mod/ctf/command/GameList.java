package me.gking2224.mc.mod.ctf.command;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import me.gking2224.mc.mod.ctf.game.GameManager;
import me.gking2224.mc.mod.ctf.util.StringUtils;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class GameList extends CommandBase {
  private final List<String> aliases;

  public GameList() {

    aliases = new ArrayList<String>();
    aliases.add("gl");
  }

  @Override public int compareTo(ICommand o) {
    return 0;
  }

  @Override public String getName() {
    return "ctf:game_list";
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
      Set<String> games = GameManager.get().getAllGames();
      sender.sendMessage(StringUtils.toIText(format("Games: %s", games)));
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

  @Override protected boolean[] getMandatoryArgs() {
    return new boolean[0];
  }

  @Override protected String[] getArgNames() {
    return new String[0];
  }
}
