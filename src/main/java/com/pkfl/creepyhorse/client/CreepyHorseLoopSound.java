package com.pkfl.creepyhorse.client;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(value=Dist.CLIENT)
final class CreepyHorseLoopSound
extends AbstractTickableSoundInstance {
    CreepyHorseLoopSound(SoundEvent sound, float pitch) {
        super(sound, SoundSource.HOSTILE, RandomSource.create());
        this.relative = true;
        this.looping = true;
        this.delay = 0;
        this.volume = 1.0f;
        this.pitch = pitch;
        this.attenuation = SoundInstance.Attenuation.NONE;
    }

    void updatePitch(float pitch) {
        this.pitch = pitch;
    }

    void stopLoop() {
        this.stop();
    }

    public void tick() {
    }
}

