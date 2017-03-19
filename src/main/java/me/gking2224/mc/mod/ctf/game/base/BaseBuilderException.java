package me.gking2224.mc.mod.ctf.game.base;

public class BaseBuilderException extends RuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public BaseBuilderException(Exception e) {
    super(e);
  }

  public BaseBuilderException(String msg) {
    super(msg);
  }

}
