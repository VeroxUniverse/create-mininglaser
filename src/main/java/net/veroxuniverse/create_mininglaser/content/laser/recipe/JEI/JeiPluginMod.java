package net.veroxuniverse.create_mininglaser.content.laser.recipe.JEI;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.veroxuniverse.create_mininglaser.CreateMininglaser;
import net.veroxuniverse.create_mininglaser.content.laser.recipe.DrillCoreRecipe;
import net.veroxuniverse.create_mininglaser.registry.ModBlocks;
import net.veroxuniverse.create_mininglaser.registry.ModRecipes;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

@JeiPlugin
public class JeiPluginMod implements IModPlugin {
    private static final ResourceLocation UID = new ResourceLocation("create_mininglaser", "jei_plugin");

    private static IJeiRuntime RUNTIME;

    @Override public ResourceLocation getPluginUid() { return UID; }

    @Override
    public void registerCategories(IRecipeCategoryRegistration reg) {
        var gh = reg.getJeiHelpers().getGuiHelper();

        ResourceLocation BG_TEX = new ResourceLocation(
                CreateMininglaser.MODID,
                "textures/gui/jei/laser_drill_backround.png"
        );

        IDrawable background = gh.drawableBuilder(BG_TEX, 0, 0, 180, 100).build();

        reg.addRecipeCategories(new DrillCoreCategory(
                background,
                gh.createDrawableItemStack(new ItemStack(ModBlocks.LASER_DRILL.get()))
        ));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration reg) {
        reg.addRecipeCatalyst(new ItemStack(ModBlocks.LASER_DRILL.get()), DrillCoreJei.TYPE);
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registry) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        registry.addRecipes(DrillCoreJei.TYPE, collectRecipes(mc.level.getRecipeManager()));
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        RUNTIME = runtime;
    }

    public static void reloadJeiRecipes() {
        if (RUNTIME == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        var rm = mc.level.getRecipeManager();
        var list = collectRecipes(rm);

        var mgr = RUNTIME.getRecipeManager();
        mgr.hideRecipeCategory(DrillCoreJei.TYPE);
        mgr.unhideRecipeCategory(DrillCoreJei.TYPE);

        mgr.unhideRecipes(DrillCoreJei.TYPE, list);
    }

    private static List<DrillCoreRecipe> collectRecipes(RecipeManager manager) {
        Collection<Recipe<?>> all = manager.getRecipes();
        return all.stream()
                .filter(r -> r.getType() == ModRecipes.DRILL_CORE_TYPE.get())
                .map(r -> (DrillCoreRecipe) r)
                .toList();
    }
}
