package net.veroxuniverse.create_mininglaser.content.laser.recipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.veroxuniverse.create_mininglaser.content.items.TierDef;
import net.veroxuniverse.create_mininglaser.registry.ModRecipes;

import java.util.ArrayList;
import java.util.List;

public class DrillCoreRecipeHelper {

    public static List<DrillCoreRecipe> findAllByTier(RecipeManager mgr, TierDef tier) {
        List<DrillCoreRecipe> out = new ArrayList<>();
        for (Recipe<?> r : mgr.getRecipes()) {
            if (r.getType() == ModRecipes.DRILL_CORE_TYPE.get()
                    && r instanceof DrillCoreRecipe d
                    && d.getTierDef() != null
                    && d.getTierDef().id.equals(tier.id)) {
                out.add(d);
            }
        }
        return out;
    }

    public static DrillCoreRecipe mergeForTier(RecipeManager mgr, TierDef tier) {
        List<DrillCoreRecipe> list = findAllByTier(mgr, tier);
        if (list.isEmpty()) return null;

        int duration = list.get(0).getDurationTicks();

        List<DrillCoreRecipe.Drop> mergedDrops = new ArrayList<>();
        for (DrillCoreRecipe rec : list) {
            mergedDrops.addAll(rec.getDrops());
        }

        ResourceLocation id = new ResourceLocation("create_mininglaser", "merged/" + tier.id.getPath());
        return new DrillCoreRecipe(id, tier.id, duration, mergedDrops);
    }
}
