package me.gking2224.mc.mod.ctf.net;

import io.netty.buffer.ByteBuf;
import me.gking2224.mc.mod.ctf.util.NetworkUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class ConfirmationResponse implements IMessage {

  private boolean confirmed;
  private String errorMessage;

  public ConfirmationResponse() {}

  public ConfirmationResponse(boolean confirmed) {
    this(confirmed, null);
  }

  public ConfirmationResponse(boolean confirmed, String errorMessage) {
    this.confirmed = confirmed;
    this.errorMessage = errorMessage;
  }

  @Override public void fromBytes(ByteBuf buf) {
    this.confirmed = buf.readBoolean();
    this.errorMessage = NetworkUtils.readString(buf);
  }

  public String getErrorMessage() {
    return this.errorMessage;
  }

  public boolean isConfirmed() {
    return this.confirmed;
  }

  @Override public void toBytes(ByteBuf buf) {
    buf.writeBoolean(this.confirmed);
    NetworkUtils.writeString(buf, this.errorMessage);
  }
}
