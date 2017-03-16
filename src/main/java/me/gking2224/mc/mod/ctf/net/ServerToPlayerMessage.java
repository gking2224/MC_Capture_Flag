package me.gking2224.mc.mod.ctf.net;

import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class ServerToPlayerMessage implements IMessage {

	private static final Charset CHARSET = Charset.defaultCharset();

	private String message;
	private Map<String, String> options = new HashMap<String, String>();

	public ServerToPlayerMessage() {

	}

	public ServerToPlayerMessage(String message) {
		this(message, Collections.emptyMap());
	}

	public ServerToPlayerMessage(String message, Map<String, String> options) {
		this.message = message;
		this.options.putAll(options);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(message.length());
		buf.writeBytes(message.getBytes(CHARSET));
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		int messageLength = buf.readInt();
		byte[] messageBytes = new byte[messageLength];
		buf.readBytes(messageBytes);
		this.message = new String(messageBytes, CHARSET);
	}

	public String getMessage() {
		return message;
	}

	public Map<String, String> getOptions() {
		return options;
	}
}
