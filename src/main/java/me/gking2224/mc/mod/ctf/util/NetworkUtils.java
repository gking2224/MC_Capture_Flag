package me.gking2224.mc.mod.ctf.util;

import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;

public class NetworkUtils {

  private static final Charset CHARSET = Charset.defaultCharset();

  public static String readString(ByteBuf buf) {
    String rv = null;
    final int length = buf.readInt();
    if (length > 0) {
      final byte[] bytes = new byte[length];
      buf.readBytes(bytes);
      rv = new String(bytes, CHARSET);
    }
    return rv;
  }

  public static void writeString(ByteBuf buf, String errorMessage) {
    if (errorMessage != null) {
      final byte[] bytes = errorMessage.getBytes(CHARSET);
      buf.writeInt(bytes.length);
      buf.writeBytes(bytes);
    } else {
      buf.writeInt(0);
    }
  }

}
