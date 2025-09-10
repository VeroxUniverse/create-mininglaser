package net.veroxuniverse.create_mininglaser.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.veroxuniverse.create_mininglaser.CreateMininglaser;

public class ModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateMininglaser.MODID);

    @SuppressWarnings("unused")
    public static final RegistryObject<CreativeModeTab> TAB = CREATIVE_TABS.register(
            CreateMininglaser.MODID,
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + CreateMininglaser.MODID))
                    .icon(() -> new ItemStack(ModBlocks.LASER_DRILL.get().asItem()))
                    .displayItems((displayParameters, output) -> {
                        for (RegistryObject<Item> item : ModItems.ITEMS.getEntries())
                            output.accept(item.get());
                    })
                    .build()
    );

    public static void register(IEventBus eventBus) {
        CREATIVE_TABS.register(eventBus);
    }
}