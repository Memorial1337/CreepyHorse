package com.pkfl.creepyhorse.client;

import com.pkfl.creepyhorse.client.CreepyHorseLoopSound;
import com.pkfl.creepyhorse.network.TaskLoopSoundPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

@OnlyIn(value=Dist.CLIENT)
public final class CreepyHorseClientSounds {
    private static CreepyHorseLoopSound currentLoop;
    private static ResourceLocation currentId;

    private CreepyHorseClientSounds() {
    }

    public static void handle(TaskLoopSoundPacket packet) {
        if (!packet.playing()) {
            if (packet.soundId().equals((Object)currentId)) {
                CreepyHorseClientSounds.stopCurrent();
            }
            return;
        }
        if (!packet.soundId().equals((Object)currentId) || currentLoop == null || currentLoop.isStopped()) {
            CreepyHorseClientSounds.stopCurrent();
            SoundEvent sound = (SoundEvent)ForgeRegistries.SOUND_EVENTS.getValue(packet.soundId());
            if (sound == null) {
                return;
            }
            currentId = packet.soundId();
            currentLoop = new CreepyHorseLoopSound(sound, packet.pitch());
            Minecraft.getInstance().getSoundManager().play((SoundInstance)currentLoop);
            return;
        }
        currentLoop.updatePitch(packet.pitch());
    }

    private static void stopCurrent() {
        if (currentLoop != null) {
            currentLoop.stopLoop();
            Minecraft.getInstance().getSoundManager().stop((SoundInstance)currentLoop);
        }
        currentLoop = null;
        currentId = null;
    }
}

