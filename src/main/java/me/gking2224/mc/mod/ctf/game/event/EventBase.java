package me.gking2224.mc.mod.ctf.game.event;

import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;

public abstract class EventBase extends Event {

  private final World world;

  public EventBase(World world) {
    this.world = world;
  }

  public World getWorld() {
    return this.world;
  }
}
