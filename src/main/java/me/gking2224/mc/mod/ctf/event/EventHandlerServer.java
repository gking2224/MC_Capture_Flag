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
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;

public class EventHandlerServer extends EventHandlerCommon {

  @SubscribeEvent public void blockPlaced(PlaceEvent event) {
    System.out.println("block placed");
    final IBlockState pb = event.getPlacedBlock();
    final int id = Block.getIdFromBlock(pb.getBlock());
    final int sid = Block.getStateId(pb);
    System.out.printf("Block %s(%d, state=%d) placed\n",
            pb.getBlock().getRegistryName(), id, sid);
  }

  @SubscribeEvent public void itemPlaced(RightClickBlock event) {
    System.out.println("itemPlaced");
    final EntityPlayer player = event.getEntityPlayer();
    final Vec3d hitVec = event.getHitVec();
    final ItemStack item = event.getItemStack();
    final Optional<ItemBase> f = Flag.toFlag(item);
    // final int i = Block.getIdFromBlock(item.getItem());
    f.ifPresent(flag -> GameEventManager.get().flagPlaced(player.getName(),
            flag, new BlockPos((int) hitVec.xCoord, (int) hitVec.yCoord,
                    (int) hitVec.zCoord)));
  }

  @SubscribeEvent public void newGame(NewGameEvent event) {

    GameWorldManager.get().createGameArea(event.getGame());
  }

  @SubscribeEvent public void resetGame(GameResetEvent event) {

    GameEventManager.get().schedule(
            () -> GameWorldManager.get().resetFlags(event.getGame()), 1000);
  }

  @SubscribeEvent public void resetGame(PlayerRespawnEvent event) {
    final EntityPlayer player = event.player;
    final GameManager gameManager = GameManager.get();
    final String playerName = player.getName();
    final Optional<Game> g = gameManager.getPlayerActiveGame(playerName);
    g.ifPresent(game -> {
      final Optional<CtfTeam> t = game.getTeamForPlayer(playerName);
      t.ifPresent(team -> {
        final int respawnDelay = game.getOptions().getInteger("respawnDelay")
                .orElse(10);
        gameManager.broadCastMessageToPlayer(playerName,
                toIText(format("Going back to %s base in 10 seconds",
                        team.getColour().toString())));
        gameManager.toolUpPlayer(player);
        GameEventManager.get().schedule(
                () -> gameManager.sendPlayerToBase(game, player), respawnDelay);
        ;
      });
    });
  }
}
