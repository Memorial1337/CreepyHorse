package com.pkfl.creepyhorse.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.pkfl.creepyhorse.client.renderer.CreepyHorseRenderer;
import com.pkfl.creepyhorse.entity.CreepyHorseEntity;
import net.minecraft.client.model.PigModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

final class CreepyHorseSaddleLayer
extends GeoRenderLayer<CreepyHorseEntity> {
    private static final ResourceLocation VANILLA_SADDLE_TEXTURE = new ResourceLocation("minecraft", "textures/entity/pig/pig_saddle.png");
    private final PigModel<CreepyHorseEntity> saddleModel;

    CreepyHorseSaddleLayer(CreepyHorseRenderer renderer, EntityRendererProvider.Context context) {
        super((GeoRenderer)renderer);
        this.saddleModel = new PigModel(context.bakeLayer(ModelLayers.PIG_SADDLE));
    }

    public void renderForBone(PoseStack poseStack, CreepyHorseEntity entity, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if (!entity.isSaddled() || !"saddle".equals(bone.getName())) {
            return;
        }
        poseStack.pushPose();
        poseStack.scale(1.25f, 1.25f, 1.25f);
        this.saddleModel.setupAnim(entity, 0.0f, 0.0f, (float)entity.tickCount + partialTick, 0.0f, 0.0f);
        this.saddleModel.renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.entityCutoutNoCull((ResourceLocation)VANILLA_SADDLE_TEXTURE)), packedLight, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
        poseStack.popPose();
    }
}
