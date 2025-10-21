package net.veroxuniverse.create_mininglaser.content.laser.recipe.JEI;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.veroxuniverse.create_mininglaser.content.laser.recipe.DrillCoreRecipe;
import net.veroxuniverse.create_mininglaser.registry.ModBlocks;
import net.veroxuniverse.create_mininglaser.registry.ModRecipes;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

@JeiPlugin
public class JeiPluginMod implements IModPlugin {
    private static final ResourceLocation UID = new ResourceLocation("create_mininglaser", "jei_plugin");

    @Override public ResourceLocation getPluginUid() { return UID; }

    @Override
    public void registerCategories(IRecipeCategoryRegistration reg) {
        var gh = reg.getJeiHelpers().getGuiHelper();

        final int COLS      = 5;
        final int SLOT      = 18;
        final int START_X   = 60;
        final int START_Y   = 20;
        final int ROWS_MAX  = 2;
        final int PAD_RIGHT = 12;
        final int PAD_BOTTOM= 22;

        int width  = START_X + COLS * SLOT + PAD_RIGHT;
        int height = START_Y + ROWS_MAX * SLOT + PAD_BOTTOM;

        reg.addRecipeCategories(new DrillCoreCategory(
                gh.createBlankDrawable(width, height),
                gh.createDrawableItemStack(new ItemStack(ModBlocks.LASER_DRILL.get()))
        ));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration reg) {
        reg.addRecipeCatalyst(new ItemStack(ModBlocks.LASER_DRILL.get()), DrillCoreJei.TYPE);
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registry) {
        var mc = Minecraft.getInstance();
        if (mc.level == null) return;

        RecipeManager manager = mc.level.getRecipeManager();
        Collection<Recipe<?>> all = manager.getRecipes();

        List<DrillCoreRecipe> recipes = all.stream()
                .filter(r -> r.getType() == ModRecipes.DRILL_CORE_TYPE.get())
                .map(r -> (DrillCoreRecipe) r)
                .toList();

        registry.addRecipes(DrillCoreJei.TYPE, recipes);
    }
}
