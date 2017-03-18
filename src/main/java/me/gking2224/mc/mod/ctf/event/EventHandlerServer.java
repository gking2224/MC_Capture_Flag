package me.gking2224.mc.mod.ctf.event;

import java.util.Optional;

import me.gking2224.mc.mod.ctf.game.GameWorldManager;
import me.gking2224.mc.mod.ctf.game.event.GameEventManager;
import me.gking2224.mc.mod.ctf.game.event.GameResetEvent;
import me.gking2224.mc.mod.ctf.game.event.NewGameEvent;
import me.gking2224.mc.mod.ctf.item.Flag;
import me.gking2224.mc.mod.ctf.item.ItemBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventHandlerServer extends EventHandlerCommon {
	
	@SubscribeEvent
	public void itemPlaced(RightClickBlock event) {
		System.out.println("itemPlaced");
		EntityPlayer player = event.getEntityPlayer();
		Vec3d hitVec = event.getHitVec();
		Optional<ItemBase> f = Flag.toFlag(event.getItemStack());
		f.ifPresent(flag -> GameEventManager.get().flagPlaced(
				player.getName(), flag, new BlockPos((int)hitVec.xCoord, (int)hitVec.yCoord, (int)hitVec.zCoord)));
	}
	
	@SubscribeEvent
	public void newGame(NewGameEvent event) {

		GameWorldManager.get().createGameArea(event.getGame());
	}
	
	@SubscribeEvent
	public void resetGame(GameResetEvent event) {
		
		GameEventManager.get().schedule(() -> GameWorldManager.get().resetFlags(event.getGame()), 1000);
	}
}
