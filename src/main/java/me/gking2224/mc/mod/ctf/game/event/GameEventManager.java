package me.gking2224.mc.mod.ctf.game.event;

import static java.lang.String.format;
import static me.gking2224.mc.mod.ctf.util.InventoryUtils.moveItemFromInventoryToPlayerHand;

import java.util.Optional;

import me.gking2224.mc.mod.ctf.game.CtfTeam;
import me.gking2224.mc.mod.ctf.game.CtfTeam.TeamColour;
import me.gking2224.mc.mod.ctf.game.Game;
import me.gking2224.mc.mod.ctf.game.GameManager;
import me.gking2224.mc.mod.ctf.game.GameWorldManager;
import me.gking2224.mc.mod.ctf.item.Flag;
import me.gking2224.mc.mod.ctf.item.ItemBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
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
	private World world;
	
	private GameEventManager(MinecraftServer server) {
		this.server = server;
		this.world = server.getEntityWorld();
	}

	public void playerPickedUpFlag(String playerName, ItemBase item) {
		Optional<Game> g = GameManager.get().getPlayerActiveGame(playerName);
		g.ifPresent((game) -> {
			Optional<EntityPlayer> p = GameManager.get().getPlayerByName(playerName);
			p.ifPresent(player -> {
				Optional<CtfTeam> t = game.getTeamForPlayer(playerName);
				t.ifPresent((team) -> {
					TeamColour flagColour = Flag.getFlagColour(item);
					game.setPlayerHoldingFlag(flagColour, playerName);
					if (flagColour != team.getColour()) {
						broadcastTeamCapturedFlag(game, playerName, flagColour);
						schedule(() -> moveItemFromInventoryToPlayerHand(player, item), 0);
					}
					else {
						GameManager.get().broadcastToAllPlayers(game, format("Player %s tried to pick up his own flag!", playerName));
						GameWorldManager.get().resetFlag(game, flagColour);
					}
				});
			});
		});
	}

	private void broadcastTeamCapturedFlag(Game game, String player, TeamColour teamColour) {
		GameManager.get().broadcastToAllPlayers(game, format("Player %s has got %s team's flag!", player, teamColour));
	}

	public void flagPlaced(String player, ItemBase flag, BlockPos blockPos) {
		Optional<Game> g = GameManager.get().getPlayerActiveGame(player);
		g.ifPresent((game) -> {
			TeamColour flagColour = Flag.getFlagColour(flag);
			game.setFlagBlockPosition(flagColour, blockPos);
			System.out.printf("%s: %s flag position updated as %s\n", Thread.currentThread().getName(), flagColour, blockPos);
			Optional<CtfTeam> t = game.getTeamForPlayer(player);
			t.ifPresent(team -> {
				if (!Flag.isOwnTeamFlag(flag, team)) {
					if (GameWorldManager.get().isInHomeBase(game, team.getColour(), blockPos)) {
						GameManager.get().gameRoundWon(game, player, team, flagColour);
					}
				}
				else {
					GameManager.get().broadcastToAllPlayers(game, format("Player %s has placed his team's flag!", player));
				}
			});
		});
	}

	public void schedule(Runnable r) {
		schedule(r, -1);
	}

	public void schedule(Runnable r, int delay) {
		if (delay != -1) {
			try { Thread.sleep(delay); } catch (Exception e) {}
		}
		new Thread(r).start();
	}

	public void playerDied(Entity entity) {
//		GameManager.get().broadcastToAllPlayers(game, format("Player %s has placed his team's flag!", player));
	}
}
