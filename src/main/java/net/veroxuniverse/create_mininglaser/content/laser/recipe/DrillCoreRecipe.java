package net.veroxuniverse.create_mininglaser.content.laser.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
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
import net.veroxuniverse.create_mininglaser.content.items.DrillTier;
import net.veroxuniverse.create_mininglaser.registry.ModRecipes;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DrillCoreRecipe implements Recipe<Container> {

    public record EnvFilter(
            java.util.Set<ResourceLocation> dimensions,
            java.util.Set<ResourceLocation> biomeIds,
            java.util.Set<ResourceLocation> biomeTagIds
    ) {
        public static final EnvFilter NONE = new EnvFilter(Set.of(), Set.of(), Set.of());
        public boolean isEmpty() {
            return dimensions.isEmpty() && biomeIds.isEmpty() && biomeTagIds.isEmpty();
        }
    }

    public record Drop(ResourceLocation item, double chance, int min, int max, EnvFilter filter) {}

    private final ResourceLocation id;
    private final DrillTier tier;
    private final int durationTicks;
    private final List<Drop> drops;

    public DrillCoreRecipe(ResourceLocation id, DrillTier tier, int durationTicks, List<Drop> drops) {
        this.id = id;
        this.tier = tier;
        this.durationTicks = durationTicks;
        this.drops = List.copyOf(drops);
    }

    public DrillTier getTier() { return tier; }
    public int getDurationTicks(){ return durationTicks; }
    public List<Drop> getDrops() { return drops; }

    private static boolean envMatches(Level level, BlockPos pos, EnvFilter f) {
        if (f == null || f.isEmpty()) return true;

        if (!f.dimensions().isEmpty()) {
            ResourceLocation dimId = level.dimension().location();
            if (!f.dimensions().contains(dimId)) return false;
        }

        if (!f.biomeIds().isEmpty() || !f.biomeTagIds().isEmpty()) {
            var biomeHolder = level.getBiome(pos);
            if (!f.biomeIds().isEmpty()) {
                ResourceLocation biomeId = level.registryAccess()
                        .registryOrThrow(net.minecraft.core.registries.Registries.BIOME)
                        .getKey(biomeHolder.value());
                if (biomeId == null || !f.biomeIds().contains(biomeId)) return false;
            }
            if (!f.biomeTagIds().isEmpty()) {
                var biomeReg = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME);
                for (ResourceLocation tagId : f.biomeTagIds()) {
                    var tagKey = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BIOME, tagId);
                    if (biomeHolder.is(tagKey)) {
                        return true; // passt über Tag
                    }
                }
                if (!f.biomeTagIds().isEmpty()) return false;
            }
        }
        return true;
    }

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

    @Override public boolean matches(Container c, Level l){ return false; }
    @Override public ItemStack assemble(Container c, RegistryAccess a){ return ItemStack.EMPTY; }
    @Override public boolean canCraftInDimensions(int w, int h){ return false; }
    @Override public ItemStack getResultItem(RegistryAccess a){ return ItemStack.EMPTY; }
    @Override public ResourceLocation getId(){ return id; }
    @Override public RecipeSerializer<?> getSerializer(){ return ModRecipes.DRILL_CORE_SERIALIZER.get(); }
    @Override public RecipeType<?> getType(){ return ModRecipes.DRILL_CORE_TYPE.get(); }

    public static class Serializer implements RecipeSerializer<DrillCoreRecipe> {

        private static EnvFilter parseEnvFilter(JsonObject o) {
            java.util.Set<ResourceLocation> dims = new java.util.HashSet<>();
            java.util.Set<ResourceLocation> biomeIds = new java.util.HashSet<>();
            java.util.Set<ResourceLocation> biomeTags = new java.util.HashSet<>();

            if (o.has("dimensions")) {
                for (JsonElement e : GsonHelper.getAsJsonArray(o, "dimensions")) {
                    dims.add(new ResourceLocation(GsonHelper.convertToString(e, "dimension")));
                }
            }
            if (o.has("biomes")) {
                for (JsonElement e : GsonHelper.getAsJsonArray(o, "biomes")) {
                    String s = GsonHelper.convertToString(e, "biome");
                    if (s.startsWith("#"))
                        biomeTags.add(new ResourceLocation(s.substring(1)));
                    else
                        biomeIds.add(new ResourceLocation(s));
                }
            }
            if (dims.isEmpty() && biomeIds.isEmpty() && biomeTags.isEmpty())
                return EnvFilter.NONE;
            return new EnvFilter(dims, biomeIds, biomeTags);
        }

        @Override
        public DrillCoreRecipe fromJson(ResourceLocation id, JsonObject json){
            int tierLvl = GsonHelper.getAsInt(json, "tier");
            int duration = GsonHelper.getAsInt(json, "duration");
            List<Drop> drops = new ArrayList<>();

            for (JsonElement el : GsonHelper.getAsJsonArray(json, "drops")){
                JsonObject o = el.getAsJsonObject();
                ResourceLocation item = new ResourceLocation(GsonHelper.getAsString(o, "item"));
                double chance = GsonHelper.getAsDouble(o, "chance");
                int min = GsonHelper.getAsInt(o, "min", 1);
                int max = GsonHelper.getAsInt(o, "max", 1);
                EnvFilter filter = o.has("env") ? parseEnvFilter(GsonHelper.getAsJsonObject(o, "env"))
                        : EnvFilter.NONE;
                drops.add(new Drop(item, chance, min, max, filter));
            }
            return new DrillCoreRecipe(id, DrillTier.fromLevel(tierLvl), duration, drops);
        }

        @Override
        public DrillCoreRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf){
            int tierLvl = buf.readVarInt();
            int duration = buf.readVarInt();
            int n = buf.readVarInt();
            List<Drop> drops = new ArrayList<>(n);
            for (int i = 0; i < n; i++){
                ResourceLocation item = buf.readResourceLocation();
                double chance = buf.readDouble();
                int min = buf.readVarInt();
                int max = buf.readVarInt();

                // env
                int dCount = buf.readVarInt();
                java.util.Set<ResourceLocation> dims = new java.util.HashSet<>();
                for (int j = 0; j < dCount; j++) dims.add(buf.readResourceLocation());

                int bCount = buf.readVarInt();
                java.util.Set<ResourceLocation> biomeIds = new java.util.HashSet<>();
                for (int j = 0; j < bCount; j++) biomeIds.add(buf.readResourceLocation());

                int tCount = buf.readVarInt();
                java.util.Set<ResourceLocation> biomeTags = new java.util.HashSet<>();
                for (int j = 0; j < tCount; j++) biomeTags.add(buf.readResourceLocation());

                drops.add(new Drop(item, chance, min, max, new EnvFilter(dims, biomeIds, biomeTags)));
            }
            return new DrillCoreRecipe(id, DrillTier.fromLevel(tierLvl), duration, drops);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, DrillCoreRecipe rec){
            buf.writeVarInt(rec.tier.level);
            buf.writeVarInt(rec.durationTicks);
            buf.writeVarInt(rec.drops.size());
            for (Drop d : rec.drops){
                buf.writeResourceLocation(d.item());
                buf.writeDouble(d.chance());
                buf.writeVarInt(d.min());
                buf.writeVarInt(d.max());

                // env
                EnvFilter f = d.filter();
                java.util.Set<ResourceLocation> dims = f == null ? java.util.Set.of() : f.dimensions();
                java.util.Set<ResourceLocation> biomeIds = f == null ? java.util.Set.of() : f.biomeIds();
                java.util.Set<ResourceLocation> biomeTags = f == null ? java.util.Set.of() : f.biomeTagIds();

                buf.writeVarInt(dims.size());       dims.forEach(buf::writeResourceLocation);
                buf.writeVarInt(biomeIds.size());   biomeIds.forEach(buf::writeResourceLocation);
                buf.writeVarInt(biomeTags.size());  biomeTags.forEach(buf::writeResourceLocation);
            }
        }
    }
}
