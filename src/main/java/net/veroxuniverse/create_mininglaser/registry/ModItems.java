package net.veroxuniverse.create_mininglaser.registry;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.veroxuniverse.create_mininglaser.CreateMininglaser;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, CreateMininglaser.MODID);

    public static final RegistryObject<Item> DRILL_CORE_T1 = ITEMS.register("drill_core_t1",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> DRILL_CORE_T2 = ITEMS.register("drill_core_t2",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> DRILL_CORE_T3 = ITEMS.register("drill_core_t3",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> DRILL_CORE_T4 = ITEMS.register("drill_core_t4",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> DRILL_CORE_T5 = ITEMS.register("drill_core_t5",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> LASER_MECHANISM = ITEMS.register("laser_mechanism",
            () -> new Item(new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> CRYSTAL_T2 = ITEMS.register("crystal_t2",
            () -> new Item(new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> CRYSTAL_T3 = ITEMS.register("crystal_t3",
            () -> new Item(new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> CRYSTAL_T4 = ITEMS.register("crystal_t4",
            () -> new Item(new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> CRYSTAL_T5 = ITEMS.register("crystal_t5",
            () -> new Item(new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> CRYSTAL_T6 = ITEMS.register("crystal_t6",
            () -> new Item(new Item.Properties().stacksTo(64)));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
