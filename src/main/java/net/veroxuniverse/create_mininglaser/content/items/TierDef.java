package net.veroxuniverse.create_mininglaser.content.items;

import net.minecraft.resources.ResourceLocation;

public final class TierDef {
    public final ResourceLocation id;
    public final int order;
    public final ResourceLocation coreItem;
    public final double stress_at_minRPM;
    public final int minRpm;
    public final int maxRpm;
    public final ResourceLocation headPartial;

    public TierDef(ResourceLocation id, int order, ResourceLocation coreItem,
                   double stress_at_minRPM, int minRpm, int maxRpm, ResourceLocation headPartial) {
        this.id = id; this.order = order; this.coreItem = coreItem;
        this.stress_at_minRPM = stress_at_minRPM; this.minRpm = minRpm; this.maxRpm = maxRpm;
        this.headPartial = headPartial;
    }
}
