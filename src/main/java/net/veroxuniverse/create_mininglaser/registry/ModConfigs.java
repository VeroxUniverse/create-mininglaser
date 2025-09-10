package net.veroxuniverse.create_mininglaser.registry;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.veroxuniverse.create_mininglaser.content.items.DrillTier;

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
        private static final double SU_MIN = 0.0;
        private static final double SU_MAX = 1.0e12;

        public final ForgeConfigSpec.DoubleValue suScale;

        public final ForgeConfigSpec.DoubleValue stressT1;
        public final ForgeConfigSpec.DoubleValue stressT2;
        public final ForgeConfigSpec.DoubleValue stressT3;
        public final ForgeConfigSpec.DoubleValue stressT4;
        public final ForgeConfigSpec.DoubleValue stressT5;

        Common(ForgeConfigSpec.Builder b) {
            b.push("laser_drill");

            suScale = b.comment(
                            "Global stress multiplier applied to all tiers.",
                            "IMPORTANT:",
                            " - The listed stress values are defined for the minimum operating speed of 128 RPM.",
                            " - At 128 RPM the drill consumes exactly the configured SU.",
                            " - At higher RPM the drill runs faster, but SU consumption is higher as well(doubled on 256 RPM).",
                            "Example: suScale = 1000.0 → a value of 32 becomes 32,000 SU at 128 RPM.")
                    .defineInRange("suScale", 1000.0, 0.0, 1.0e6);

            stressT1 = b.comment("Tier 1: Stress consumption at 128 RPM (before suScale is applied)")
                    .defineInRange("stressTier1", 32.0, SU_MIN, SU_MAX);
            stressT2 = b.comment("Tier 2: Stress consumption at 128 RPM (before suScale is applied)")
                    .defineInRange("stressTier2", 128.0, SU_MIN, SU_MAX);
            stressT3 = b.comment("Tier 3: Stress consumption at 128 RPM (before suScale is applied)")
                    .defineInRange("stressTier3", 512.0, SU_MIN, SU_MAX);
            stressT4 = b.comment("Tier 4: Stress consumption at 128 RPM (before suScale is applied)")
                    .defineInRange("stressTier4", 1024.0, SU_MIN, SU_MAX);
            stressT5 = b.comment("Tier 5: Stress consumption at 128 RPM (before suScale is applied)")
                    .defineInRange("stressTier5", 2048.0, SU_MIN, SU_MAX);

            b.pop();
        }

        public double getStressForTier(DrillTier tier) {
            double scale = suScale.get();
            return switch (tier) {
                case T1 -> stressT1.get() * scale;
                case T2 -> stressT2.get() * scale;
                case T3 -> stressT3.get() * scale;
                case T4 -> stressT4.get() * scale;
                case T5 -> stressT5.get() * scale;
            };
        }
    }
}
