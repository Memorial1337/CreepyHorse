package com.pkfl.creepyhorse.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.pkfl.creepyhorse.client.model.CreepyHorseModel;
import com.pkfl.creepyhorse.entity.CreepyHorseEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class CreepyHorseRenderer
extends GeoEntityRenderer<CreepyHorseEntity> {
    public CreepyHorseRenderer(EntityRendererProvider.Context context) {
        super(context, (GeoModel)new CreepyHorseModel());
        this.shadowRadius = 0.9f;
    }

    public void render(CreepyHorseEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }
}
