package me.gking2224.mc.mod.ctf.blocks;

import net.minecraft.block.material.Material;

public class BlockFlag extends BlockBase {

	public BlockFlag(String name) {
		super(Material.WOOD, name);

		setHardness(0f);
		setResistance(1f);
	}
}