package net.veroxuniverse.create_mininglaser.registry;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.ResourceLocation;
import net.veroxuniverse.create_mininglaser.CreateMininglaser;

import static net.minecraft.Util.prefix;

public class ModPartials {
    public static final PartialModel SHAFT_MODEL =
            PartialModel.of(new ResourceLocation(CreateMininglaser.MODID,"block/laser_drill/shaft"));
    public static final PartialModel LASER_HEAD_T1 =
            PartialModel.of(new ResourceLocation(CreateMininglaser.MODID, "block/laser_head_t1"));
    public static final PartialModel LASER_HEAD_T2 =
            PartialModel.of(new ResourceLocation(CreateMininglaser.MODID, "block/laser_head_t2"));
    public static final PartialModel LASER_HEAD_T3 =
            PartialModel.of(new ResourceLocation(CreateMininglaser.MODID, "block/laser_head_t3"));
    public static final PartialModel LASER_HEAD_T4 =
            PartialModel.of(new ResourceLocation(CreateMininglaser.MODID, "block/laser_head_t4"));
    public static final PartialModel LASER_HEAD_T5 =
            PartialModel.of(new ResourceLocation(CreateMininglaser.MODID, "block/laser_head_t5"));

    public static void init() {}
}

