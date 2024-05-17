package com.toast.apocalypse.common.entity.living;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidType;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.UUID;

public abstract class AbstractFullMoonGhast extends Ghast implements IFullMoonMob {

    protected UUID playerTargetUUID;
    protected int eventGeneration = 0;

    public AbstractFullMoonGhast(EntityType<? extends Ghast> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * Used for the seeker and destroyer when shooting fireballs
     * at their target. Only X and Z distance is checked to prevent
     * players from being able to hide far underground to avoid
     * the constant barrage of explosions that is waiting for them.
     */
    public final double horizontalDistanceToSqr(Entity entity) {
        return horizontalDistanceToSqr(entity.position());
    }

    public final double horizontalDistanceToSqr(BlockPos pos) {
        return horizontalDistanceToSqr(new Vec3(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D));
    }

    public final double horizontalDistanceToSqr(Vec3 vec) {
        double x = this.getX() - vec.x;
        double z = this.getZ() - vec.z;
        return x * x + z * z;
    }

    @Override
    public boolean canDrownInFluidType(FluidType type) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    /**
     * Checks if this ghast type has direct
     * line of sight to the target entity.
     */
    public boolean canSeeDirectly(Entity entity) {
        Vec3 vector3d = new Vec3(this.getX(), this.getEyeY(), this.getZ());
        Vec3 vector3d1 = new Vec3(entity.getX(), entity.getEyeY(), entity.getZ());
        return level().clip(new ClipContext(vector3d, vector3d1, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }

    @Nullable
    @Override
    public UUID getPlayerTargetUUID() {
        return this.playerTargetUUID;
    }

    @Override
    public void setPlayerTargetUUID(@Nullable UUID playerTargetUUID) {
        this.playerTargetUUID = playerTargetUUID;
    }

    @Override
    public int getEventGeneration() {
        return eventGeneration;
    }

    @Override
    public void setEventGeneration(int generation) {
        eventGeneration = generation;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!level().isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level();

            if (IFullMoonMob.shouldDisappear(getPlayerTargetUUID(), serverLevel, this)) {
                IFullMoonMob.spawnSmoke(serverLevel, this);
                discard();
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);

        if (this.getPlayerTargetUUID() != null) {
            compoundTag.putUUID(PLAYER_UUID_KEY, getPlayerTargetUUID());
            compoundTag.putInt(EVENT_GEN_KEY, getEventGeneration());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);

        if (compoundTag.hasUUID(PLAYER_UUID_KEY)) {
            setPlayerTargetUUID(compoundTag.getUUID(PLAYER_UUID_KEY));
        }
    }

    protected boolean canReachDist(double x, double y, double z, int dist) {
        Vec3 vector3d = new Vec3(x - getX(), y - getY(), z - getZ());
        vector3d = vector3d.normalize();

        AABB aabb = getBoundingBox().inflate(0.5F);

        for (int i = 0; i < dist; i++) {
            aabb = aabb.move(vector3d);

            if (!level().noCollision(this, aabb)) {
                return false;
            }
        }
        return true;
    }

    /** Slightly modified version of the ghast's movement controller */
    protected static class MoveHelperController extends MoveControl {

        private final AbstractFullMoonGhast ghast;
        private int floatDuration;
        private boolean canReachCurrent;

        public MoveHelperController(AbstractFullMoonGhast ghast) {
            super(ghast);
            this.ghast = ghast;
        }

        @Override
        public void setWantedPosition(double x, double y, double z, double speedMod) {
            super.setWantedPosition(x, y, z, speedMod);
        }

        public void setAction(Operation operation) {
            this.operation = operation;
        }


        @Override
        public void tick() {
            if (operation == Operation.MOVE_TO) {
                if (floatDuration-- <= 0) {
                    floatDuration += ghast.getRandom().nextInt(5) + 2;
                    Vec3 vec3 = new Vec3(wantedX - ghast.getX(), wantedY - ghast.getY(), wantedZ - ghast.getZ());
                    vec3 = vec3.normalize();

                    canReachCurrent = canReach(vec3);

                    if (canReachCurrent) {
                        ghast.setDeltaMovement(ghast.getDeltaMovement().add(vec3.scale(speedModifier / 10.0D)));
                    }
                    else {
                        operation = Operation.WAIT;
                    }
                }
            }
        }

        private boolean canReach(Vec3 vec) {
            AABB aabb = ghast.getBoundingBox().inflate(0.5F);

            aabb = aabb.move(vec);
            return ghast.level().noCollision(ghast, aabb);
        }

        public boolean canReachCurrentWanted() {
            return canReachCurrent;
        }
    }

    /** Copied from ghast */
    protected static class LookAroundGoal extends Goal {
        private final AbstractFullMoonGhast ghast;

        public LookAroundGoal(AbstractFullMoonGhast ghast) {
            this.ghast = ghast;
            setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return true;
        }

        public void tick() {
            if (ghast.getTarget() == null) {
                Vec3 vec3 = ghast.getDeltaMovement();
                ghast.setYRot(-((float) Mth.atan2(vec3.x, vec3.z)) * (180F / (float)Math.PI));
            }
            else {
                LivingEntity target = ghast.getTarget();

                double x = target.getX() - ghast.getX();
                double z = target.getZ() - ghast.getZ();
                ghast.setYRot(-((float)Mth.atan2(x, z)) * (180F / (float)Math.PI));
            }
            ghast.yBodyRot = ghast.getYRot();
        }
    }
}
