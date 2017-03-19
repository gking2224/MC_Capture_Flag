package me.gking2224.mc.mod.ctf.event;

import static java.lang.String.format;
import static me.gking2224.mc.mod.ctf.util.StringUtils.toIText;

import java.util.Optional;

import me.gking2224.mc.mod.ctf.game.CtfTeam;
import me.gking2224.mc.mod.ctf.game.Game;
import me.gking2224.mc.mod.ctf.game.GameManager;
import me.gking2224.mc.mod.ctf.game.GameWorldManager;
import me.gking2224.mc.mod.ctf.game.event.GameEventManager;
import me.gking2224.mc.mod.ctf.game.event.GameResetEvent;
import me.gking2224.mc.mod.ctf.game.event.NewGameEvent;
import me.gking2224.mc.mod.ctf.item.Flag;
import me.gking2224.mc.mod.ctf.item.ItemBase;
import me.gking2224.mc.mod.ctf.util.StringUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;

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
	
	@SubscribeEvent
	public void resetGame(PlayerRespawnEvent event) {
		EntityPlayer player = event.player;
		GameManager gameManager = GameManager.get();
		String playerName = player.getName();
		Optional<Game> g = gameManager.getPlayerActiveGame(playerName);
		g.ifPresent(game -> {
			Optional<CtfTeam> t = game.getTeamForPlayer(playerName);
			t.ifPresent(team -> {
				int respawnDelay = game.getOptions().getInteger("respawnDelay").orElse(10);
				gameManager.broadCastMessageToPlayer(playerName, toIText(format(
						"Going back to %sbase in 10 seconds", team.getColour().toString())));
				gameManager.toolUpPlayer(player);
				GameEventManager.get().schedule(() -> gameManager.sendPlayerToBase(game, player), respawnDelay);
				;
			});
		});
	}
}
