package com.pkfl.creepyhorse.network;

import com.pkfl.creepyhorse.network.TaskLoopSoundPacket;
import com.pkfl.creepyhorse.network.TaskOverlayPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class CreepyHorseNetwork {
    private static final String PROTOCOL = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder.named((ResourceLocation)new ResourceLocation("creepyhorse", "main")).networkProtocolVersion(() -> "1").clientAcceptedVersions("1"::equals).serverAcceptedVersions("1"::equals).simpleChannel();
    private static int nextId;

    private CreepyHorseNetwork() {
    }

    public static void init() {
        CHANNEL.registerMessage(nextId++, TaskOverlayPacket.class, TaskOverlayPacket::encode, TaskOverlayPacket::decode, TaskOverlayPacket::handle);
        CHANNEL.registerMessage(nextId++, TaskLoopSoundPacket.class, TaskLoopSoundPacket::encode, TaskLoopSoundPacket::decode, TaskLoopSoundPacket::handle);
    }

    public static void sendOverlay(ServerPlayer player, String top, String bottom, int remainingTicks, boolean bottomRed, boolean danger, boolean glitch) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), (Object)new TaskOverlayPacket(top, bottom, remainingTicks, bottomRed, danger, glitch));
    }

    public static void sendLoopSound(ServerPlayer player, ResourceLocation soundId, boolean playing, float pitch) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), (Object)new TaskLoopSoundPacket(soundId, playing, pitch));
    }
}

