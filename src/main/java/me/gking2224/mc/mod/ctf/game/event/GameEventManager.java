package me.gking2224.mc.mod.ctf.game.event;

import static java.lang.String.format;
import static me.gking2224.mc.mod.ctf.util.StringUtils.blockPosStr;

import java.util.Optional;

import me.gking2224.mc.mod.ctf.game.Game;
import me.gking2224.mc.mod.ctf.game.GameManager;
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
	
	@SuppressWarnings("unused")
	private MinecraftServer server;
	@SuppressWarnings("unused")
	private World world;
	
	private GameEventManager(MinecraftServer server) {
		this.server = server;
		this.world = server.getEntityWorld();
	}

	public void playerPickedUpFlag(String player, ItemBase item) {
		Optional<Game> g = GameManager.get().getPlayerActiveGame(player);
		g.ifPresent((game) -> {
			String team = game.getTeamForPlayer(player);
			if (!Flag.isOwnTeamFlag(item, team)) {
				broadcastTeamCapturedFlag(game, player, Flag.getFlagColour(item));
			}
			else {
				//pickedUpOwnFlag(game, player, item);
			}
		});
	}

	private void broadcastTeamCapturedFlag(Game game, String player, String team) {
		GameManager.get().broadcastToAllPlayers(game, format("Player %s has captured %s team's flag!", player, team));
	}

	public void flagPlaced(String player, ItemBase flag, BlockPos blockPos) {
		Optional<Game> g = GameManager.get().getPlayerActiveGame(player);
		g.ifPresent((game) -> GameManager.get().broadcastToAllPlayers(
				game, format("Player %s has placed %s flag at (%d %d %d)!", player, Flag.getFlagColour(flag), blockPosStr(blockPos))));
	}
}
