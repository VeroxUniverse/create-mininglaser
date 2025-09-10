package net.veroxuniverse.create_mininglaser.content.items;

import net.minecraft.resources.ResourceLocation;

public final class TierDef {
    public final ResourceLocation id;
    public final int order;
    public final ResourceLocation coreItem;
    public final double stressAt128;
    public final int minRpm;
    public final int maxRpm;
    public final ResourceLocation headPartial;

    public TierDef(ResourceLocation id, int order, ResourceLocation coreItem,
                   double stressAt128, int minRpm, int maxRpm, ResourceLocation headPartial) {
        this.id = id; this.order = order; this.coreItem = coreItem;
        this.stressAt128 = stressAt128; this.minRpm = minRpm; this.maxRpm = maxRpm;
        this.headPartial = headPartial;
    }
}
