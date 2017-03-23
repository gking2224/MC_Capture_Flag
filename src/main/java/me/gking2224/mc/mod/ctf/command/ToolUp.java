package me.gking2224.mc.mod.ctf.command;

import java.util.ArrayList;
import java.util.List;

import me.gking2224.mc.mod.ctf.game.Game;
import me.gking2224.mc.mod.ctf.game.GameInventoryFactory;
import me.gking2224.mc.mod.ctf.game.GameManager;
import me.gking2224.mc.mod.ctf.game.GameOption;
import me.gking2224.mc.mod.ctf.util.InventoryUtils;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class ToolUp extends CommandBase {
  private final List<String> aliases;

  protected String fullEntityName;
  protected Entity conjuredEntity;

  public ToolUp() {

    this.aliases = new ArrayList<String>();
    this.aliases.add("tu");
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

    if (e == null) { return; }
    if (e instanceof EntityPlayer) {
      final EntityPlayer player = (EntityPlayer) e;
      final Game g = GameManager.get().getPlayerActiveGame(e.getName())
              .orElseThrow(this.gameNotFoundForPlayerException(player));

      final String inv = g.getOptions().getString(GameOption.INVENTORY)
              .orElse("default");
      InventoryUtils.addItemsToPlayerInventory(player,
              GameInventoryFactory.get(inv).getGameItems());
    }
  }

  @Override public List<String> getAliases() {
    return this.aliases;
  }

  @Override protected String[] getArgNames() {
    return new String[0];
  }

  @Override protected boolean[] getMandatoryArgs() {
    return new boolean[0];
  }

  @Override public String getName() {
    return "ctf:tool_up";
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
