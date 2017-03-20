package me.gking2224.mc.mod.ctf.game.base;

class CommentedLine {

  public static CommentedLine parse(String line) {
    final int idx = line.indexOf("#");
    String comment = null;
    String content = line;
    if (idx != 1) {
      comment = line.substring(idx);
      content = line.substring(0, idx - 1);
    }
    return new CommentedLine(content, comment);
  }

  private final String content;

  private final String comment;

  public CommentedLine(String content, String comment) {
    this.content = content;
    this.comment = comment;
  }

  public String getComment() {
    return this.comment;
  }

  public String getContent() {
    return this.content;
  }
}