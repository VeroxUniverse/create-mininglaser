package net.veroxuniverse.create_mininglaser.content.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.veroxuniverse.create_mininglaser.registry.DrillCasingVariant;
import net.veroxuniverse.create_mininglaser.registry.MultiblockHandler;

public class LaserDrillCasingBlock extends Block {
    public static final DirectionProperty FACING =
            DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);

    public static final EnumProperty<DrillCasingVariant> VARIANT =
            EnumProperty.create("variant", DrillCasingVariant.class);

    public static final BooleanProperty FORMED = BooleanProperty.create("formed");

    public LaserDrillCasingBlock(BlockBehaviour.Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(VARIANT, DrillCasingVariant.BOTTOM_N)
                .setValue(FORMED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, VARIANT, FORMED);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide && state.getBlock() != newState.getBlock()) {
            MultiblockHandler.unformNear(level, pos, state);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            MultiblockHandler.unformNear(level, pos, state);
        }
        super.playerWillDestroy(level, pos, state, player);
    }


}
