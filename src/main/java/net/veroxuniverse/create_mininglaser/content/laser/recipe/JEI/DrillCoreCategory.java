package net.veroxuniverse.create_mininglaser.content.laser.recipe.JEI;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.veroxuniverse.create_mininglaser.content.laser.recipe.DrillCoreRecipe;
import net.veroxuniverse.create_mininglaser.registry.ModConfigs;
import net.veroxuniverse.create_mininglaser.registry.ModItems;

import java.util.ArrayList;
import java.util.List;

public class DrillCoreCategory implements IRecipeCategory<DrillCoreRecipe> {
    private static final ResourceLocation BG = new ResourceLocation("create_mininglaser", "textures/gui/jei/drill_core.png");
    private final IDrawable background;
    private final IDrawable icon;

    public DrillCoreCategory(IDrawable background, IDrawable icon) {
        this.background = background;
        this.icon = icon;
    }

    @Override public RecipeType<DrillCoreRecipe> getRecipeType() { return DrillCoreJei.TYPE; }
    @Override public Component getTitle() { return Component.literal("Laser Drill"); }
    @Override public IDrawable getBackground() { return background; }
    @Override public IDrawable getIcon() { return icon; }

    private static Component prettyDimension(ResourceLocation id) {
        String key = "dimension." + id.getNamespace() + "." + id.getPath();
        Component trans = Component.translatable(key);

        String raw = trans.getString();
        if (raw.equals(key)) {
            String nice = id.getPath()
                    .replace('_', ' ')
                    .replace('/', ' ')
                    .replace('.', ' ')
                    .replace('-', ' ');
            nice = titleCase(nice);
            if (!"minecraft".equals(id.getNamespace()))
                nice = id.getNamespace() + ": " + nice;
            return Component.literal(nice);
        }
        return trans;
    }

    private static String titleCase(String s) {
        StringBuilder out = new StringBuilder(s.length());
        boolean capitalizeNext = true;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (capitalizeNext && Character.isLetter(c)) {
                out.append(Character.toTitleCase(c));
                capitalizeNext = false;
            } else {
                out.append(c);
            }
            if (c == ' ' || c == '-' || c == '_' || c == '/' || c == '.')
                capitalizeNext = true;
        }
        return out.toString();
    }

    private static Component prettyBiome(ResourceLocation biomeId) {
        return Component.translatable("biome." + biomeId.getNamespace() + "." + biomeId.getPath());
    }

    private static Component prettyTag(ResourceLocation tagId) {
        String nice = "#" + tagId.getPath().replace('_', ' ');
        if (!"minecraft".equals(tagId.getNamespace()))
            nice = "#" + tagId.getNamespace() + ":" + tagId.getPath().replace('_', ' ');
        return Component.literal(nice);
    }

    private static String capitalizeWords(String s) {
        String[] parts = s.split("\\s+");
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String p = parts[i];
            if (!p.isEmpty()) {
                out.append(Character.toUpperCase(p.charAt(0)));
                if (p.length() > 1) out.append(p.substring(1));
            }
            if (i + 1 < parts.length) out.append(' ');
        }
        return out.toString();
    }

    private static void addWrappedList(List<Component> tooltip, Component title, List<Component> lines) {
        if (lines.isEmpty()) return;
        tooltip.add(title.copy().withStyle(ChatFormatting.GRAY));
        for (Component c : lines) {
            tooltip.add(Component.literal("  ").append(c).withStyle(ChatFormatting.DARK_GREEN));
        }
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder b, DrillCoreRecipe r, IFocusGroup focuses) {

        b.addSlot(mezz.jei.api.recipe.RecipeIngredientRole.INPUT, 20, 20)
                .addItemStack(new ItemStack(ModItems.byTier(r.getTier())))
                .addTooltipCallback((view, tooltip) -> {
                    double su = ModConfigs.COMMON.getStressForTier(r.getTier());
                    tooltip.add(Component.literal(String.format("Stress: %.0f SU", su)).withStyle(ChatFormatting.WHITE));
                });

        int x = 60;
        int y = 20;
        for (var drop : r.getDrops()) {
            b.addSlot(mezz.jei.api.recipe.RecipeIngredientRole.OUTPUT, x, y)
                    .addItemStack(new ItemStack(ForgeRegistries.ITEMS.getValue(drop.item())))
                    .addTooltipCallback((view, tooltip) -> {
                        tooltip.add(Component.literal(String.format("Chance: %.0f%%", drop.chance() * 100)).withStyle(ChatFormatting.WHITE));
                        if (drop.min() != drop.max())
                            tooltip.add(Component.literal("Amount: " + drop.min() + "–" + drop.max()).withStyle(ChatFormatting.WHITE));

                        var f = drop.filter();
                        if (f != null && !f.isEmpty()) {
                            if (!f.dimensions().isEmpty()) {
                                List<Component> dims = new ArrayList<>();
                                for (ResourceLocation rl : f.dimensions()) dims.add(prettyDimension(rl));
                                addWrappedList(tooltip, Component.literal("Dimensions:"), dims);
                            }
                            if (!f.biomeIds().isEmpty()) {
                                List<Component> biomes = new ArrayList<>();
                                for (ResourceLocation rl : f.biomeIds()) biomes.add(prettyBiome(rl));
                                addWrappedList(tooltip, Component.literal("Biomes:"), biomes);
                            }
                            if (!f.biomeTagIds().isEmpty()) {
                                List<Component> tags = new ArrayList<>();
                                for (ResourceLocation rl : f.biomeTagIds()) tags.add(prettyTag(rl));
                                addWrappedList(tooltip, Component.literal("Biome Tags:"), tags);
                            }
                        }
                    });
            x += 20;
        }
    }

    @Override
    public void draw(DrillCoreRecipe r, IRecipeSlotsView v, GuiGraphics g, double mx, double my) {
        var font = Minecraft.getInstance().font;

        double su = ModConfigs.COMMON.getStressForTier(r.getTier());
        String timeText = String.format("%.1f s", r.getDurationTicks() / 20.0);
        String suText   = String.format("%.0f SU", su);
        String rpmText  = "≥128 RPM";

        int topY = 6;
        g.drawString(font, timeText, 8,  topY, 0xFFFFFF, false);
        g.drawString(font, suText,   80, topY, 0xFFFFFF, false);

        int inputX = 20, inputY = 20;
        int rpmX = inputX + 9 - font.width(rpmText) / 2;
        int rpmY = inputY + 18 + 6;
        g.drawString(font, rpmText, rpmX, rpmY, 0xAAAAAA, false);
    }
}
