package com.pkfl.creepyhorse.client;

import com.pkfl.creepyhorse.client.PolyMeshBakedModelFactory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import software.bernie.geckolib.loading.object.BakedModelFactory;

@Mod.EventBusSubscriber(modid="creepyhorse", bus=Mod.EventBusSubscriber.Bus.MOD, value={Dist.CLIENT})
public final class PolyMeshClientBootstrap {
    private PolyMeshClientBootstrap() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(PolyMeshClientBootstrap::registerFactory);
    }

    public static void registerFactory() {
        BakedModelFactory.register((String)"creepyhorse", (BakedModelFactory)new PolyMeshBakedModelFactory());
    }

    static {
        PolyMeshClientBootstrap.registerFactory();
    }
}

