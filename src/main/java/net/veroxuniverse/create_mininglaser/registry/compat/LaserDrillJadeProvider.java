package net.veroxuniverse.create_mininglaser.registry.compat;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.veroxuniverse.create_mininglaser.content.blocks.LaserDrillControllerBlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum LaserDrillJadeProvider implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        BlockEntity be = accessor.getBlockEntity();
        if (!(be instanceof LaserDrillControllerBlockEntity drill)) return;

        ItemStack core = drill.getCore();
        if (core.isEmpty()) {
            tooltip.add(Component.literal("No Core inserted").withStyle(ChatFormatting.GRAY));
            return;
        }

        tooltip.add(Component.literal("Core: ")
                .withStyle(ChatFormatting.GRAY)
                .append(core.getHoverName().copy().withStyle(ChatFormatting.AQUA)));
    }

    @Override
    public ResourceLocation getUid() {
        return new ResourceLocation("create_mininglaser", "laser_drill_core");
    }
}
