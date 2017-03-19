package me.gking2224.mc.mod.ctf.command;

import static me.gking2224.mc.mod.ctf.util.StringUtils.toIText;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import me.gking2224.mc.mod.ctf.game.Game;
import me.gking2224.mc.mod.ctf.game.GameInventoryFactory;
import me.gking2224.mc.mod.ctf.game.GameManager;
import me.gking2224.mc.mod.ctf.util.InventoryUtils;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class GetScore extends CommandBase {
  private final List<String> aliases;

  protected String fullEntityName;
  protected Entity conjuredEntity;

  public GetScore() {

    aliases = new ArrayList<String>();
    aliases.add("gs");
  }

  @Override public int compareTo(ICommand o) {
    return 0;
  }

  @Override public String getName() {
    return "ctf:get_score";
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

      sender.sendMessage(toIText(g.get().getFormattedScore()));
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
