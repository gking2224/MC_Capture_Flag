package me.gking2224.mc.mod.ctf.game.event;

import me.gking2224.mc.mod.ctf.game.Game;
import net.minecraft.world.World;

public class GameResetEvent extends EventBase {

  private final Game game;

  public GameResetEvent(World world, Game game) {
    super(world);
    this.game = game;
  }

  public Game getGame() {
    return this.game;
  }

}
