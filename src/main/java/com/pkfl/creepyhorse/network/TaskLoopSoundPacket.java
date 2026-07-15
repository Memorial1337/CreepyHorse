package com.pkfl.creepyhorse.network;

import com.pkfl.creepyhorse.client.CreepyHorseClientSounds;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public record TaskLoopSoundPacket(ResourceLocation soundId, boolean playing, float pitch) {
    public static void encode(TaskLoopSoundPacket packet, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(packet.soundId);
        buffer.writeBoolean(packet.playing);
        buffer.writeFloat(packet.pitch);
    }

    public static TaskLoopSoundPacket decode(FriendlyByteBuf buffer) {
        return new TaskLoopSoundPacket(buffer.readResourceLocation(), buffer.readBoolean(), buffer.readFloat());
    }

    public static void handle(TaskLoopSoundPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> TaskLoopSoundPacket.receiveClient(packet)));
        context.setPacketHandled(true);
    }

    @OnlyIn(value=Dist.CLIENT)
    private static void receiveClient(TaskLoopSoundPacket packet) {
        CreepyHorseClientSounds.handle(packet);
    }
}

