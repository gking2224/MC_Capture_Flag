package me.gking2224.mc.mod.ctf.game;

import net.minecraft.nbt.NBTTagCompound;

public class Bounds {

	@Override
	public String toString() {
		return String.format("[%s : %s]", from, to);
	}
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
	
	public static int[] toIntArray(Bounds b) {
		return new int[] {
				b.from.getX(), b.from.getZ(),
				b.to.getX(), b.to.getZ()
		};
	}

	public static Bounds readFromNBT(NBTTagCompound nbt, String key) {
		int[] a = nbt.getIntArray(key);
		ChunkLocation from = new ChunkLocation(a[0], a[1]);
		ChunkLocation to = new ChunkLocation(a[2], a[3]);
		Bounds rv = new Bounds(from, to);
		return rv;
	}

	public static void writeToNBT(NBTTagCompound nbt, String key, Bounds b) {
		nbt.setIntArray(key, toIntArray(b));
	}
	
	
}
