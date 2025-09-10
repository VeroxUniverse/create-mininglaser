package net.veroxuniverse.create_mininglaser.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.veroxuniverse.create_mininglaser.CreateMininglaser;

public class ModDamageTypes {

    public static final ResourceKey<DamageType> LASER_KEY =
            ResourceKey.create(Registries.DAMAGE_TYPE,
                    new ResourceLocation(CreateMininglaser.MODID, "laser"));

    public static DamageSource laser(Level level) {
        var holder = level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(LASER_KEY);

        return new DamageSource(holder) {
            @Override
            public Component getLocalizedDeathMessage(LivingEntity victim) {
                Entity killer = this.getEntity();
                boolean hasKiller = killer != null;
                String baseKey = "death.attack.laser";
                int variant = victim.level().random.nextInt(4) + 1; // 1–4
                String key = baseKey + (hasKiller ? ".player." : ".") + variant;
                return hasKiller
                        ? Component.translatable(key, victim.getDisplayName(), killer.getDisplayName())
                        : Component.translatable(key, victim.getDisplayName());
            }
        };
    }
}
