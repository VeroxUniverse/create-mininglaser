package net.veroxuniverse.create_mininglaser.content.laser.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.veroxuniverse.create_mininglaser.content.items.TierDef;
import net.veroxuniverse.create_mininglaser.content.items.TierDefs;
import net.veroxuniverse.create_mininglaser.registry.ModRecipes;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DrillCoreRecipe implements Recipe<Container> {

    public record EnvFilter(Set<ResourceLocation> dimensions,
                            Set<ResourceLocation> biomeIds,
                            Set<ResourceLocation> biomeTagIds) {
        public static final EnvFilter NONE = new EnvFilter(Set.of(), Set.of(), Set.of());
        public boolean isEmpty() { return dimensions.isEmpty() && biomeIds.isEmpty() && biomeTagIds.isEmpty(); }
    }

    public record Drop(ResourceLocation item, double chance, int min, int max, EnvFilter filter) {}

    private final ResourceLocation id;
    private final ResourceLocation tierId;
    private final int durationTicks;
    private final List<Drop> drops;

    public DrillCoreRecipe(ResourceLocation id, ResourceLocation tierId, int durationTicks, List<Drop> drops) {
        this.id = id;
        this.tierId = tierId;
        this.durationTicks = durationTicks;
        this.drops = List.copyOf(drops);
    }

    public ResourceLocation getTierId() { return tierId; }
    @Nullable
    public TierDef getTierDef() { return TierDefs.get(tierId); }
    @Nullable
    public ResourceLocation getCoreItemId() {
        TierDef d = getTierDef();
        return d != null ? d.coreItem : null;
    }
    public float getMinRpmForJei() {
        TierDef d = getTierDef();
        return d != null ? d.minRpm : 128f;
    }
    public double getStressAtMinForJei() {
        TierDef d = getTierDef();
        return d != null ? d.stress_at_minRPM : 0d;
    }
    public double getStressAtMaxForJei() {
        TierDef d = getTierDef();
        return d != null ? Math.max(d.stress_at_minRPM, 0d) * 2d : 0d;
    }

    public int getDurationTicks() { return durationTicks; }
    public List<Drop> getDrops() { return drops; }

    public ItemStack rollOnce(RandomSource rand, Level level, BlockPos pos){
        for (Drop d : drops){
            if (!envMatches(level, pos, d.filter())) continue;
            if (rand.nextDouble() <= d.chance()){
                int count = Mth.nextInt(rand, d.min(), d.max());
                return new ItemStack(ForgeRegistries.ITEMS.getValue(d.item()), count);
            }
        }
        return ItemStack.EMPTY;
    }

    private static boolean envMatches(Level level, BlockPos pos, EnvFilter f) {
        if (f == null || f.isEmpty()) return true;

        if (!f.dimensions().isEmpty()) {
            ResourceLocation dimId = level.dimension().location();
            if (!f.dimensions().contains(dimId)) return false;
        }

        if (!f.biomeIds().isEmpty() || !f.biomeTagIds().isEmpty()) {
            var biomeHolder = level.getBiome(pos);

            if (!f.biomeIds().isEmpty()) {
                var biomeKey = level.registryAccess()
                        .registryOrThrow(Registries.BIOME)
                        .getKey(biomeHolder.value());
                if (biomeKey == null || !f.biomeIds().contains(biomeKey))
                    return false;
            }

            if (!f.biomeTagIds().isEmpty()) {
                boolean anyTagMatch = false;
                for (ResourceLocation tagId : f.biomeTagIds()) {
                    var tagKey = TagKey.create(Registries.BIOME, tagId);
                    if (biomeHolder.is(tagKey)) {
                        anyTagMatch = true;
                        break;
                    }
                }
                if (!anyTagMatch) return false;
            }
        }

        return true;
    }

    @Override public boolean matches(Container c, Level l){ return false; }
    @Override public ItemStack assemble(Container c, RegistryAccess a){ return ItemStack.EMPTY; }
    @Override public boolean canCraftInDimensions(int w, int h){ return false; }
    @Override public ItemStack getResultItem(RegistryAccess a){ return ItemStack.EMPTY; }
    @Override public ResourceLocation getId(){ return id; }
    @Override public RecipeSerializer<?> getSerializer(){ return ModRecipes.DRILL_CORE_SERIALIZER.get(); }
    @Override public RecipeType<?> getType(){ return ModRecipes.DRILL_CORE_TYPE.get(); }

    public static class Serializer implements RecipeSerializer<DrillCoreRecipe> {

        private static EnvFilter parseEnvFilter(JsonObject o) {
            Set<ResourceLocation> dims = new HashSet<>();
            Set<ResourceLocation> biomeIds = new HashSet<>();
            Set<ResourceLocation> biomeTags = new HashSet<>();
            if (o.has("dimensions"))
                for (JsonElement e : GsonHelper.getAsJsonArray(o, "dimensions"))
                    dims.add(new ResourceLocation(GsonHelper.convertToString(e, "dimension")));
            if (o.has("biomes"))
                for (JsonElement e : GsonHelper.getAsJsonArray(o, "biomes")) {
                    String s = GsonHelper.convertToString(e, "biome");
                    if (s.startsWith("#")) biomeTags.add(new ResourceLocation(s.substring(1)));
                    else biomeIds.add(new ResourceLocation(s));
                }
            return (dims.isEmpty() && biomeIds.isEmpty() && biomeTags.isEmpty())
                    ? EnvFilter.NONE
                    : new EnvFilter(dims, biomeIds, biomeTags);
        }

        @Override
        public DrillCoreRecipe fromJson(ResourceLocation id, JsonObject json){
            ResourceLocation tierId = new ResourceLocation(GsonHelper.getAsString(json, "tier"));
            int duration = GsonHelper.getAsInt(json, "duration");
            List<Drop> drops = new ArrayList<>();

            for (JsonElement el : GsonHelper.getAsJsonArray(json, "drops")){
                JsonObject o = el.getAsJsonObject();
                ResourceLocation item = new ResourceLocation(GsonHelper.getAsString(o, "item"));
                double chance = GsonHelper.getAsDouble(o, "chance");
                int min = GsonHelper.getAsInt(o, "min", 1);
                int max = GsonHelper.getAsInt(o, "max", 1);
                EnvFilter filter = o.has("env") ? parseEnvFilter(GsonHelper.getAsJsonObject(o, "env")) : EnvFilter.NONE;
                drops.add(new Drop(item, chance, min, max, filter));
            }
            return new DrillCoreRecipe(id, tierId, duration, drops);
        }

        @Override
        public DrillCoreRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf){
            ResourceLocation tierId = buf.readResourceLocation();
            int duration = buf.readVarInt();
            int n = buf.readVarInt();
            List<Drop> drops = new ArrayList<>(n);
            for (int i = 0; i < n; i++){
                ResourceLocation item = buf.readResourceLocation();
                double chance = buf.readDouble();
                int min = buf.readVarInt();
                int max = buf.readVarInt();

                int dCount = buf.readVarInt();
                Set<ResourceLocation> dims = new HashSet<>();
                for (int j = 0; j < dCount; j++) dims.add(buf.readResourceLocation());

                int bCount = buf.readVarInt();
                Set<ResourceLocation> biomeIds = new HashSet<>();
                for (int j = 0; j < bCount; j++) biomeIds.add(buf.readResourceLocation());

                int tCount = buf.readVarInt();
                Set<ResourceLocation> biomeTags = new HashSet<>();
                for (int j = 0; j < tCount; j++) biomeTags.add(buf.readResourceLocation());

                drops.add(new Drop(item, chance, min, max, new EnvFilter(dims, biomeIds, biomeTags)));
            }
            return new DrillCoreRecipe(id, tierId, duration, drops);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, DrillCoreRecipe rec){
            buf.writeResourceLocation(rec.tierId);
            buf.writeVarInt(rec.durationTicks);
            buf.writeVarInt(rec.drops.size());
            for (Drop d : rec.drops){
                buf.writeResourceLocation(d.item());
                buf.writeDouble(d.chance());
                buf.writeVarInt(d.min());
                buf.writeVarInt(d.max());

                var f = d.filter();
                Set<ResourceLocation> dims = f == null ? Set.of() : f.dimensions();
                Set<ResourceLocation> biomeIds = f == null ? Set.of() : f.biomeIds();
                Set<ResourceLocation> biomeTags = f == null ? Set.of() : f.biomeTagIds();

                buf.writeVarInt(dims.size());      dims.forEach(buf::writeResourceLocation);
                buf.writeVarInt(biomeIds.size());  biomeIds.forEach(buf::writeResourceLocation);
                buf.writeVarInt(biomeTags.size()); biomeTags.forEach(buf::writeResourceLocation);
            }
        }
    }
}