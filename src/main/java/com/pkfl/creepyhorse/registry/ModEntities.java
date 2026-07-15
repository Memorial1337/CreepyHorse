package com.pkfl.creepyhorse.registry;

import com.pkfl.creepyhorse.entity.CreepyHorseEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create((IForgeRegistry)ForgeRegistries.ENTITY_TYPES, (String)"creepyhorse");
    public static final RegistryObject<EntityType<CreepyHorseEntity>> CREEPY_HORSE = ENTITY_TYPES.register("creepy_horse", () -> EntityType.Builder.of(CreepyHorseEntity::new, (MobCategory)MobCategory.MONSTER).sized(1.85f, 3.35f).clientTrackingRange(128).updateInterval(1).build("creepy_horse"));

    private ModEntities() {
    }

    @Mod.EventBusSubscriber(modid="creepyhorse", bus=Mod.EventBusSubscriber.Bus.MOD)
    public static final class Attributes {
        @SubscribeEvent
        public static void register(EntityAttributeCreationEvent event) {
            event.put((EntityType)CREEPY_HORSE.get(), CreepyHorseEntity.createAttributes().build());
        }
    }
}

