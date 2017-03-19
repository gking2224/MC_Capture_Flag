package me.gking2224.mc.mod.ctf.game.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import me.gking2224.mc.mod.ctf.game.Bounds;
import me.gking2224.mc.mod.ctf.game.CtfTeam;
import me.gking2224.mc.mod.ctf.game.CtfTeam.TeamColour;
import me.gking2224.mc.mod.ctf.game.GameOptions;
import me.gking2224.mc.mod.ctf.util.NBTUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;

public class GameData extends WorldSavedData {

	private static final String FLAG_LOC = "flagLoc";
	private static final String BASE_LOC = "baseLoc";
	private static final String FLAG_HOLDER = "flagHolder";
	private static final String SCORE = "score";
	private static final String TEAM = "team";
	private static final String NUM_TEAMS = "num_teams";
	private static final String BOUNDS = "bounds";
	private static final String NAME = "name";
	private static final String OWNER = "owner";
	private static final String FLAG_HELD = "flagHeld";
	private static final String DATA_NAME = "CTF_GAME_DATA_";
	private static final String OPTIONS = "options";

	private String owner;
	private String name;
	private Bounds bounds;
	private Map<TeamColour, CtfTeam> teams = new HashMap<TeamColour, CtfTeam>();
	private Map<TeamColour, Integer> score = new HashMap<TeamColour, Integer>();
	private Map<TeamColour, BlockPos> baseLocations = new HashMap<TeamColour, BlockPos>();
	private Map<TeamColour, BlockPos> flagLocations = new HashMap<TeamColour, BlockPos>();
	private Map<TeamColour, String> playerHoldingFlag = new HashMap<TeamColour, String>();
	private GameOptions options;

	public GameData() {
		this("unknown", GameOptions.getDefault());
	}
	public GameData(String name) {
		this(getDataIdentifier(name), null);
	}
	public GameData(String name, GameOptions options) {
		super(getDataIdentifier(name));
		this.name = name;
		this.options = options;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getName() {
		return name;
	}

	public Bounds getBounds() {
		return bounds;
	}

	public void setBounds(Bounds bounds) {
		this.bounds = bounds;
	}

	public Map<TeamColour, CtfTeam> getTeams() {
		return teams;
	}

	public void setTeams(Map<TeamColour, CtfTeam> teams) {
		this.teams = teams;
	}

	public Map<TeamColour, Integer> getScore() {
		return score;
	}

	public void setScore(Map<TeamColour, Integer> score) {
		this.score = score;
	}

	public Map<TeamColour, BlockPos> getBaseLocations() {
		return baseLocations;
	}

	public void setBaseLocations(Map<TeamColour, BlockPos> baseLocations) {
		this.baseLocations = baseLocations;
	}

	public Map<TeamColour, BlockPos> getFlagLocations() {
		return flagLocations;
	}

	public void setFlagLocations(Map<TeamColour, BlockPos> flagLocations) {
		this.flagLocations = flagLocations;
	}

	public Map<TeamColour, String> getPlayerHoldingFlag() {
		return playerHoldingFlag;
	}

	public void setPlayerHoldingFlag(Map<TeamColour, String> playerHoldingFlag) {
		this.playerHoldingFlag = playerHoldingFlag;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		this.owner = nbt.getString(OWNER);
		this.name = nbt.getString(NAME);
		this.bounds = Bounds.readFromNBT(nbt, BOUNDS);
		int numTeams = nbt.getInteger(NUM_TEAMS);
		for (int i = 0; i < numTeams; i++) {
			CtfTeam team = CtfTeam.readFromNBT(nbt, TEAM + i);
			this.teams.put(team.getColour(), team);
			this.score.put(team.getColour(), nbt.getInteger(SCORE + i));
			this.baseLocations.put(team.getColour(), NBTUtils.getBlockPos(nbt, BASE_LOC + i));
			this.flagLocations.put(team.getColour(), NBTUtils.getBlockPos(nbt, FLAG_LOC + i));
			boolean flagHeld = nbt.getBoolean(FLAG_HELD+i);
			if (flagHeld) this.playerHoldingFlag.put(team.getColour(), nbt.getString(FLAG_HOLDER + i));
		}
		
		String optionsStr = nbt.getString(OPTIONS);
		System.out.printf("read game options: %s\n", optionsStr);
		this.options = (optionsStr != null) ? new GameOptions(optionsStr) : GameOptions.getDefault();
		System.out.printf("Read game from NBT: %s\n", this);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setString(OWNER, owner);
		nbt.setString(NAME, name);
		Bounds.writeToNBT(nbt, BOUNDS, bounds);
		nbt.setInteger(NUM_TEAMS, teams.size());
		int i = 0;
		for (CtfTeam t : teams.values()) {

			CtfTeam.writeToNBT(nbt, TEAM + i, t);
			TeamColour colour = t.getColour();
			nbt.setInteger(SCORE + i, score.get(colour));
			NBTUtils.setBlockPos(nbt, BASE_LOC + i, baseLocations.get(colour));
			NBTUtils.setBlockPos(nbt, FLAG_LOC + i, flagLocations.get(colour));
			String flagHolder = playerHoldingFlag.get(colour);
			nbt.setBoolean(FLAG_HELD+i, flagHolder != null);
			if (flagHolder != null) nbt.setString(FLAG_HOLDER + i, flagHolder);
			i++;
		}
		String optionsStr = options.toString();
		nbt.setString(OPTIONS, optionsStr);
		System.out.printf("wrote game options: %s\n", optionsStr);
		System.out.printf("Wrote game to NBT: %s\n", this);
		return nbt;
	}
	
	private static String getDataIdentifier(String name) {
		return DATA_NAME+name;
	}
	
	@Override
	public String toString() {
		return String.format("GameData:[owner=%s; name=%s; bounds=%s; teams=%s; score=%s; baseLocations=%s; flagLocations=%s; playerHoldingFlag=%s]",
				owner, name, bounds, teams, score, baseLocations, flagLocations, playerHoldingFlag);
	}

	public static GameData create(World world, String name, GameOptions options) {

		MapStorage storage = world.getPerWorldStorage();
		GameData instance = new GameData(name, options);
		storage.setData(getDataIdentifier(name), instance);
		System.out.printf("Stored GameData: %s\n", instance);
		return instance;
	}

	public static Optional<GameData> get(World world, String name) {
		MapStorage storage = world.getPerWorldStorage();
		GameData instance = (GameData) storage.getOrLoadData(GameData.class, getDataIdentifier(name));
		System.out.printf("Loaded GameData: %s\n", instance);
		return Optional.ofNullable(instance);
	}
	public GameOptions getOptions() {
		return this.options;
	}
}
