package com.pkfl.creepyhorse.network;

import com.pkfl.creepyhorse.client.CreepyHorseHud;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public record TaskOverlayPacket(String top, String bottom, int remainingTicks, boolean bottomRed, boolean danger, boolean glitch) {
    public static void encode(TaskOverlayPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.top, 96);
        buffer.writeUtf(packet.bottom, 128);
        buffer.writeVarInt(Math.max(0, packet.remainingTicks));
        buffer.writeBoolean(packet.bottomRed);
        buffer.writeBoolean(packet.danger);
        buffer.writeBoolean(packet.glitch);
    }

    public static TaskOverlayPacket decode(FriendlyByteBuf buffer) {
        return new TaskOverlayPacket(buffer.readUtf(96), buffer.readUtf(128), buffer.readVarInt(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean());
    }

    public static void handle(TaskOverlayPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> TaskOverlayPacket.receiveClient(packet)));
        context.setPacketHandled(true);
    }

    @OnlyIn(value=Dist.CLIENT)
    private static void receiveClient(TaskOverlayPacket packet) {
        CreepyHorseHud.receive(packet);
    }
}

