package com.pkfl.creepyhorse.client;

import com.pkfl.creepyhorse.client.renderer.CreepyHorseRenderer;
import com.pkfl.creepyhorse.registry.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="creepyhorse", bus=Mod.EventBusSubscriber.Bus.MOD, value={Dist.CLIENT})
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer((EntityType)ModEntities.CREEPY_HORSE.get(), CreepyHorseRenderer::new);
    }
}

