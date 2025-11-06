package net.veroxuniverse.create_mininglaser.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.veroxuniverse.create_mininglaser.CreateMininglaser;

public class ModNetworking {
    private static final String VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(CreateMininglaser.MODID, "main"),
            () -> VERSION, VERSION::equals, VERSION::equals
    );

    public static void register() {
        int id = 0;
        INSTANCE.registerMessage(id++, TierSyncPacket.class, TierSyncPacket::encode, TierSyncPacket::decode, TierSyncPacket::handle);
    }
}
