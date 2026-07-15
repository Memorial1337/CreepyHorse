package com.pkfl.creepyhorse.client;

import com.mojang.blaze3d.platform.Window;
import com.pkfl.creepyhorse.network.TaskOverlayPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="creepyhorse", value={Dist.CLIENT})
public final class CreepyHorseHud {
    private static String top = "";
    private static String bottom = "";
    private static long expiry;
    private static boolean bottomRed;
    private static boolean danger;
    private static boolean glitch;

    private CreepyHorseHud() {
    }

    public static void receive(TaskOverlayPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        top = packet.top();
        bottom = packet.bottom();
        expiry = minecraft.level == null ? 0L : minecraft.level.getGameTime() + (long)packet.remainingTicks();
        bottomRed = packet.bottomRed();
        danger = packet.danger();
        glitch = packet.glitch();
    }

    @SubscribeEvent
    public static void render(RenderGuiOverlayEvent.Post event) {
        if (!event.getOverlay().id().equals((Object)VanillaGuiOverlay.HOTBAR.id())) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || top.isBlank() || minecraft.options.hideGui) {
            return;
        }
        if (minecraft.level.getGameTime() > expiry) {
            top = "";
            bottom = "";
            danger = false;
            return;
        }
        GuiGraphics graphics = event.getGuiGraphics();
        if (danger) {
            CreepyHorseHud.renderDeterioration(graphics, minecraft.getWindow());
        }
        Font font = minecraft.font;
        int color = danger && minecraft.level.getGameTime() / 4L % 2L == 0L ? -43691 : -1;
        int bottomColor = bottomRed || danger ? -52429 : color;
        int baseY = graphics.guiHeight() - 76;
        graphics.pose().pushPose();
        graphics.pose().translate((float)graphics.guiWidth() / 2.0f, (float)baseY, 0.0f);
        graphics.pose().scale(1.35f, 1.35f, 1.0f);
        graphics.drawCenteredString(font, (Component)Component.literal((String)top), 0, 0, color);
        graphics.drawCenteredString(font, CreepyHorseHud.bottomComponent(bottomColor), 0, 12, bottomColor);
        graphics.pose().popPose();
    }

    private static Component bottomComponent(int color) {
        if (!glitch) {
            return Component.literal((String)bottom);
        }
        String prefix = bottom.endsWith("...") ? bottom.substring(0, bottom.length() - 3) : bottom;
        return Component.literal((String)prefix).withStyle(style -> style.withColor(color)).append((Component)Component.literal((String)"......").withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.OBFUSCATED}));
    }

    private static void renderDeterioration(GuiGraphics graphics, Window window) {
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        long frame = Minecraft.getInstance().level == null ? 0L : Minecraft.getInstance().level.getGameTime();
        graphics.fill(0, 0, width, height, 0x55000000);
        for (int row = 0; row < height; row += 11) {
            int offset = (int)((frame * 13L + (long)row * 7L) % 32L);
            graphics.fill(Math.max(0, offset - 12), row, Math.min(width, offset + width / 4), row + 1, 0x33AA0000);
        }
    }
}

