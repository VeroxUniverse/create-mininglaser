package net.veroxuniverse.create_mininglaser.content.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.veroxuniverse.create_mininglaser.registry.ModBlockEntities;
import org.jetbrains.annotations.Nullable;

public class LaserDrillHatchBlockEntity extends BlockEntity {

    private final ItemStackHandler inv = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private final LazyOptional<IItemHandler> invCap = LazyOptional.of(() -> inv);

    public LaserDrillHatchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DRILL_HATCH_ENTITY.get(), pos, state);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        invCap.invalidate();
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable net.minecraft.core.Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return invCap.cast();
        return super.getCapability(cap, side);
    }

    public boolean insertOutput(ItemStack stack) {
        ItemStack leftover = ItemHandlerHelper.insertItem(inv, stack, false);
        return leftover.isEmpty();
    }

    public boolean hasSpace() {
        ItemStack s = inv.getStackInSlot(0);
        return s.isEmpty() || s.getCount() < s.getMaxStackSize();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inv", inv.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Inv")) inv.deserializeNBT(tag.getCompound("Inv"));
    }
}
