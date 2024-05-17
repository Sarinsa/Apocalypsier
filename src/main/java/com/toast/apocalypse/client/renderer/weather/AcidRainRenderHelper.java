package com.toast.apocalypse.client.renderer.weather;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.toast.apocalypse.common.core.Apocalypse;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

import java.util.Random;

import static net.minecraft.client.renderer.LevelRenderer.getLightColor;

public class AcidRainRenderHelper {

    private static final ResourceLocation RAIN_TEXTURE = Apocalypse.resourceLoc("textures/environment/acid_rain.png");

    public static Vector3f RAIN_COLOR;

    private final float[] rainSizeX = new float[1024];
    private final float[] rainSizeZ = new float[1024];

    private int rainSoundTime = 0;
    private boolean rainingAcid = false;

    public AcidRainRenderHelper() {
        for(int i = 0; i < 32; ++i) {
            for(int j = 0; j < 32; ++j) {
                float f = (float)(j - 16);
                float f1 = (float)(i - 16);
                float f2 = Mth.sqrt(f * f + f1 * f1);
                rainSizeX[i << 5 | j] = -f1 / f2;
                rainSizeZ[i << 5 | j] = f / f2;
            }
        }
    }

    public void setRainingAcid(boolean rainingAcid) {
        this.rainingAcid = rainingAcid;
    }

