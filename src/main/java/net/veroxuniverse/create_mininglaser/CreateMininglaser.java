package net.veroxuniverse.create_mininglaser;

import com.mojang.logging.LogUtils;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.veroxuniverse.create_mininglaser.content.items.TierRegistry;
import net.veroxuniverse.create_mininglaser.content.laser.LaserDrillControllerRenderer;
import net.veroxuniverse.create_mininglaser.network.ModNetworking;
import net.veroxuniverse.create_mininglaser.network.TierSyncPacket;
import net.veroxuniverse.create_mininglaser.ponder.MiningLaserPonderPlugin;
import net.veroxuniverse.create_mininglaser.registry.*;
import org.slf4j.Logger;

import java.util.ArrayList;

@Mod(CreateMininglaser.MODID)
public class CreateMininglaser {

    public static final String MODID = "create_mininglaser";
    private static final Logger LOGGER = LogUtils.getLogger();


    public CreateMininglaser() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);

        ModRecipes.SERIALIZERS.register(modEventBus);
        ModRecipes.TYPES.register(modEventBus);

        ModBlockEntities.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModItemsNoTab.register(modEventBus);
        ModTabs.register(modEventBus);
        ModPartials.init();
        ModConfigs.register();

        MinecraftForge.EVENT_BUS.addListener(this::onAddReloadListener);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
        event.enqueueWork(() -> {
            ModCreateCompat.init();
            ModNetworking.register();
        });
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }

    private void onAddReloadListener(AddReloadListenerEvent e) {
        e.addListener(TierRegistry.INSTANCE);
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("HELLO FROM CLIENT SETUP");

            event.enqueueWork(() -> {
                BlockEntityRenderers.register(
                        ModBlockEntities.LASER_DRILL_ENTITY.get(),
                        LaserDrillControllerRenderer::new
                );
                ModPartials.init();
                PonderIndex.addPlugin(new MiningLaserPonderPlugin());

            });
        }
    }

    @Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ClientReloadHandler {
        @SubscribeEvent
        public static void onClientReload(net.minecraftforge.client.event.RegisterClientReloadListenersEvent event) {
            event.registerReloadListener(TierRegistry.INSTANCE);
        }
    }

    @Mod.EventBusSubscriber(modid = CreateMininglaser.MODID)
    public static class ServerEvents {
        @SubscribeEvent
        public static void onPlayerJoin(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent e) {
            if (!(e.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)) return;
            ModNetworking.INSTANCE.send(
                    net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                    new TierSyncPacket(new ArrayList<>(net.veroxuniverse.create_mininglaser.content.items.TierDefs.getAll()))
            );
        }
    }

}
