package com.pkfl.creepyhorse;

import com.pkfl.creepyhorse.network.CreepyHorseNetwork;
import com.pkfl.creepyhorse.registry.ModEntities;
import com.pkfl.creepyhorse.registry.ModSounds;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import software.bernie.geckolib.GeckoLib;

@Mod(value="creepyhorse")
public final class CreepyHorseMod {
    public static final String MOD_ID = "creepyhorse";

    public CreepyHorseMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.ENTITY_TYPES.register(modBus);
        ModSounds.SOUNDS.register(modBus);
        GeckoLib.initialize();
        CreepyHorseNetwork.init();
        MinecraftForge.EVENT_BUS.register((Object)this);
    }
}

