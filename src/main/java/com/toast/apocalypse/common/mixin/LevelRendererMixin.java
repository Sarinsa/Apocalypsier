package com.toast.apocalypse.common.mixin;

import com.toast.apocalypse.common.misc.mixin_work.ClientMixinHooks;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin implements ResourceManagerReloadListener, AutoCloseable {

    @Shadow
    private ClientLevel level;
    @Shadow
    private int ticks;

    public LevelRendererMixin(Minecraft mc, EntityRenderDispatcher entityRD, BlockEntityRenderDispatcher blockEntityRD, RenderBuffers renderBuffers) {

    }


    @Inject(method = "renderSnowAndRain", at = @At(value = "HEAD"), cancellable = true)
    public void onRenderSnowAndRain(LightTexture lightTexture, float partialTick, double x, double y, double z, CallbackInfo ci) {
        ClientMixinHooks.onRenderSnowAndRain(level, ticks, lightTexture, partialTick, x, y, z, ci);
    }

    @Inject(method = "tickRain", at = @At(value = "HEAD"), cancellable = true)
    public void onTickRain(Camera camera, CallbackInfo ci) {
        ClientMixinHooks.onTickRain(level, ticks, camera, ci);
    }
}
