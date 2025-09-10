package net.veroxuniverse.create_mininglaser.content.items;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;


import java.util.*;

public class TierDefs {
    private static final Map<ResourceLocation, TierDef> BY_ID = new HashMap<>();
    private static final Map<Item, TierDef> BY_CORE_ITEM = new HashMap<>();

    public static void register(TierDef def) {
        BY_ID.put(def.id, def);
        Item item = ForgeRegistries.ITEMS.getValue(def.coreItem);
        if (item != null) BY_CORE_ITEM.put(item, def);
    }

    public static void reload(Collection<TierDef> defs) {
        BY_ID.clear();
        BY_CORE_ITEM.clear();
        for (TierDef def : defs) register(def);
    }

    public static TierDef get(ResourceLocation id) { return BY_ID.get(id); }
    public static TierDef byCoreItem(Item item)    { return BY_CORE_ITEM.get(item); }

    public static Collection<TierDef> all() { return Collections.unmodifiableCollection(BY_ID.values()); }
}
