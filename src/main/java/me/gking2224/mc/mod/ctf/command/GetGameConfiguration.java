package me.gking2224.mc.mod.ctf.command;

import static me.gking2224.mc.mod.ctf.util.StringUtils.toIText;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import me.gking2224.mc.mod.ctf.game.data.GameConfigData;
import me.gking2224.mc.mod.ctf.util.StringUtils;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import com.google.common.base.Strings;

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
    final String name = (args.length > 0) ? args[0] : null;
    if (!Strings.isNullOrEmpty(name)) {
      final GameConfigData config = GameConfigData
              .get(server.getEntityWorld(), name).orElseThrow(
                      () -> new CommandException("No such game configuration: %s",
                              name));
      sender.sendMessage(
              toIText("Configuration '%s': %s", name, config.toString()));
    }
    else {
      Set<String> configs = this.gm.getAllAvailableConfigNames();
      sender.sendMessage(
          toIText(configs.stream().collect(stringConcat())));
    }

  }

  private Collector<String, String, String> stringConcat() {
    Collector<String, String, String> n = new Collector<String, String, String>() {

      @Override
      public Supplier<String> supplier() {
        return () -> "";
      }

      @Override
      public BiConsumer<String, String> accumulator() {
        return (s1, s2) -> {};
      }

      @Override
      public BinaryOperator<String> combiner() {
        return (s1, s2) -> s1 + ", "+s2;
      }

      @Override
      public Function<String, String> finisher() {
        return s -> Strings.nullToEmpty(s).startsWith(", ") ? s.substring(2) : s;
      }

      @Override
      public Set<java.util.stream.Collector.Characteristics> characteristics() {
        // TODO Auto-generated method stub
        return null;
      }
      
    };
    return n;
  }

  @Override public List<String> getAliases() {
    return Collections.singletonList("gc");
  }

  @Override protected String[] getArgNames() {
    return new String[] {
        "name"
    };
  }

  @Override protected boolean[] getMandatoryArgs() {
    return new boolean[] {
        false  
    };
  }

  @Override public String getName() {
    return "get_config";
  }

}
