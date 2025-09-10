package net.veroxuniverse.create_mininglaser.content.items;

public enum DrillTier {
    T1(1), T2(2), T3(3), T4(4), T5(5);
    public final int level;
    DrillTier(int l){ this.level = l; }
    public static DrillTier fromLevel(int i) {
        return switch(i){
            case 1->T1; case 2->T2; case 3->T3; case 4->T4; default->T5;
        };
    }
}