    public boolean renderRain(Level level, int ticks, LightTexture lightTexture, float partialTick, double x, double y, double z) {
        if (!rainingAcid)
            return false;

        float rainLevel = level.getRainLevel(partialTick);
        if (!(rainLevel <= 0.0F)) {
            lightTexture.turnOnLightLayer();
            int i = Mth.floor(x);
            int j = Mth.floor(y);
            int k = Mth.floor(z);
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tesselator.getBuilder();
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            int l = 5;

            if (Minecraft.useFancyGraphics()) {
                l = 10;
            }
            RenderSystem.depthMask(Minecraft.useShaderTransparency());
            int i1 = -1;
            float f1 = (float)ticks + partialTick;
            RenderSystem.setShader(GameRenderer::getParticleShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

            for(int j1 = k - l; j1 <= k + l; ++j1) {
                for(int k1 = i - l; k1 <= i + l; ++k1) {
                    int l1 = (j1 - k + 16) * 32 + k1 - i + 16;
                    double d0 = (double)this.rainSizeX[l1] * 0.5D;
                    double d1 = (double)this.rainSizeZ[l1] * 0.5D;
                    pos.set(k1, y, j1);
                    Biome biome = level.getBiome(pos).value();

                    if (biome.getPrecipitationAt(pos) != Biome.Precipitation.NONE) {
                        int i2 = level.getHeight(Heightmap.Types.MOTION_BLOCKING, k1, j1);
                        int j2 = j - l;
                        int k2 = j + l;
                        if (j2 < i2) {
                            j2 = i2;
                        }

                        if (k2 < i2) {
                            k2 = i2;
                        }

                        int l2 = i2;
                        if (i2 < j) {
                            l2 = j;
                        }

                        if (j2 != k2) {
                            RandomSource randomsource = RandomSource.create((long) k1 * k1 * 3121 + k1 * 45238971L ^ (long) j1 * j1 * 418711 + j1 * 13761L);
                            pos.set(k1, j2, j1);

                            if (biome.warmEnoughToRain(pos)) {
                                if (i1 != 0) {
                                    if (i1 >= 0) {
                                        tesselator.end();
                                    }

                                    i1 = 0;
                                    RenderSystem.setShaderTexture(0, RAIN_TEXTURE);
                                    bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
                                }

                                int i3 = ticks + k1 * k1 * 3121 + k1 * 45238971 + j1 * j1 * 418711 + j1 * 13761 & 31;
                                float f2 = -((float)i3 + partialTick) / 32.0F * (3.0F + randomsource.nextFloat());
                                double d2 = (double)k1 + 0.5D - x;
                                double d4 = (double)j1 + 0.5D - z;
                                float f3 = (float)Math.sqrt(d2 * d2 + d4 * d4) / (float)l;
                                float f4 = ((1.0F - f3 * f3) * 0.5F + 0.5F) * rainLevel;
                                pos.set(k1, l2, j1);
                                int j3 = getLightColor(level, pos);
                                bufferbuilder.vertex((double)k1 - x - d0 + 0.5D, (double)k2 - y, (double)j1 - z - d1 + 0.5D).uv(0.0F, (float)j2 * 0.25F + f2).color(RAIN_COLOR.x, RAIN_COLOR.y, RAIN_COLOR.z, f4).uv2(j3).endVertex();
                                bufferbuilder.vertex((double)k1 - x + d0 + 0.5D, (double)k2 - y, (double)j1 - z + d1 + 0.5D).uv(1.0F, (float)j2 * 0.25F + f2).color(RAIN_COLOR.x, RAIN_COLOR.y, RAIN_COLOR.z, f4).uv2(j3).endVertex();
                                bufferbuilder.vertex((double)k1 - x + d0 + 0.5D, (double)j2 - y, (double)j1 - z + d1 + 0.5D).uv(1.0F, (float)k2 * 0.25F + f2).color(RAIN_COLOR.x, RAIN_COLOR.y, RAIN_COLOR.z, f4).uv2(j3).endVertex();
                                bufferbuilder.vertex((double)k1 - x - d0 + 0.5D, (double)j2 - y, (double)j1 - z - d1 + 0.5D).uv(0.0F, (float)k2 * 0.25F + f2).color(RAIN_COLOR.x, RAIN_COLOR.y, RAIN_COLOR.z, f4).uv2(j3).endVertex();
                            }
                        }
                    }
                }
            }

            if (i1 >= 0) {
                tesselator.end();
            }

            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            lightTexture.turnOffLightLayer();
        }
        return true;
    }

    public boolean tickRain(Level level, int ticks, Camera camera) {
        if (!rainingAcid)
            return false;

        float rainLevel = level.getRainLevel(1.0F) / (Minecraft.useFancyGraphics() ? 1.0F : 2.0F);

        if (!(rainLevel <= 0.0F)) {
            Random random = new Random((long) ticks * 312987231L);
            BlockPos renderInfoPos = camera.getBlockPosition();
            BlockPos soundPos = null;
            int particleCount = (int) (100.0F * rainLevel * rainLevel) / 3;

            for (int i = 0; i < particleCount; ++i) {
                if (Minecraft.getInstance().options.particles().get() == ParticleStatus.MINIMAL) {
                    break;
                }

                int offsetX = random.nextInt(26) - 15;
                int offsetZ = random.nextInt(26) - 15;
                BlockPos groundPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, renderInfoPos.offset(offsetX, 0, offsetZ)).below();
                Biome biome = level.getBiome(groundPos).get();

                if (random.nextInt(5) > 0)
                    continue;

                if (groundPos.getY() > 0 && groundPos.getY() <= renderInfoPos.getY() + 10 && groundPos.getY() >= renderInfoPos.getY() - 10 && biome.getPrecipitationAt(groundPos) == Biome.Precipitation.RAIN && biome.warmEnoughToRain(groundPos)) {
                    soundPos = groundPos;

                    double xOffset = random.nextDouble();
                    double zOffset = random.nextDouble();
                    BlockState state = level.getBlockState(groundPos);
                    FluidState fluidState = level.getFluidState(groundPos);
                    VoxelShape collisionShape = state.getCollisionShape(level, groundPos);

                    double d2 = collisionShape.max(Direction.Axis.Y, xOffset, zOffset);
                    double d3 = fluidState.getHeight(level, groundPos);
                    double yOffset = Math.max(d2, d3);

                    ParticleOptions particleType = fluidState.is(FluidTags.WATER)
                            ? ParticleTypes.RAIN
                            : ParticleTypes.SMOKE;
                    level.addParticle(particleType, (double) groundPos.getX() + xOffset, (double) groundPos.getY() + yOffset, (double) groundPos.getZ() + zOffset, 0.0D, 0.0D, 0.0D);
                }
            }

            if (soundPos != null && random.nextInt(3) < rainSoundTime++) {
                rainSoundTime = 0;
                if (soundPos.getY() > renderInfoPos.getY() + 1 && level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, renderInfoPos).getY() > Mth.floor((float) renderInfoPos.getY())) {
                    level.playLocalSound(soundPos.getX(), soundPos.getY(), soundPos.getZ(), SoundEvents.WEATHER_RAIN_ABOVE, SoundSource.WEATHER, 0.1F, 0.5F, false);
                } else {
                    level.playLocalSound(soundPos.getX(), soundPos.getY(), soundPos.getZ(), SoundEvents.WEATHER_RAIN, SoundSource.WEATHER, 0.2F, 1.0F, false);
                }
            }
        }
        return true;
    }
}
