package com.toast.apocalypse.common.core.mod_event.events;

import com.toast.apocalypse.common.core.mod_event.EventType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.server.ServerWorld;

public class EmptyEvent extends AbstractEvent {

    public EmptyEvent(EventType<?> type, ServerPlayerEntity player) {
        super(type, player);
    }

    @Override
    public void onStart(MinecraftServer server) {

    }

    @Override
    public void update(ServerWorld world) {

    }

    @Override
    public void update(PlayerEntity player) {

    }

    @Override
    public void onEnd() {

    }

    @Override
    public void stop() {

    }

    @Override
    public CompoundNBT write(CompoundNBT data) {
        data.putInt("EventId", this.getType().getId());
        return data;
    }

    @Override
    public void read(CompoundNBT data)  {

    }
}