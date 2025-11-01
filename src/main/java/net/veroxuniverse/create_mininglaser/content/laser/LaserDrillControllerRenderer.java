package net.veroxuniverse.create_mininglaser.content.laser;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.veroxuniverse.create_mininglaser.content.blocks.LaserDrillControllerBlock;
import net.veroxuniverse.create_mininglaser.content.blocks.LaserDrillControllerBlockEntity;
import net.veroxuniverse.create_mininglaser.registry.ModPartials;
import org.jetbrains.annotations.NotNull;

public class LaserDrillControllerRenderer extends KineticBlockEntityRenderer<LaserDrillControllerBlockEntity> {

    public LaserDrillControllerRenderer(BlockEntityRendererProvider.Context ctx) { super(ctx); }

    @Override
    protected void renderSafe(LaserDrillControllerBlockEntity be, float pt,
                              PoseStack ms, MultiBufferSource buffers, int light, int overlay) {

        BlockState state = be.getBlockState();
        RenderType rt = getRenderType(be, state);

        SuperByteBuffer shaft = CachedBuffers.partial(ModPartials.SHAFT_MODEL, state);
        renderRotatingBuffer(be, shaft, ms, buffers.getBuffer(rt), light);

        if (be.getCore().isEmpty()) return;
        var tier = be.getActiveTier();
        if (tier == null || tier.headPartial == null) return;

        PartialModel head = PartialModel.of(tier.headPartial);
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

        if (be.isLaserActiveClient()) {
            renderColoredBeam(be, pt, ms, buffers);
        }
    }

    private void renderColoredBeam(LaserDrillControllerBlockEntity be, float pt, PoseStack ms, MultiBufferSource buffers) {
        Level level = be.getLevel();
        if (level == null) return;

        double length = computeBeamLength(level, be.getBlockPos(), 32, 128);
        if (length <= 0.01) return;

        long gameTime = level.getGameTime();
        float f = (float) Math.floorMod(gameTime, 40) + pt;

        ms.pushPose();
        ms.translate(0.5f, 0f, 0.5f);
        ms.mulPose(Axis.YP.rotationDegrees(f * 2.25f - 45f));

        VertexConsumer vc = buffers.getBuffer(RenderType.gui());

        int color = 0xC8FF4040;

        float width = 0.20f;

        renderBeam(vc, ms, 0f, 0f, 0f, color, (float) length, width);

        ms.popPose();
    }

    private static double computeBeamLength(Level level, BlockPos origin, int fallbackMax, int hardCap) {
        int yStart = origin.getY() - 1;
        int yMin = Math.max(level.getMinBuildHeight(), origin.getY() - 64);
        int stopY = yStart;

        for (int y = yStart; y >= yMin; y--) {
            BlockPos p = new BlockPos(origin.getX(), y, origin.getZ());
            if (!level.getBlockState(p).isAir()) {
                stopY = y + 1;
                break;
            }
            stopY = yMin;
        }

        double top = origin.getY() - 0.5;
        double bottom = stopY + 0.5;
        double len = top - bottom;
        if (len <= 0) return fallbackMax;
        return Math.min(len, hardCap);
    }

    private static void renderBeam(VertexConsumer consumer, PoseStack pose, float cx, float cy, float cz, int color, float length, float width) {
        for (int i = 0; i < 4; i++) {
            pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees(90f * i));
            pose.pushPose();
            pose.translate(-width / 2f, 0f, -width / 2f);
            renderQuad(consumer, pose, cx, cy, cz, color, length, width);
            pose.popPose();
            pose.popPose();
        }
    }

    private static void renderQuad(VertexConsumer consumer, PoseStack pose, float x, float y, float z, int color, float length, float width) {
        var last = pose.last();

        // Top-Left
        consumer.vertex(last.pose(), x, y, z)
                .color(color)
                .uv(0f, 0f)
                .overlayCoords(0, 10)
                .uv2(0x00F000F0)
                .normal(0f, 0f, 0f)
                .endVertex();

        // Top-Right
        consumer.vertex(last.pose(), x + width, y, z)
                .color(color)
                .uv(1f, 0f)
                .overlayCoords(0, 10)
                .uv2(0x00F000F0)
                .normal(0f, 0f, 0f)
                .endVertex();

        // Bottom-Right
        consumer.vertex(last.pose(), x + width, y - length, z)
                .color(color)
                .uv(1f, 1f)
                .overlayCoords(0, 10)
                .uv2(0x00F000F0)
                .normal(0f, 0f, 0f)
                .endVertex();

        // Bottom-Left
        consumer.vertex(last.pose(), x, y - length, z)
                .color(color)
                .uv(0f, 1f)
                .overlayCoords(0, 10)
                .uv2(0x00F000F0)
                .normal(0f, 0f, 0f)
                .endVertex();
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull LaserDrillControllerBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return Minecraft.getInstance().options.getEffectiveRenderDistance() * 16 + 64;
    }

    @Override
    public boolean shouldRender(@NotNull LaserDrillControllerBlockEntity blockEntity, @NotNull Vec3 cameraPos) {
        return true;
    }

}