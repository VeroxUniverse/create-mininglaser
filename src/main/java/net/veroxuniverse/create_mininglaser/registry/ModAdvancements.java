package net.veroxuniverse.create_mininglaser.registry;

import net.minecraft.resources.ResourceLocation;
import net.veroxuniverse.create_mininglaser.CreateMininglaser;

public class ModAdvancements {
    private static final ResourceLocation LASER_TOUCH =
            new ResourceLocation(CreateMininglaser.MODID, "laser_touch");

    public static void awardTouchedLaser(net.minecraft.server.level.ServerPlayer sp) {
        var server = sp.server;
        if (server == null) return;

        var adv = server.getAdvancements().getAdvancement(LASER_TOUCH);
        if (adv == null) return;

        var prog = sp.getAdvancements().getOrStartProgress(adv);
        if (!prog.isDone()) {
            for (String criterion : prog.getRemainingCriteria()) {
                sp.getAdvancements().award(adv, criterion);
            }
        }
    }
}
