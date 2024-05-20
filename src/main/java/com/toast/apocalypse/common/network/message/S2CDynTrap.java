package com.toast.apocalypse.common.network.message;

import com.toast.apocalypse.common.network.work.ClientWork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CDynTrap {

    public final BlockPos pos;
    public final String id;

    public S2CDynTrap(BlockPos pos, String id) {
        this.pos = pos;
        this.id = id;
    }

    public static void handle(S2CDynTrap message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> ClientWork.handleDynTrapUpdate(message));
        }
        context.setPacketHandled(true);
    }

    public static S2CDynTrap decode(FriendlyByteBuf buffer) {
        return new S2CDynTrap(buffer.readBlockPos(), buffer.readUtf());
    }

    public static void encode(S2CDynTrap message, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(message.pos);
        buffer.writeUtf(message.id);
    }
}
