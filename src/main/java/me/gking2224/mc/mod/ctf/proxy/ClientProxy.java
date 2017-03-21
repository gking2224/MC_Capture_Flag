package me.gking2224.mc.mod.ctf.proxy;

import java.util.HashMap;
import java.util.Map;

import me.gking2224.mc.mod.ctf.ModCaptureTheFlag;
import me.gking2224.mc.mod.ctf.net.CanMovePlayerToPosition;
import me.gking2224.mc.mod.ctf.net.MoveItemToHand;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.relauncher.Side;

public class ClientProxy extends CommonProxy {

  private static final Map<Class<? extends IMessage>, Class<? extends IMessageHandler<? extends IMessage, ? extends IMessage>>> handledMessages = new HashMap<Class<? extends IMessage>, Class<? extends IMessageHandler<? extends IMessage, ? extends IMessage>>>();

  static {
    handledMessages.put(MoveItemToHand.class, MoveItemToHand.Handler.class);
    handledMessages.put(CanMovePlayerToPosition.class,
            CanMovePlayerToPosition.Handler.class);
  }

  @Override public Map<Class<? extends IMessage>, Class<? extends IMessageHandler<? extends IMessage, ? extends IMessage>>> getHandledMessages() {
    return handledMessages;
  }

  @Override protected Side getSide() {
    return Side.SERVER;
  }

  @Override public void init(FMLInitializationEvent event) {
    super.init(event);
  }

  @Override public void registerItemRenderer(Item item, int meta, String id) {
    ModelLoader.setCustomModelResourceLocation(item, meta,
            new ModelResourceLocation(ModCaptureTheFlag.modId + ":" + id,
                    "inventory"));
  }
}
