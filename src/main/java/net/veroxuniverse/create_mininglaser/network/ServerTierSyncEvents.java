package net.veroxuniverse.create_mininglaser.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.veroxuniverse.create_mininglaser.CreateMininglaser;
import net.veroxuniverse.create_mininglaser.content.items.TierDef;
import net.veroxuniverse.create_mininglaser.content.items.TierRegistry;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = CreateMininglaser.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerTierSyncEvents {

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        List<TierDef> defs = new ArrayList<>(TierRegistry.INSTANCE.getAll());
        ModNetworking.INSTANCE.sendTo(new TierSyncPacket(defs), sp.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT);
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent e) {
        List<TierDef> defs = new ArrayList<>(TierRegistry.INSTANCE.getAll());
        if (e.getPlayer() != null) {
            ServerPlayer sp = e.getPlayer();
            ModNetworking.INSTANCE.sendTo(new TierSyncPacket(defs), sp.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT);
        } else {
            for (ServerPlayer sp : e.getPlayerList().getPlayers()) {
                ModNetworking.INSTANCE.sendTo(new TierSyncPacket(defs), sp.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT);
            }
        }
    }
}
