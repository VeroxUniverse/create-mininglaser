package net.veroxuniverse.create_mininglaser.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.veroxuniverse.create_mininglaser.CreateMininglaser;
import net.veroxuniverse.create_mininglaser.content.laser.recipe.DrillCoreRecipe;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, CreateMininglaser.MODID);
    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, CreateMininglaser.MODID);

    public static final RegistryObject<RecipeSerializer<DrillCoreRecipe>> DRILL_CORE_SERIALIZER =
            SERIALIZERS.register("drill_core", DrillCoreRecipe.Serializer::new);

    public static final RegistryObject<RecipeType<DrillCoreRecipe>> DRILL_CORE_TYPE =
            TYPES.register("drill_core", () -> RecipeType.simple(new ResourceLocation(CreateMininglaser.MODID, "drill_core")));
}

