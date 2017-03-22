package me.gking2224.mc.mod.ctf.event;

import java.util.Optional;

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
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
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
    System.out.println(String.format("Block %s(%d, state=%d) placed\n", 
            pb.getBlock().getRegistryName(), id, sid));
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

  @SubscribeEvent public void onDamage(LivingHurtEvent event) {
    final EntityLivingBase entity = event.getEntityLiving();
    if (entity == null) { return; }
    if (EntityPlayer.class.isAssignableFrom(entity.getClass())) {
      final EntityPlayer attackee = (EntityPlayer) entity;
      final Entity source = event.getSource().getEntity();
      if (source == null) { return; }
      if (EntityPlayer.class.isAssignableFrom(source.getClass())) {
        final EntityPlayer attacker = (EntityPlayer) source;

        final GameManager gameManager = GameManager.get();

        if (!gameManager.allowPlayerToAttackedPlayer(attacker, attackee)) {
          event.setCanceled(true);
          System.out.println(String.format("%s prevented from attacking %s\n", attacker, 
                  attackee));
        }
      }

    }
  }

  @SubscribeEvent public void playerRespawned(PlayerRespawnEvent event) {
    final EntityPlayer player = event.player;
    final GameManager gameManager = GameManager.get();
    final String playerName = player.getName();
    System.out.println(String.format("Player %s respawned\n",  playerName));
    final Optional<Game> g = gameManager.getPlayerActiveGame(playerName);
    g.ifPresent(game -> {
      System.out.println(String.format("Rejoin game %s\n",  game.getName()));
      GameEventManager.get().playerRespawned(player, game);
    });
  }

  @SubscribeEvent public void resetGame(GameResetEvent event) {

    GameEventManager.get().schedule(
            () -> GameWorldManager.get().resetFlags(event.getGame()), 1);
  }
}
