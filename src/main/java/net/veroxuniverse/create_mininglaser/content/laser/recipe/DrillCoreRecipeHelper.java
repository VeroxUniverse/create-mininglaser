package net.veroxuniverse.create_mininglaser.content.laser.recipe;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.veroxuniverse.create_mininglaser.content.items.TierDef;
import net.veroxuniverse.create_mininglaser.registry.ModRecipes;

public class DrillCoreRecipeHelper {
    public static DrillCoreRecipe findRecipe(RecipeManager mgr, TierDef tier){
        if (tier == null) return null;
        for (Recipe<?> r : mgr.getRecipes()) {
            if (r.getType() == ModRecipes.DRILL_CORE_TYPE.get()
                    && r instanceof DrillCoreRecipe d
                    && tier.id.equals(d.getTierId())) {
                return d;
            }
        }
        return null;
    }
}

