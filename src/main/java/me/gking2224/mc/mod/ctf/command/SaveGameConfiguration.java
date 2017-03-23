package me.gking2224.mc.mod.ctf.command;

import static me.gking2224.mc.mod.ctf.util.StringUtils.toIText;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import me.gking2224.mc.mod.ctf.game.GameOptions;
import me.gking2224.mc.mod.ctf.game.data.GameConfigData;
import me.gking2224.mc.mod.ctf.util.StringUtils;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class SaveGameConfiguration extends CommandBase {

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
    final String optionsString = args[1];
    GameOptions gameOptions = null;
    try {
      gameOptions = new GameOptions(optionsString);
    } catch (final Exception e) {
      throw new CommandException("Could not parse game options: %s",
              optionsString);
    }
    final Optional<GameConfigData> existing = GameConfigData
            .get(server.getEntityWorld(), name);
    if (existing.isPresent()) {
      existing.get().update(gameOptions);
      sender.sendMessage(toIText("Updated config '%s'", name));
    } else {
      GameConfigData.create(server.getEntityWorld(), name, gameOptions);
      sender.sendMessage(StringUtils.toIText("Created config '%s'", name));
    }

  }

  @Override public List<String> getAliases() {
    return Collections.singletonList("sc");
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
    return "save_config";
  }

}
