package me.gking2224.mc.mod.ctf.game;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class Bounds {
	private BlockPos from;
	private BlockPos to;
	
	public Bounds() {
		
	}
	
	public Bounds(BlockPos from, BlockPos to) {
		this.from = from;
		this.to = to;
	}

	public BlockPos getFrom() {
		return from;
	}
	public BlockPos getTo() {
		return to;
	}
	
	public static int[] toIntArray(Bounds b) {
		return new int[] {
				b.from.getX(), b.from.getZ(),
				b.to.getX(), b.to.getZ()
		};
	}

	public static Bounds readFromNBT(NBTTagCompound nbt, String key) {
		int[] a = nbt.getIntArray(key);
		Bounds rv = new Bounds(new BlockPos(a[0], 0, a[1]), new BlockPos(a[2], 0, a[3]));
		return rv;
	}

	public static void writeToNBT(NBTTagCompound nbt, String key, Bounds b) {
		nbt.setIntArray(key, toIntArray(b));
	}
	
	public Vec3i getSize() {
		return new Vec3i(to.getX() - from.getX(), 0, to.getZ() - from.getZ());
	}

	@Override
	public String toString() {
		return String.format("[%s : %s]", from, to);
	}
	
}
