package net.veroxuniverse.create_mininglaser.registry;

import net.minecraft.util.StringRepresentable;

public enum DrillCasingVariant implements StringRepresentable {
    BOTTOM_NW("bottom_nw"),
    BOTTOM_N("bottom_n"),
    BOTTOM_NE("bottom_ne"),
    BOTTOM_W("bottom_w"),
    BOTTOM_E("bottom_e"),
    BOTTOM_SW("bottom_sw"),
    BOTTOM_S("bottom_s"),
    BOTTOM_SE("bottom_se"),

    TOP_LEFT("top_left"),
    TOP_FRONT("top_front"),
    TOP_BACK("top_back");

    private final String name;

    DrillCasingVariant(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
