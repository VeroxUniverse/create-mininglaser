package net.veroxuniverse.create_mininglaser.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.veroxuniverse.create_mininglaser.content.items.TierDef;
import net.veroxuniverse.create_mininglaser.content.items.TierDefs;
import java.util.function.Supplier;
import java.util.ArrayList;
import java.util.List;

public class TierSyncPacket {
    private final List<TierDef> tiers;

    public TierSyncPacket(List<TierDef> tiers) {
        this.tiers = tiers;
    }

    public static void encode(TierSyncPacket pkt, FriendlyByteBuf buf) {
        buf.writeVarInt(pkt.tiers.size());
        for (TierDef def : pkt.tiers) {
            buf.writeResourceLocation(def.id);
            buf.writeVarInt(def.order);
            buf.writeResourceLocation(def.coreItem);
            buf.writeDouble(def.stress_at_minRPM);
            buf.writeVarInt(def.minRpm);
            buf.writeVarInt(def.maxRpm);
            buf.writeResourceLocation(def.headPartial);
        }
    }

    public static TierSyncPacket decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<TierDef> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(new TierDef(
                    buf.readResourceLocation(),
                    buf.readVarInt(),
                    buf.readResourceLocation(),
                    buf.readDouble(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readResourceLocation()
            ));
        }
        return new TierSyncPacket(list);
    }

    public static void handle(TierSyncPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            TierDefs.setAll(pkt.tiers);
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.level != null) {
                net.veroxuniverse.create_mininglaser.content.laser.recipe.JEI.JeiPluginMod.reloadJeiRecipes();
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
