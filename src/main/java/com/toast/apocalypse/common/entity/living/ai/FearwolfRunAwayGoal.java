package com.toast.apocalypse.common.entity.living.ai;

import com.toast.apocalypse.common.entity.living.Fearwolf;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;


public class FearwolfRunAwayGoal extends Goal {

    private final Fearwolf fearwolf;
    private final double speedMul;
    private Path path;

    public FearwolfRunAwayGoal(Fearwolf fearwolf, double speedMul) {
        this.fearwolf = fearwolf;
        this.speedMul = speedMul;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean canUse() {
        if (fearwolf.runningAway() && fearwolf.isAlive() && !fearwolf.isVehicle() && !fearwolf.isPassenger()) {
            Vec3 vec3 = DefaultRandomPos.getPosAway(fearwolf, 16, 7, fearwolf.position());

            if (vec3 == null) {
                return false;
            }
            else {
                path = fearwolf.getNavigation().createPath(vec3.x, vec3.y, vec3.z, 0);
                return path != null;
            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return fearwolf.isAlive() && !fearwolf.isVehicle() && !fearwolf.isPassenger()
                && fearwolf.getNavigation().isDone();
    }

    @Override
    public void start() {
        if (!fearwolf.level().isClientSide) {
            ServerLevel level = (ServerLevel) fearwolf.level();
            spawnSmoke(level, fearwolf);
        }
        fearwolf.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 150, 0));
        fearwolf.getNavigation().moveTo(path, speedMul);
        fearwolf.setRunningAway(false);
    }

    private static void spawnSmoke(ServerLevel level, Mob mob) {
        for (int i = 0; i < 15; i++) {
            level.sendParticles(ParticleTypes.SMOKE, mob.getX(), mob.getY(), mob.getZ(), 4, 0.1, 0.1, 0.1, 0.1);
        }
    }
}
