package net.veroxuniverse.create_mininglaser.registry;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.veroxuniverse.create_mininglaser.CreateMininglaser;
import net.veroxuniverse.create_mininglaser.content.blocks.LaserDrillControllerBlockEntity;
import net.veroxuniverse.create_mininglaser.content.blocks.LaserDrillHatchBlockEntity;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, CreateMininglaser.MODID);

    public static final RegistryObject<BlockEntityType<LaserDrillControllerBlockEntity>> LASER_DRILL_ENTITY = BLOCK_ENTITIES.register(
            "laser_drill_block_entity",
            () -> BlockEntityType.Builder.of(LaserDrillControllerBlockEntity::new,
                    ModBlocks.LASER_DRILL.get()).build(null)
    );

    public static final RegistryObject<BlockEntityType<LaserDrillHatchBlockEntity>> DRILL_HATCH_ENTITY = BLOCK_ENTITIES.register(
            "drill_hatch_block_entity",
            () -> BlockEntityType.Builder.of(LaserDrillHatchBlockEntity::new,
                    ModBlocks.DRILL_HATCH.get()).build(null)
    );

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}