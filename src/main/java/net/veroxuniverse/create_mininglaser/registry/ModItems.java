package net.veroxuniverse.create_mininglaser.registry;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.veroxuniverse.create_mininglaser.CreateMininglaser;
import net.veroxuniverse.create_mininglaser.content.items.DrillCoreItem;
import net.veroxuniverse.create_mininglaser.content.items.DrillTier;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, CreateMininglaser.MODID);

    public static final RegistryObject<Item> DRILL_CORE_T1 = ITEMS.register("drill_core_t1",
            () -> new DrillCoreItem(new Item.Properties().stacksTo(1), DrillTier.T1));

    public static final RegistryObject<Item> DRILL_CORE_T2 = ITEMS.register("drill_core_t2",
            () -> new DrillCoreItem(new Item.Properties().stacksTo(1), DrillTier.T2));

    public static final RegistryObject<Item> DRILL_CORE_T3 = ITEMS.register("drill_core_t3",
            () -> new DrillCoreItem(new Item.Properties().stacksTo(1), DrillTier.T3));

    public static final RegistryObject<Item> DRILL_CORE_T4 = ITEMS.register("drill_core_t4",
            () -> new DrillCoreItem(new Item.Properties().stacksTo(1), DrillTier.T4));

    public static final RegistryObject<Item> DRILL_CORE_T5 = ITEMS.register("drill_core_t5",
            () -> new DrillCoreItem(new Item.Properties().stacksTo(1), DrillTier.T5));

    public static Item byTier(DrillTier t) {
        return switch (t) {
            case T1 -> DRILL_CORE_T1.get();
            case T2 -> DRILL_CORE_T2.get();
            case T3 -> DRILL_CORE_T3.get();
            case T4 -> DRILL_CORE_T4.get();
            case T5 -> DRILL_CORE_T5.get();
        };
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
