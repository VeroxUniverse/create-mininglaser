package net.veroxuniverse.create_mininglaser.content.items;

import net.minecraft.world.item.Item;

public class DrillCoreItem extends Item {
    private final DrillTier tier;
    public DrillCoreItem(Properties p, DrillTier tier){
        super(p); this.tier = tier;
    }
    public DrillTier getTier(){
        return tier;
    }
}
