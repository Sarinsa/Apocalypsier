package com.toast.apocalypse.common.mixin;

import com.toast.apocalypse.common.misc.mixin_work.ServerMixinHooks;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

@Mixin(ServerLevel.class)
public abstract class ServerWorldMixin extends Level implements WorldGenLevel {


    protected ServerWorldMixin(WritableLevelData levelData, ResourceKey<Level> levelId, RegistryAccess registryAccess,
                               Holder<DimensionType> dimensionType, Supplier<ProfilerFiller> profilerFiller, boolean isClient, boolean isDebug, long seed, int maxChainedNeighborUpdates) {
        super(levelData, levelId, registryAccess, dimensionType, profilerFiller, isClient, isDebug, seed, maxChainedNeighborUpdates);
    }

    @Final
    @Inject(method = "setDayTime", at = @At("HEAD"), cancellable = true)
    public void onSetDayTime(long time, CallbackInfo ci) {
        ServerMixinHooks.onServerWorldSetDayTime((ServerLevel) (Object) this, ci);
    }
}
