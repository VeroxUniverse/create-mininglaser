package net.veroxuniverse.create_mininglaser.registry;

import com.simibubi.create.api.stress.BlockStressValues;

import java.util.function.DoubleSupplier;

public class ModCreateCompat {
    public static void init() {
        BlockStressValues.IMPACTS.register(
                ModBlocks.LASER_DRILL.get(),
                (DoubleSupplier) () -> 0.001d
        );
    }
}
