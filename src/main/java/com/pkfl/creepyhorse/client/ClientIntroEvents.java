package com.pkfl.creepyhorse.client;

import com.pkfl.creepyhorse.client.CreepyHorseIntroScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="creepyhorse", value={Dist.CLIENT})
public final class ClientIntroEvents {
    private static boolean shownThisSession;

    private ClientIntroEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || shownThisSession) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Screen screen = minecraft.screen;
        if (screen instanceof TitleScreen) {
            TitleScreen titleScreen = (TitleScreen)screen;
            shownThisSession = true;
            minecraft.setScreen((Screen)new CreepyHorseIntroScreen((Screen)titleScreen));
        }
    }
}

