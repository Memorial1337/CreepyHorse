package com.pkfl.creepyhorse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.pkfl.creepyhorse.registry.ModSounds;
import java.io.IOException;
import java.io.InputStream;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.sounds.SoundEvent;

public final class CreepyHorseIntroScreen
extends Screen {
    private static final int FRAME_COUNT = 211;
    private static final int FRAME_RATE = 20;
    private static final int FRAME_WIDTH = 1880;
    private static final int FRAME_HEIGHT = 1080;
    private static final ResourceLocation DYNAMIC_FRAME = new ResourceLocation("creepyhorse", "dynamic/intro");
    private final Screen returnScreen;
    private long startedAt;
    private int loadedFrame = -1;
    private DynamicTexture frameTexture;
    private SoundInstance introSound;
    private boolean finished;

    public CreepyHorseIntroScreen(Screen returnScreen) {
        super((Component)Component.empty());
        this.returnScreen = returnScreen;
    }

    protected void init() {
        if (this.frameTexture != null || this.finished) {
            return;
        }
        this.startedAt = Util.getMillis();
        this.frameTexture = new DynamicTexture(new NativeImage(FRAME_WIDTH, FRAME_HEIGHT, false));
        Minecraft.getInstance().getTextureManager().register(DYNAMIC_FRAME, (AbstractTexture)this.frameTexture);
        this.loadFrame(0);
        this.introSound = SimpleSoundInstance.forUI((SoundEvent)((SoundEvent)ModSounds.NEW_INTRO.get()), (float)1.0f);
        Minecraft.getInstance().getSoundManager().play(this.introSound);
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int frame = (int)((Util.getMillis() - this.startedAt) * 20L / 1000L);
        if (frame >= 211) {
            this.finish();
            return;
        }
        this.loadFrame(frame);
        graphics.fill(0, 0, this.width, this.height, -16777216);
        float scale = Math.min((float)this.width / FRAME_WIDTH, (float)this.height / FRAME_HEIGHT);
        int drawWidth = Math.round(FRAME_WIDTH * scale);
        int drawHeight = Math.round(FRAME_HEIGHT * scale);
        graphics.blit(DYNAMIC_FRAME, (this.width - drawWidth) / 2, (this.height - drawHeight) / 2,
                drawWidth, drawHeight, 0.0f, 0.0f, FRAME_WIDTH, FRAME_HEIGHT, FRAME_WIDTH, FRAME_HEIGHT);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.finish();
            return true;
        }
        return true;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.finish();
        return true;
    }

    public void onClose() {
        this.finish();
    }

    public boolean isPauseScreen() {
        return false;
    }

    private void loadFrame(int frame) {
        if (frame == this.loadedFrame || this.finished) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        ResourceLocation source = new ResourceLocation("creepyhorse", String.format("textures/gui/intro/frame_%03d.png", frame + 1));
        try (InputStream stream = ((Resource)minecraft.getResourceManager().getResource(source).orElseThrow()).open();){
            if (this.frameTexture == null) {
                return;
            }
            this.frameTexture.setPixels(NativeImage.read((InputStream)stream));
            this.frameTexture.upload();
            this.loadedFrame = frame;
        }
        catch (IOException | IllegalStateException exception) {
            this.finish();
        }
    }

    private void finish() {
        if (this.finished) {
            return;
        }
        this.finished = true;
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getTextureManager().release(DYNAMIC_FRAME);
        if (this.introSound != null) {
            minecraft.getSoundManager().stop(this.introSound);
        }
        minecraft.setScreen(this.returnScreen);
    }
}
