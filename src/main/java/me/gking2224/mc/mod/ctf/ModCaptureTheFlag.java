package me.gking2224.mc.mod.ctf;

import me.gking2224.mc.mod.ctf.blocks.ModBlocks;
import me.gking2224.mc.mod.ctf.item.ModItems;
import me.gking2224.mc.mod.ctf.proxy.CtfSidedProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(
  modid = ModCaptureTheFlag.modId,
  name = ModCaptureTheFlag.name,
  version = ModCaptureTheFlag.version,
  acceptedMinecraftVersions = "[1.11.2]") public class ModCaptureTheFlag
{

  private static final String CLIENT_PROXY = "me.gking2224.mc.mod.ctf.proxy.ClientProxy";
  private static final String SERVER_PROXY = "me.gking2224.mc.mod.ctf.proxy.ServerProxy";
  public static final String modId = "ctf";
  public static final String name = "Capture The Flag";
  public static final String version = "1.0.0";

  @Mod.Instance(modId) public static ModCaptureTheFlag instance;

  @SidedProxy(
    serverSide = SERVER_PROXY,
    clientSide = CLIENT_PROXY) public static CtfSidedProxy proxy;

  @Mod.EventHandler public void init(FMLInitializationEvent event) {
    proxy.init(event);
  }

  @Mod.EventHandler public void postInit(FMLPostInitializationEvent event) {
    System.out.println("post-init");
  }

  @Mod.EventHandler public void preInit(FMLPreInitializationEvent event) {
    System.out.println("pre-init");
    ModBlocks.init();
    ModItems.init();
    ModBlocks.postItemsInit();
  }

  @Mod.EventHandler public void serverLoad(FMLServerStartingEvent event) {
    proxy.serverLoad(event);
  }

}
