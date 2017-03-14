package me.gking2224.mc.mod.ctf.game;

public class Bounds {

	private ChunkLocation from;
	private ChunkLocation to;
	
	public Bounds() {
		
	}
	
	public Bounds(ChunkLocation from, ChunkLocation to) {
		setFrom(from);
		setTo(to);
	}

	public ChunkLocation getFrom() {
		return from;
	}
	public void setFrom(ChunkLocation from) {
		this.from = from;
	}
	public ChunkLocation getTo() {
		return to;
	}
	public void setTo(ChunkLocation to) {
		this.to = to;
	}
	
	
}
