package net.veroxuniverse.create_mininglaser.content.blocks;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.veroxuniverse.create_mininglaser.content.items.TierDef;
import net.veroxuniverse.create_mininglaser.content.items.TierDefs;
import net.veroxuniverse.create_mininglaser.content.laser.recipe.DrillCoreRecipe;
import net.veroxuniverse.create_mininglaser.content.laser.recipe.DrillCoreRecipeHelper;
import net.veroxuniverse.create_mininglaser.registry.ModAdvancements;
import net.veroxuniverse.create_mininglaser.registry.ModBlockEntities;
import net.veroxuniverse.create_mininglaser.registry.ModConfigs;
import net.veroxuniverse.create_mininglaser.registry.ModDamageTypes;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public class LaserDrillControllerBlockEntity extends KineticBlockEntity {

    public static final float DEFAULT_MIN_RPM  = 128f;
    private static final float DEFAULT_MAX_RPM = 256f;

    private TierDef activeTier;
    private double progress;
    private int processTime;

    public TierDef getActiveTier() { return activeTier; }

    public LaserDrillControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LASER_DRILL_ENTITY.get(), pos, state);
    }
    public LaserDrillControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private final net.minecraftforge.items.ItemStackHandler inv = new net.minecraftforge.items.ItemStackHandler(1) {
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            return TierDefs.byCoreItem(stack.getItem()) != null;
        }
        @Override protected int getStackLimit(int slot, ItemStack stack) { return 1; }
        @Override protected void onContentsChanged(int slot) { setChanged(); sendData(); }
    };
    private final LazyOptional<net.minecraftforge.items.IItemHandler> invCap = LazyOptional.of(() -> inv);

    @Override public void invalidateCaps() { super.invalidateCaps(); invCap.invalidate(); }
    @Override public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return invCap.cast();
        return super.getCapability(cap, side);
    }

    public ItemStack getCore() { return inv.getStackInSlot(0); }
    public void setCore(ItemStack stack) { inv.setStackInSlot(0, stack); }
    public ItemStack removeCore() { return inv.extractItem(0, 1, false); }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        tag.put("Core", inv.serializeNBT());
        tag.putInt("processTime", processTime);
        tag.putDouble("progress", progress);
        if (activeTier != null) tag.putString("activeTierId", activeTier.id.toString());
        super.write(tag, clientPacket);
    }
    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        if (tag.contains("Core")) inv.deserializeNBT(tag.getCompound("Core"));
        processTime = tag.getInt("processTime");
        progress = tag.contains("progress") ? tag.getDouble("progress") : tag.getInt("progress");
        activeTier = null;
        if (tag.contains("activeTierId")) {
            var id = new net.minecraft.resources.ResourceLocation(tag.getString("activeTierId"));
            activeTier = TierDefs.get(id);
        } else if (tag.contains("activeTier")) {
            // Legacy-Fallback: create_mininglaser:t{level}
            int legacyLevel = tag.getInt("activeTier");
            var id = new net.minecraft.resources.ResourceLocation("create_mininglaser", "t" + legacyLevel);
            activeTier = TierDefs.get(id);
        }
        super.read(tag, clientPacket);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!level.isClientSide) { detachKinetics(); attachKinetics(); }
    }
    @Override
    public void remove() {
        super.remove();
        if (!level.isClientSide) this.detachKinetics();
    }

    private void recalcStress() {
        this.lastStressApplied = -1f;
        this.setChanged();
        this.notifyUpdate();
        if (this.hasNetwork()) {
            var net = this.getOrCreateNetwork();
            if (net != null) net.updateNetwork();
        } else {
            this.updateSpeed = true;
            this.attachKinetics();
        }
    }

    @Override
    public void tick() {
        if (level == null || level.isClientSide) return;

        BlockState state = getBlockState();

        if (!state.hasProperty(LaserDrillControllerBlock.ACTIVE) || !state.getValue(LaserDrillControllerBlock.ACTIVE)) {
            if (this.lastStressApplied != 0f || activeTier != null) recalcStress();
            progress = 0.0;
            return;
        }

        ItemStack core = inv.getStackInSlot(0);
        TierDef foundTier = core.isEmpty() ? null : TierDefs.byCoreItem(core.getItem());
        if (foundTier == null) {
            if (activeTier != null) {
                activeTier = null;
                processTime = 0;
                progress = 0.0;
                recalcStress();
            }
            return;
        }

        if (activeTier == null || !activeTier.id.equals(foundTier.id)) {
            activeTier = foundTier;
            processTime = 0;
            progress = 0.0;
            recalcStress();
        }

        DrillCoreRecipe rec = DrillCoreRecipeHelper.findRecipe(level.getRecipeManager(), activeTier);
        if (rec == null) { progress = 0.0; return; }
        if (processTime == 0) processTime = rec.getDurationTicks();

        float minRpm = activeTier != null && activeTier.minRpm > 0 ? activeTier.minRpm : DEFAULT_MIN_RPM;
        float maxRpm = activeTier != null && activeTier.maxRpm > 0 ? activeTier.maxRpm : DEFAULT_MAX_RPM;

        float absSpeed = Math.abs(getSpeed());
        if (absSpeed < minRpm) {
            if (progress != 0.0) progress = 0.0;
            return;
        }

        float rawMult = absSpeed / minRpm;
        float speedMult = Mth.clamp(rawMult, 1.0f, maxRpm / minRpm);

        doLaserBeamEffects(true);

        progress += speedMult;

        if (progress < processTime) return;

        progress = 0.0;
        ItemStack result = rec.rollOnce(level.random, level, worldPosition);
        if (result.isEmpty()) return;
        trySendToHatch(result.copy());
    }

    @Override
    public void onSpeedChanged(float prevSpeed) {
        super.onSpeedChanged(prevSpeed);
        float minRpm = activeTier != null && activeTier.minRpm > 0 ? activeTier.minRpm : DEFAULT_MIN_RPM;
        float p = Math.abs(prevSpeed);
        float c = Math.abs(getSpeed());
        boolean crossedGate = (p < minRpm) != (c < minRpm);
        if (crossedGate) recalcStress();
    }

    private boolean trySendToHatch(ItemStack stack) {
        Direction hatchDir = getBlockState().getValue(HORIZONTAL_FACING);
        BlockPos hatchPos = worldPosition.relative(hatchDir);
        var be = level.getBlockEntity(hatchPos);
        if (!(be instanceof LaserDrillHatchBlockEntity hatch)) return false;
        if (!hatch.hasSpace()) return false;
        return hatch.insertOutput(stack);
    }

    public void ejectCoreUp() {
        if (level == null || level.isClientSide) return;
        ItemStack core = removeCore();
        if (core.isEmpty()) return;
        double x = worldPosition.getX() + 0.5;
        double y = worldPosition.getY() + 1.2;
        double z = worldPosition.getZ() + 0.5;
        var ie = new net.minecraft.world.entity.item.ItemEntity(level, x, y, z, core);
        ie.setDefaultPickUpDelay();
        ie.setDeltaMovement(0, 0.2, 0);
        level.addFreshEntity(ie);
    }

    @Override
    public float calculateStressApplied() {
        if (activeTier == null) return this.lastStressApplied = 0f;

        var state = getBlockState();
        if (!state.hasProperty(LaserDrillControllerBlock.ACTIVE) || !state.getValue(LaserDrillControllerBlock.ACTIVE))
            return this.lastStressApplied = 0f;

        float absSpeed = Math.abs(getSpeed());
        float minRpm = activeTier.minRpm > 0 ? activeTier.minRpm : DEFAULT_MIN_RPM;
        if (absSpeed < minRpm) return this.lastStressApplied = 0f;

        double baseSuAt128 = activeTier.stressAt128 * ModConfigs.COMMON.suScale.get();
        float impactPerRpm = (float) (baseSuAt128 / minRpm);

        this.lastStressApplied = impactPerRpm;
        return impactPerRpm;
    }

    private void doLaserBeamEffects(boolean spawnParticlesToo) {
        if (getActiveTier() == null) return;

        double x = worldPosition.getX() + 0.5;
        double yStart = worldPosition.getY() - 0.5;
        double z = worldPosition.getZ() + 0.5;

        int yEndInt = worldPosition.getY() - 20;
        int yMin = Math.max(level.getMinBuildHeight(), yEndInt);

        int stopY = worldPosition.getY() - 1;
        for (int y = worldPosition.getY() - 1; y >= yMin; y--) {
            BlockPos bp = new BlockPos(worldPosition.getX(), y, worldPosition.getZ());
            BlockState bs = level.getBlockState(bp);
            if (!bs.isAir()) { stopY = y + 1; break; }
            stopY = yMin;
        }

        double yEnd = stopY + 0.5;
        AABB beamBB = new AABB(x - 0.35, Math.min(yStart, yEnd), z - 0.35,
                x + 0.35, Math.max(yStart, yEnd), z + 0.35);

        var victims = level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, beamBB,
                e -> !e.isSpectator() && e.isAlive());

        if (!victims.isEmpty()) {
            float dmg = 2.0f;
            var dmgType = ModDamageTypes.laser(level);
            for (var le : victims) {
                le.hurt(dmgType, dmg);
                if (le instanceof net.minecraft.server.level.ServerPlayer sp) {
                    ModAdvancements.awardTouchedLaser(sp);
                }
            }
        }

        if (spawnParticlesToo && level instanceof net.minecraft.server.level.ServerLevel sl) {
            int steps = (int) Math.max(1, Math.abs(yStart - yEnd) * 4);
            for (int i = 0; i < steps; i++) {
                double t = i / (double) steps;
                double py = yStart + (yEnd - yStart) * t;

                DustParticleOptions red = new DustParticleOptions(new Vector3f(1.0f, 0.0f, 0.0f), 1.0f);
                DustParticleOptions whiteCore = new DustParticleOptions(new Vector3f(1.0f, 0.8f, 0.8f), 0.5f);

                sl.sendParticles(red, x, py, z, 1, 0.01, 0.01, 0.01, 0.0);
                sl.sendParticles(whiteCore, x, py, z, 1, 0.005, 0.005, 0.005, 0.0);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private AbstractTickableSoundInstance humSound;

    @OnlyIn(Dist.CLIENT)
    private boolean lastActiveClient;

    @OnlyIn(Dist.CLIENT)
    private boolean isLaserActiveClient() {
        if (level == null) return false;
        BlockState st = getBlockState();
        float minRpm = activeTier != null && activeTier.minRpm > 0 ? activeTier.minRpm : DEFAULT_MIN_RPM;
        return st.hasProperty(LaserDrillControllerBlock.ACTIVE)
                && st.getValue(LaserDrillControllerBlock.ACTIVE)
                && activeTier != null
                && Math.abs(getSpeed()) >= minRpm;
    }

    @OnlyIn(Dist.CLIENT)
    private void startLoopSound() {
        if (level == null || !level.isClientSide || humSound != null) return;
        level.playLocalSound(worldPosition, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.6f, 1.1f, false);
        humSound = new LaserLoopSound(this);
        Minecraft.getInstance().getSoundManager().play(humSound);
    }

    @OnlyIn(Dist.CLIENT)
    private void stopLoopSound() {
        if (level == null || !level.isClientSide) return;
        if (humSound != null) {
            Minecraft.getInstance().getSoundManager().stop(humSound);
            humSound = null;
        }
        level.playLocalSound(worldPosition, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 0.6f, 0.9f, false);
    }

    @OnlyIn(Dist.CLIENT)
    public void clientTick() {
        boolean active = isLaserActiveClient();
        if (active && !lastActiveClient) startLoopSound();
        if (!active && lastActiveClient)  stopLoopSound();
        lastActiveClient = active;
    }

    @OnlyIn(Dist.CLIENT)
    private static class LaserLoopSound extends AbstractTickableSoundInstance {
        private final LaserDrillControllerBlockEntity be;

        LaserLoopSound(LaserDrillControllerBlockEntity be) {
            super(SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, RandomSource.create());
            this.be = be;
            this.looping = true;
            this.relative = false;
            this.x = be.getBlockPos().getX() + 0.5;
            this.y = be.getBlockPos().getY() + 0.5;
            this.z = be.getBlockPos().getZ() + 0.5;
            this.volume = 0.25f;
            this.pitch  = 1.0f;
        }

        @Override
        public void tick() {
            if (be.isRemoved() || be.getLevel() == null || !be.getLevel().isClientSide) {
                this.stop();
                return;
            }
            boolean active = be.isLaserActiveClient();
            if (!active) {
                this.stop();
                return;
            }

            this.x = be.getBlockPos().getX() + 0.5;
            this.y = be.getBlockPos().getY() + 0.5;
            this.z = be.getBlockPos().getZ() + 0.5;

            float rpm = Math.abs(be.getSpeed());
            float minRpm = be.getActiveTier() != null && be.getActiveTier().minRpm > 0 ? be.getActiveTier().minRpm : DEFAULT_MIN_RPM;
            float maxRpm = be.getActiveTier() != null && be.getActiveTier().maxRpm > 0 ? be.getActiveTier().maxRpm : DEFAULT_MAX_RPM;
            float t = Mth.clamp((rpm - minRpm) / Math.max(1f, (maxRpm - minRpm)), 0f, 1f);
            this.pitch  = 0.95f + 0.35f * t;
            this.volume = 0.18f + 0.22f * t;
        }
    }

}