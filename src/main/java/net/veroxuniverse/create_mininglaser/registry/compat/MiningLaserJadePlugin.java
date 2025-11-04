package net.veroxuniverse.create_mininglaser.registry.compat;

import net.veroxuniverse.create_mininglaser.content.blocks.LaserDrillControllerBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class MiningLaserJadePlugin implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(LaserDrillJadeProvider.INSTANCE, LaserDrillControllerBlock.class);
    }
}

