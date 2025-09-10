package net.veroxuniverse.create_mininglaser.content.blocks;

import com.simibubi.create.content.equipment.wrench.WrenchItem;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.veroxuniverse.create_mininglaser.content.items.TierDef;
import net.veroxuniverse.create_mininglaser.content.items.TierDefs;
import net.veroxuniverse.create_mininglaser.registry.ModBlockEntities;
import net.veroxuniverse.create_mininglaser.registry.MultiblockHandler;
import org.jetbrains.annotations.Nullable;

public class LaserDrillControllerBlock extends HorizontalKineticBlock implements EntityBlock {

    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public LaserDrillControllerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(HORIZONTAL_FACING, Direction.NORTH)
                .setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVE);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide && state.getBlock() != newState.getBlock()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof LaserDrillControllerBlockEntity drill) {
                drill.ejectCoreUp();
            }
            MultiblockHandler.unformAtController(level, pos, state, false);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction prefferedSide = this.getPreferredHorizontalFacing(context);
        return prefferedSide != null ? (BlockState)this.defaultBlockState().setValue(HORIZONTAL_FACING, prefferedSide) : super.getStateForPlacement(context);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return Direction.Axis.Y;
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face == Direction.UP;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            ItemStack heldItem = player.getItemInHand(hand);
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof LaserDrillControllerBlockEntity drill)) return InteractionResult.PASS;

            TierDef def = TierDefs.byCoreItem(heldItem.getItem());
            if (def != null && drill.getCore().isEmpty()) {
                if (!state.getValue(LaserDrillControllerBlock.ACTIVE)) {
                    player.displayClientMessage(Component.literal("§cController is not active."), true);
                    return InteractionResult.SUCCESS;
                }
                ItemStack one = heldItem.split(1);
                drill.setCore(one);
                player.displayClientMessage(Component.literal("§aCore inserted"), true);
                return InteractionResult.SUCCESS;
            }

            if (heldItem.is(Items.DIAMOND_SHOVEL) && !drill.getCore().isEmpty()) {
                drill.ejectCoreUp();
                player.displayClientMessage(Component.literal("§eCore removed"), true);
                return InteractionResult.SUCCESS;
            }

            if (heldItem.getItem() instanceof WrenchItem) {

                if (state.getValue(LaserDrillControllerBlock.ACTIVE)) {
                    player.displayClientMessage(Component.literal("§eMultiblock is already active!"), true);
                    return InteractionResult.SUCCESS;
                }

                Direction hatchDir = null;
                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    if (level.getBlockState(pos.relative(dir)).getBlock() instanceof LaserDrillHatchBlock) {
                        hatchDir = dir;
                        break;
                    }
                }

                boolean success = MultiblockHandler.tryFormDrill(level, pos, state, player);

                if (success) {
                    if (hatchDir == null) {
                        hatchDir = state.getValue(HORIZONTAL_FACING);
                    }

                    Direction controllerFacing = hatchDir;

                    BlockState updated = state
                            .setValue(HORIZONTAL_FACING, controllerFacing)
                            .setValue(LaserDrillControllerBlock.ACTIVE, true);

                    com.simibubi.create.content.kinetics.base.KineticBlockEntity.switchToBlockState(level, pos, updated);
                    KineticBlockEntity.switchToBlockState(level, pos, updated);
                    level.sendBlockUpdated(pos, state, updated, 2);

                    player.displayClientMessage(Component.literal("§aMultiblock successfully built!"), true);
                } else {
                    player.displayClientMessage(Component.literal("§cInvalid structure!"), true);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LaserDrillControllerBlockEntity(ModBlockEntities.LASER_DRILL_ENTITY.get(), pos, state);
    }

    @Override
    public @org.jetbrains.annotations.Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {

        if (type != ModBlockEntities.LASER_DRILL_ENTITY.get())
            return null;

        if (level.isClientSide) {
            return (lvl, pos, st, be) -> {
                if (be instanceof LaserDrillControllerBlockEntity kbe)
                    kbe.clientTick();
            };
        } else {
            return (lvl, pos, st, be) -> {
                if (be instanceof LaserDrillControllerBlockEntity kbe)
                    kbe.tick();
            };
        }
    }

}
