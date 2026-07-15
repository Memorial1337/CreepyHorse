package com.pkfl.creepyhorse.client.model;

import com.pkfl.creepyhorse.entity.CreepyHorseEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class CreepyHorseModel
extends GeoModel<CreepyHorseEntity> {
    private static final ResourceLocation MODEL = new ResourceLocation("creepyhorse", "geo/creepy_horse.geo.json");
    private static final ResourceLocation ANIMATIONS = new ResourceLocation("creepyhorse", "animations/creepy_horse.animation.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation("creepyhorse", "textures/entity/creepy_horse.png");

    public ResourceLocation getModelResource(CreepyHorseEntity entity) {
        return MODEL;
    }

    public ResourceLocation getTextureResource(CreepyHorseEntity entity) {
        return TEXTURE;
    }

    public ResourceLocation getAnimationResource(CreepyHorseEntity entity) {
        return ANIMATIONS;
    }
}

