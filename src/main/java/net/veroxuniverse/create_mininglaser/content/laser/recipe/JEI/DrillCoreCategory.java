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
import net.veroxuniverse.create_mininglaser.content.items.TierDef;
import net.veroxuniverse.create_mininglaser.content.laser.recipe.DrillCoreRecipe;
import net.veroxuniverse.create_mininglaser.registry.ModConfigs;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DrillCoreCategory implements IRecipeCategory<DrillCoreRecipe> {
    private static final ResourceLocation BG = new ResourceLocation("create_mininglaser", "textures/gui/jei/drill_core.png");
    private final IDrawable background;
    private final IDrawable icon;

    private static final int TOOLTIP_SECTION_LIMIT = 6;

    public DrillCoreCategory(IDrawable background, IDrawable icon) {
        this.background = background;
        this.icon = icon;
    }

    @Override public RecipeType<DrillCoreRecipe> getRecipeType() { return DrillCoreJei.TYPE; }
    @Override public Component getTitle() { return Component.literal("Laser Drill"); }
    @Override public IDrawable getBackground() { return background; }
    @Override public IDrawable getIcon() { return icon; }

    private static String titleCase(String s) {
        StringBuilder out = new StringBuilder(s.length());
        boolean cap = true;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (cap && Character.isLetter(c)) {
                out.append(Character.toTitleCase(c));
                cap = false;
            } else {
                out.append(c);
            }
            if (c == ' ' || c == '-' || c == '_' || c == '/' || c == '.')
                cap = true;
        }
        return out.toString();
    }

    private static Component prettyDimension(ResourceLocation id) {
        if ("minecraft".equals(id.getNamespace())) {
            switch (id.getPath()) {
                case "overworld":  return Component.literal("Overworld");
                case "the_nether": return Component.literal("The Nether");
                case "the_end":    return Component.literal("The End");
            }
        }
        String key = "dimension." + id.getNamespace() + "." + id.getPath();
        Component trans = Component.translatable(key);
        String raw = trans.getString();
        if (!raw.equals(key)) return trans;

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

    private static Component prettyBiome(ResourceLocation biomeId) {
        Component trans = Component.translatable("biome." + biomeId.getNamespace() + "." + biomeId.getPath());
        String raw = trans.getString();
        if (!raw.equals("biome." + biomeId.getNamespace() + "." + biomeId.getPath()))
            return trans;

        String nice = titleCase(biomeId.getPath().replace('_', ' '));
        if (!"minecraft".equals(biomeId.getNamespace()))
            nice = biomeId.getNamespace() + ": " + nice;
        return Component.literal(nice);
    }

    private static Component prettyTag(ResourceLocation tagId) {
        String nice = titleCase(tagId.getPath().replace('_', ' '));
        if (!"minecraft".equals(tagId.getNamespace()))
            return Component.literal("#" + tagId.getNamespace() + ":" + nice);
        return Component.literal("#" + nice);
    }

    private static void addWrappedList(List<Component> tooltip, Component title, List<Component> lines, int limit) {
        if (lines.isEmpty()) return;
        tooltip.add(title.copy().withStyle(ChatFormatting.GRAY));
        int shown = 0;
        for (Component c : lines) {
            if (shown >= limit) break;
            tooltip.add(Component.literal("  ").append(c).withStyle(ChatFormatting.DARK_GREEN));
            shown++;
        }
        if (lines.size() > limit) {
            int more = lines.size() - limit;
            tooltip.add(Component.literal("  … +" + more + " more").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }

    private static String formatSU(double su) {
        NumberFormat nf = NumberFormat.getIntegerInstance(Locale.US);
        return nf.format(Math.round(su));
    }

    private static String formatChance(double chance) {
        double pct = chance * 100.0;
        if (Math.abs(pct - Math.round(pct)) < 1e-6) return String.format(Locale.US, "%.0f%%", pct);
        return String.format(Locale.US, "%.1f%%", pct);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder b, DrillCoreRecipe r, IFocusGroup focuses) {
        final TierDef def = r.getTierDef();
        if (def == null) return;

        var coreItem = ForgeRegistries.ITEMS.getValue(def.coreItem);
        var coreStack = coreItem != null ? new ItemStack(coreItem) : ItemStack.EMPTY;

        b.addSlot(mezz.jei.api.recipe.RecipeIngredientRole.INPUT, 20, 20)
                .addItemStack(coreStack)
                .addTooltipCallback((view, tooltip) -> {
                    double su128 = def.stress_at_minRPM * ModConfigs.COMMON.suScale.get();
                    tooltip.add(Component.literal("Stress @" + def.minRpm + " RPM: " + formatSU(su128) + " SU")
                            .withStyle(ChatFormatting.WHITE));
                    tooltip.add(Component.literal("Min Speed: " + def.minRpm + " RPM")
                            .withStyle(ChatFormatting.GRAY));
                });

        int x = 60;
        int y = 20;
        for (var drop : r.getDrops()) {
            b.addSlot(mezz.jei.api.recipe.RecipeIngredientRole.OUTPUT, x, y)
                    .addItemStack(new ItemStack(ForgeRegistries.ITEMS.getValue(drop.item())))
                    .addTooltipCallback((view, tooltip) -> {
                        tooltip.add(Component.literal("Chance: " + formatChance(drop.chance()))
                                .withStyle(ChatFormatting.WHITE));
                        if (drop.min() != drop.max())
                            tooltip.add(Component.literal("Amount: " + drop.min() + "–" + drop.max())
                                    .withStyle(ChatFormatting.WHITE));

                        var f = drop.filter();
                        if (f != null && !f.isEmpty()) {
                            if (!f.dimensions().isEmpty()) {
                                List<Component> dims = new ArrayList<>();
                                for (ResourceLocation rl : f.dimensions()) dims.add(prettyDimension(rl));
                                addWrappedList(tooltip, Component.literal("Dimensions:"), dims, TOOLTIP_SECTION_LIMIT);
                            }
                            if (!f.biomeIds().isEmpty()) {
                                List<Component> biomes = new ArrayList<>();
                                for (ResourceLocation rl : f.biomeIds()) biomes.add(prettyBiome(rl));
                                addWrappedList(tooltip, Component.literal("Biomes:"), biomes, TOOLTIP_SECTION_LIMIT);
                            }
                            if (!f.biomeTagIds().isEmpty()) {
                                List<Component> tags = new ArrayList<>();
                                for (ResourceLocation rl : f.biomeTagIds()) tags.add(prettyTag(rl));
                                addWrappedList(tooltip, Component.literal("Biome Tags:"), tags, TOOLTIP_SECTION_LIMIT);
                            }
                        }
                    });
            x += 20;
        }
    }

    @Override
    public void draw(DrillCoreRecipe r, IRecipeSlotsView v, GuiGraphics g, double mx, double my) {
        var font = Minecraft.getInstance().font;

        final TierDef def = r.getTierDef();
        if (def == null) return;

        double su128 = def.stress_at_minRPM * ModConfigs.COMMON.suScale.get();
        String timeText = String.format(Locale.US, "%.1f s", r.getDurationTicks() / 20.0);
        String suText   = formatSU(su128) + " SU";
        String rpmText  = "≥" + def.minRpm + " RPM";

        int topY = 6;
        g.drawString(font, timeText, 8,  topY, 0xFFFFFF, false);
        g.drawString(font, suText,   80, topY, 0xFFFFFF, false);

        int inputX = 20, inputY = 20;
        int rpmX = inputX + 9 - font.width(rpmText) / 2;
        int rpmY = inputY + 18 + 6;
        g.drawString(font, rpmText, rpmX, rpmY, 0xAAAAAA, false);
    }
}
