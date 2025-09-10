package net.veroxuniverse.create_mininglaser.content.items;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class TierRegistry extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().create();
    private static final Map<ResourceLocation, TierDef> BY_ID = new HashMap<>();
    private static final Map<ResourceLocation, TierDef> BY_CORE_ITEM = new HashMap<>();
    public static final TierRegistry INSTANCE = new TierRegistry();

    public TierRegistry() { super(GSON, "drill_tiers"); }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager rm, ProfilerFiller profiler) {
        BY_ID.clear(); BY_CORE_ITEM.clear();

        for (var e : map.entrySet()) {
            var fileId = e.getKey();
            var o = e.getValue().getAsJsonObject();
            TierDef def = parse(fileId, o);
            BY_ID.put(def.id, def);
            BY_CORE_ITEM.put(new ResourceLocation(GsonHelper.getAsString(o, "core_item")), def);
        }
        TierDefs.reload(BY_ID.values());
    }

    private TierDef parse(ResourceLocation fileId, JsonObject o) {
        ResourceLocation id = new ResourceLocation(GsonHelper.getAsString(o, "id", fileId.toString()));
        int order = GsonHelper.getAsInt(o, "order");
        ResourceLocation core = new ResourceLocation(GsonHelper.getAsString(o, "core_item"));
        double su128 = GsonHelper.getAsDouble(o, "stress_at_128");
        int min = GsonHelper.getAsInt(o, "min_rpm", 128);
        int max = GsonHelper.getAsInt(o, "max_rpm", 256);
        ResourceLocation head = o.has("head_partial")
                ? new ResourceLocation(GsonHelper.getAsString(o, "head_partial"))
                : null;
        return new TierDef(id, order, core, su128, min, max, head);
    }

    public static TierDef byId(ResourceLocation id) { return BY_ID.get(id); }
    public static TierDef byCoreItem(Item item) {
        return BY_CORE_ITEM.get(ForgeRegistries.ITEMS.getKey(item));
    }
    public static Collection<TierDef> all() { return BY_ID.values(); }
}
