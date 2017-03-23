package me.gking2224.mc.mod.ctf.game.event;

import me.gking2224.mc.mod.ctf.game.Game;
import net.minecraft.world.World;

public class NewGameEvent extends EventBase {

  private final Game game;

  public NewGameEvent(World world, Game game) {
    super(world);
    this.game = game;
  }

  public Game getGame() {
    return this.game;
  }

}
