package net.veroxuniverse.create_mininglaser.ponder;

import net.createmod.ponder.api.level.PonderLevel;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.createmod.ponder.api.registration.SharedTextRegistrationHelper;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.veroxuniverse.create_mininglaser.CreateMininglaser;
import net.veroxuniverse.create_mininglaser.registry.ModBlocks;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MiningLaserPonderPlugin implements PonderPlugin {

    static {
        System.out.println("[CreateMiningLaser] Ponder plugin class loaded");
    }

    @Override
    public String getModId() {
        return "create_mininglaser";
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        var H = helper.withKeyFunction(obj ->
                obj instanceof net.minecraft.world.level.block.Block b ? b.builtInRegistryHolder().key().location()
                        : obj instanceof net.minecraft.world.item.Item i         ? i.builtInRegistryHolder().key().location()
                        : null);

        H.forComponents(ModBlocks.LASER_DRILL.get()) // Block, mapped to RL by withKeyFunction
                .addStoryBoard("laser_multiblock", MiningLaserPonderScenes::laserMultiblock)
                .addStoryBoard("insert_core",      MiningLaserPonderScenes::insertCore)
                .addStoryBoard("output_setup",     MiningLaserPonderScenes::outputSetup);
    }

    @Override public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {}
    @Override public void registerSharedText(SharedTextRegistrationHelper helper) {}
    @Override public void onPonderLevelRestore(PonderLevel level) {}
}
