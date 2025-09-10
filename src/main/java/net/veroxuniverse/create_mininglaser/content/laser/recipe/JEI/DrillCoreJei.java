package net.veroxuniverse.create_mininglaser.content.laser.recipe.JEI;

import mezz.jei.api.recipe.RecipeType;
import net.veroxuniverse.create_mininglaser.content.laser.recipe.DrillCoreRecipe;

public class DrillCoreJei {
    public static final RecipeType<DrillCoreRecipe> TYPE =
            RecipeType.create("create_mininglaser", "drill_core", DrillCoreRecipe.class);
}
