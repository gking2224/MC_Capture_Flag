package me.gking2224.mc.mod.ctf.game.event;

import me.gking2224.mc.mod.ctf.game.Game;
import net.minecraftforge.fml.common.eventhandler.Event;

public class GameResetEvent extends Event {

	private Game game;

	public GameResetEvent(Game game) {
		this.game = game;
	}

	public Game getGame() {
		return game;
	}

}
