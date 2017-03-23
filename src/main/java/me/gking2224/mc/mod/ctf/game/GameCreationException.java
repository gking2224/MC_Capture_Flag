package me.gking2224.mc.mod.ctf.game;

import static java.lang.String.format;

public class GameCreationException extends Exception {

  /**
   *
   */
  private static final long serialVersionUID = 3386438629678970502L;

  public GameCreationException(String msg) {
    super(msg);
  }

  public GameCreationException(String msg, Object... args) {
    this(format(msg, args));
  }

}
