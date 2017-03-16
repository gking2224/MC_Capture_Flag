package me.gking2224.mc.mod.ctf.game.event;

import static java.lang.String.format;

import java.util.Optional;

import me.gking2224.mc.mod.ctf.game.Game;
import me.gking2224.mc.mod.ctf.game.GameManager;
import me.gking2224.mc.mod.ctf.game.GameWorldManager;
import me.gking2224.mc.mod.ctf.item.Flag;
import me.gking2224.mc.mod.ctf.item.ItemBase;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GameEventManager {
	
	private static GameEventManager instance = null;

	public static void init(MinecraftServer server) {
		if (instance != null) throw new IllegalStateException();
		instance = new GameEventManager(server);
	}
	
	public final static GameEventManager get() { return instance; }
	
	@SuppressWarnings("unused") private MinecraftServer server;
	@SuppressWarnings("unused") private World world;
	
	private GameEventManager(MinecraftServer server) {
		this.server = server;
		this.world = server.getEntityWorld();
	}

	public void playerPickedUpFlag(String player, ItemBase item) {
		Optional<Game> g = GameManager.get().getPlayerActiveGame(player);
		g.ifPresent((game) -> {
			String team = game.getTeamForPlayer(player);
			game.setPlayerHoldingFlag(Flag.getFlagColour(item), player);
			if (!Flag.isOwnTeamFlag(item, team)) {
				broadcastTeamCapturedFlag(game, player, Flag.getFlagColour(item));
			}
			else {
				GameManager.get().broadcastToAllPlayers(game, format("Player %s has picked up his own flag!", player));
			}
		});
	}

	private void broadcastTeamCapturedFlag(Game game, String player, String team) {
		GameManager.get().broadcastToAllPlayers(game, format("Player %s has got %s team's flag!", player, team));
	}

	public void flagPlaced(String player, ItemBase flag, BlockPos blockPos) {
		Optional<Game> g = GameManager.get().getPlayerActiveGame(player);
		g.ifPresent((game) -> {
			game.setFlagBlockPosition(Flag.getFlagColour(flag), blockPos);
			String team = game.getTeamForPlayer(player);
			String flagColour = Flag.getFlagColour(flag);
			if (!Flag.isOwnTeamFlag(flag, team) && GameWorldManager.get().isInHomeBase(game, team, blockPos)) {
				GameManager.get().flagCaptureComplete(game, player, team, flagColour);
			}
			else {
				GameManager.get().broadcastToAllPlayers(game, format("Player %s has placed his team's flag!", player));
			}
		});
	}

	public void schedule(Runnable r, int timeout, String description) {

		Runnable target = () -> {
			try {
				Thread.sleep(timeout);
				r.run();
			} catch (InterruptedException e) {
				GameManager.get().log(format("Thread.sleep interrupted before scheduled task (%s) executed, running now anyway...", description));
				r.run();
			};
		};
		new Thread(target).start();
	}
}
