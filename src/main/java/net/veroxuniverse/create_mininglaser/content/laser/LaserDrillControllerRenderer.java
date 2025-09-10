package net.veroxuniverse.create_mininglaser.content.laser;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.veroxuniverse.create_mininglaser.content.blocks.LaserDrillControllerBlock;
import net.veroxuniverse.create_mininglaser.content.blocks.LaserDrillControllerBlockEntity;
import net.veroxuniverse.create_mininglaser.content.items.DrillCoreItem;
import net.veroxuniverse.create_mininglaser.content.items.DrillTier;
import net.veroxuniverse.create_mininglaser.registry.ModPartials;

public class LaserDrillControllerRenderer
        extends KineticBlockEntityRenderer<LaserDrillControllerBlockEntity> {

    public LaserDrillControllerRenderer(BlockEntityRendererProvider.Context ctx) { super(ctx); }

    @Override
    protected void renderSafe(LaserDrillControllerBlockEntity be, float pt,
                              PoseStack ms, MultiBufferSource buffers, int light, int overlay) {

        BlockState state = be.getBlockState();
        RenderType rt = getRenderType(be, state);

        SuperByteBuffer shaft = CachedBuffers.partial(ModPartials.SHAFT_MODEL, state);
        renderRotatingBuffer(be, shaft, ms, buffers.getBuffer(rt), light);

        var core = be.getCore();
        if (core.isEmpty() || !(core.getItem() instanceof DrillCoreItem dci))
            return;

        DrillTier tier = dci.getTier();

        PartialModel head = switch (tier) {
            case T1 -> ModPartials.LASER_HEAD_T1;
            case T2 -> ModPartials.LASER_HEAD_T2;
            case T3 -> ModPartials.LASER_HEAD_T3;
            case T4 -> ModPartials.LASER_HEAD_T4;
            case T5 -> ModPartials.LASER_HEAD_T5;
        };

        SuperByteBuffer headBuf = CachedBuffers.partial(head, state);

        Direction facing = state.getValue(LaserDrillControllerBlock.HORIZONTAL_FACING);
        int yRot = switch (facing) {
            case SOUTH -> 0;
            case WEST  -> 90;
            case NORTH -> 180;
            case EAST  -> 270;
            default    -> 0;
        };

        ms.pushPose();
        ms.translate(0.0, -1.0, 0.0);
        headBuf.light(light);
        headBuf.rotateCentered((float) Math.toRadians(yRot), Direction.UP);
        headBuf.renderInto(ms, buffers.getBuffer(rt));
        ms.popPose();
    }

}
