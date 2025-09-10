package net.veroxuniverse.create_mininglaser.registry;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class ModConfigs {

    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        COMMON = new Common(b);
        COMMON_SPEC = b.build();
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
    }

    public static class Common {
        public final ForgeConfigSpec.DoubleValue suScale;

        Common(ForgeConfigSpec.Builder b) {
            b.push("laser_drill");

            suScale = b.comment(
                            "Global SU multiplier applied to ALL tiers defined via data.",
                            "IMPORTANT:",
                            " - Tier JSON uses unscaled values for stress_at_128.",
                            " - Effective SU at 128 RPM = stress_at_128 * suScale.",
                            " - At higher RPM the drill runs faster and consumes proportionally more SU.",
                            "Examples:",
                            "  - suScale = 1000.0 and stress_at_128 = 32.0  -> 32,000 SU at 128 RPM",
                            "  - suScale = 1.0    and stress_at_128 = 32000 -> 32,000 SU at 128 RPM"
                    )
                    .defineInRange("suScale", 1000.0, 0.0, 1.0e6);

            b.pop();
        }
    }
}
