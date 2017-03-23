package me.gking2224.mc.mod.ctf.command;

import static me.gking2224.mc.mod.ctf.util.StringUtils.toIText;

import java.util.Collections;
import java.util.List;

import me.gking2224.mc.mod.ctf.game.data.GameConfigData;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class GetGameConfiguration extends CommandBase {

  @Override public boolean checkPermission(MinecraftServer server,
    ICommandSender sender)
  {
    return true;
  }

  @Override protected void doExecute(MinecraftServer server,
    ICommandSender sender, String[] args)
      throws CommandException
  {
    final String name = args[0];
    final GameConfigData config = GameConfigData
            .get(server.getEntityWorld(), name).orElseThrow(
                    () -> new CommandException("No such game configuration: %s",
                            name));
    sender.sendMessage(
            toIText("Configuration '%s': %s", name, config.toString()));

  }

  @Override public List<String> getAliases() {
    return Collections.singletonList("ggc");
  }

  @Override protected String[] getArgNames() {
    return new String[] {
        "name", "options",
    };
  }

  @Override protected boolean[] getMandatoryArgs() {
    return new boolean[] {
        true, true
    };
  }

  @Override public String getName() {
    return "get_config";
  }

}
