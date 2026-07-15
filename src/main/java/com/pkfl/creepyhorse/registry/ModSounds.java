package com.pkfl.creepyhorse.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create((IForgeRegistry)ForgeRegistries.SOUND_EVENTS, (String)"creepyhorse");
    public static final RegistryObject<SoundEvent> HIDE_START = ModSounds.sound("hide_start");
    public static final RegistryObject<SoundEvent> HIDE_TWO = ModSounds.sound("hide2");
    public static final RegistryObject<SoundEvent> HIDE = ModSounds.sound("hide");
    public static final RegistryObject<SoundEvent> BRUSH_THE_FLIES = ModSounds.sound("brushtheflies");
    public static final RegistryObject<SoundEvent> FLIES_TASK = ModSounds.sound("flies_task");
    public static final RegistryObject<SoundEvent> FEED_THE_HORSE = ModSounds.sound("feedthehorse");
    public static final RegistryObject<SoundEvent> SECOND_TASK = ModSounds.sound("second_task");
    public static final RegistryObject<SoundEvent> FEED_THE_HORSE_TWO = ModSounds.sound("feedthehorse2");
    public static final RegistryObject<SoundEvent> NECK_MOVEMENT = ModSounds.sound("neck_movement");
    public static final RegistryObject<SoundEvent> STALK = ModSounds.sound("stalk");
    public static final RegistryObject<SoundEvent> HORSE_APPEAR = ModSounds.sound("horse_appear");
    public static final RegistryObject<SoundEvent> HORSE_SCREAMING_TWO = ModSounds.sound("horse_screaming2");
    public static final RegistryObject<SoundEvent> HORSE_ONE = ModSounds.sound("horse1");
    public static final RegistryObject<SoundEvent> HORSE_START = ModSounds.sound("horse_start");
    public static final RegistryObject<SoundEvent> BRUSH_REQUIRED = ModSounds.sound("brushrequired");
    public static final RegistryObject<SoundEvent> SUCCESS = ModSounds.sound("success");
    public static final RegistryObject<SoundEvent> HORSE_CHASE = ModSounds.sound("horse_chase");
    public static final RegistryObject<SoundEvent> KILL_PLAYER = ModSounds.sound("killplayer");
    public static final RegistryObject<SoundEvent> PENALTY = ModSounds.sound("penalty");
    public static final RegistryObject<SoundEvent> TIME_IS_UP = ModSounds.sound("timeisup");
    public static final RegistryObject<SoundEvent> ENTER_IN = ModSounds.sound("enter_in");
    public static final RegistryObject<SoundEvent> HORSE_SCREAM = ModSounds.sound("horse_scream");
    public static final RegistryObject<SoundEvent> NEW_INTRO = ModSounds.sound("new_intro");

    private ModSounds() {
    }

    private static RegistryObject<SoundEvent> sound(String name) {
        ResourceLocation id = new ResourceLocation("creepyhorse", name);
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent((ResourceLocation)id));
    }
}

