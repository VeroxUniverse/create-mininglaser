package net.veroxuniverse.create_mininglaser.content.laser.recipe;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.veroxuniverse.create_mininglaser.content.items.DrillTier;
import net.veroxuniverse.create_mininglaser.registry.ModRecipes;

public class DrillCoreRecipeHelper {
    public static DrillCoreRecipe findRecipe(RecipeManager mgr, DrillTier tier){
        for (Recipe<?> r : mgr.getRecipes()) {
            if (r.getType() == ModRecipes.DRILL_CORE_TYPE.get() &&
                    r instanceof DrillCoreRecipe d &&
                    d.getTier() == tier) {
                return d;
            }
        }
        return null;
    }
}
