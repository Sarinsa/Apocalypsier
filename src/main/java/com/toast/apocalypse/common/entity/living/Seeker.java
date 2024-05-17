package com.toast.apocalypse.common.entity.living;

import com.toast.apocalypse.common.core.Apocalypse;
import com.toast.apocalypse.common.core.config.ApocalypseCommonConfig;
import com.toast.apocalypse.common.entity.living.ai.MobHurtByTargetGoal;
import com.toast.apocalypse.common.entity.living.ai.MoonMobPlayerTargetGoal;
import com.toast.apocalypse.common.entity.projectile.DestroyerFireballEntity;
import com.toast.apocalypse.common.entity.projectile.SeekerFireballEntity;
import com.toast.apocalypse.common.util.ApocalypseEventFactory;
import com.toast.apocalypse.common.util.MobHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * This is a full moon mob whose entire goal in life is to break through your defenses. It is similar to a ghast, only
 * it has unlimited target range that ignores line of sight and shoots creeper-sized fireballs when it does not have a
 * clear line of sight. When it does have direct vision, it shoots much weaker fireballs that can be easily reflected
 * back at the seeker. The seeker also alerts nearby monsters of the player's whereabouts when in it's direct line of sight.
 */
public class Seeker extends AbstractFullMoonGhast {

    private static final EntityDataAccessor<Boolean> ALERTING = SynchedEntityData.defineId(Seeker.class, EntityDataSerializers.BOOLEAN);
    private static final BiPredicate<Mob, Seeker> ALERT_PREDICATE = (mob, seeker) -> !(mob instanceof IFullMoonMob) && mob instanceof Enemy;

    /** The seeker's current target. Updated when the seeker alerts nearby mobs. */
    private LivingEntity currentTarget;

    public Seeker(EntityType<? extends Ghast> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 5;
    }

    public static AttributeSupplier.Builder createSeekerAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 12.0D)
                .add(Attributes.FOLLOW_RANGE, 4096.0D)
                .add(ForgeMod.SWIM_SPEED.get(), 1.1D);
    }

    public static boolean checkSeekerSpawnRules(EntityType<? extends Seeker> entityType, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL && Mob.checkMobSpawnRules(entityType, level, spawnType, pos, random);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new Seeker.AlertOtherMonstersGoal(this));
        goalSelector.addGoal(1, new Seeker.FireballAttackGoal(this));
        goalSelector.addGoal(2, new LookAroundGoal(this));
        goalSelector.addGoal(2, new Seeker.RandomOrRelativeToTargetFlyGoal(this));
        targetSelector.addGoal(0, new MobHurtByTargetGoal(this, Enemy.class));
        targetSelector.addGoal(1, new MoonMobPlayerTargetGoal<>(this, false));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false, false));
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(ALERTING, false);
    }

    public boolean isAlerting() {
        return entityData.get(ALERTING);
    }

    private void setAlerting(boolean alerting) {
        entityData.set(ALERTING, alerting);
    }

    private boolean canAlert() {
        return !isCharging();
    }

    /**
     * Completely ignore line of sight; the target
     * is always "visible"
     */
    @Override
    public boolean hasLineOfSight(Entity entity) {
        return true;
    }

    @Override
    public boolean hurt(DamageSource damageSource, float damage) {
        if (isInvulnerableTo(damageSource)) {
            return false;
        }
        else if (damageSource.getDirectEntity() instanceof SeekerFireballEntity || damageSource.getDirectEntity() instanceof DestroyerFireballEntity) {

            if (damageSource.getEntity() == this) {
                return false;
            }
            else {
                return super.hurt(damageSource, damage);
            }
        }
        else if (damageSource.is(DamageTypeTags.IS_EXPLOSION) && damageSource.getEntity() == this) {
            return false;
        }
        return super.hurt(damageSource, damage);
    }

    @Override
    public int getExplosionPower() {
        return this.explosionPower == 0 ? ApocalypseCommonConfig.COMMON.getSeekerExplosionPower() : this.explosionPower;
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevel, DifficultyInstance difficultyInstance, MobSpawnType spawnType, @Nullable SpawnGroupData data, @Nullable CompoundTag compoundTag) {
        data = super.finalizeSpawn(serverLevel, difficultyInstance, spawnType, data, compoundTag);

        if (compoundTag != null && compoundTag.contains("ExplosionPower", Tag.TAG_ANY_NUMERIC)) {
            this.explosionPower = compoundTag.getInt("ExplosionPower");
        }
        else {
            this.explosionPower = 0;
        }
        return data;
    }

    /** Essentially a copy of the ghast's fireball goal */
    private static class FireballAttackGoal extends Goal {

        private final Seeker seeker;
        public int chargeTime;

        public FireballAttackGoal(Seeker seeker) {
            this.seeker = seeker;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            if (seeker.getTarget() != null) {
                return !seeker.isAlerting();
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse();
        }

        @Override
        public void start() {
            chargeTime = 0;
        }

        @Override
        public void stop() {
            seeker.setCharging(false);
        }

        @Override
        public void tick() {
            LivingEntity target = seeker.getTarget();

            if (seeker.horizontalDistanceToSqr(target) < 4096.0D) {
                Level level = seeker.level();
                ++chargeTime;
                if (chargeTime == 10 && !seeker.isSilent()) {
                    level.levelEvent(null, 1015, seeker.blockPosition(), 0);
                }

                if (this.chargeTime == 20) {
                    Vec3 vec3 = seeker.getViewVector(1.0F);
                    double x = target.getX() - (seeker.getX() + vec3.x * 4.0D);
                    double y = target.getY(0.5D) - (0.5D + seeker.getY(0.5D));
                    double z = target.getZ() - (seeker.getZ() + vec3.z * 4.0D);

                    if (!this.seeker.isSilent()) {
                        level.levelEvent(null, 1016, seeker.blockPosition(), 0);
                    }
                    boolean canSeeTarget = seeker.canSeeDirectly(target);
                    SeekerFireballEntity fireball = new SeekerFireballEntity(level, seeker, canSeeTarget, x, y, z);
                    fireball.setPos(seeker.getX() + vec3.x * 4.0D, seeker.getY(0.5D) + 0.2D, fireball.getZ() + vec3.z * 4.0D);
                    level.addFreshEntity(fireball);

                    chargeTime = -40;
                }
            }
            else if (chargeTime > 0) {
                --chargeTime;
            }
            seeker.setCharging(chargeTime > 10);
        }
    }

    static class RandomOrRelativeToTargetFlyGoal extends Goal {

        private static final double maxDistanceBeforeFollow = 3000.0D;
        private final Seeker seeker;

        public RandomOrRelativeToTargetFlyGoal(Seeker seeker) {
            this.seeker = seeker;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            MoveControl moveControl = seeker.getMoveControl();

            if (!moveControl.hasWanted()) {
                return true;
            }
            else {
                double x = moveControl.getWantedX() - seeker.getX();
                double y = moveControl.getWantedY() - seeker.getY();
                double z = moveControl.getWantedZ() - seeker.getZ();
                double d3 = x * x + y * y + z * z;
                return d3 < 1.0D || d3 > 3600.0D;
            }
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        private void setRandomWantedPosition() {
            RandomSource random = seeker.getRandom();
            double x = seeker.getX() + (double) ((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
            double y = seeker.getY() + (double) ((random.nextFloat() * 2.0F - 1.0F) * 6.0F);
            double z = seeker.getZ() + (double) ((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
            this.seeker.getMoveControl().setWantedPosition(x, y, z, 1.0D);
        }

        @Override
        public void start() {
            if (seeker.getTarget() != null) {
                LivingEntity target = seeker.getTarget();
                double distanceToTarget = seeker.distanceToSqr(target);

                if (distanceToTarget > maxDistanceBeforeFollow) {
                    seeker.moveControl.setWantedPosition(target.getX(), target.getY() + 10.0D, target.getZ(), 1.0D);
                }
                else {
                    setRandomWantedPosition();
                }
            }
            else {
                setRandomWantedPosition();
            }
        }
    }

    public static class AlertOtherMonstersGoal extends Goal {

        private static final int maxAlertCount = 25;

        private final Seeker seeker;
        private int timeAlerting;

        public AlertOtherMonstersGoal(Seeker seeker) {
            this.seeker = seeker;
        }

        @Override
        public boolean canUse() {
            if (seeker.getTarget() != null) {
                return seeker.canAlert()
                        && seeker.distanceToSqr(seeker.getTarget()) < 4096.0D
                        && seeker.canSeeDirectly(seeker.getTarget())
                        && seeker.currentTarget != seeker.getTarget();
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return timeAlerting < 0;
        }

        @Override
        @SuppressWarnings("all")
        public void start() {
            LivingEntity target = seeker.getTarget();
            timeAlerting = -60;

            if (target != null) {
                AABB searchBox = target.getBoundingBox().inflate(60.0D, 30.0D, 60.0D);
                List<? extends Mob> toAlert = MobHelper.getLoadedEntitiesCapped(Mob.class, seeker.level(), searchBox, (entity) -> ALERT_PREDICATE.test(entity, seeker), maxAlertCount);

                if (toAlert.isEmpty()) {
                    // No need to perform further checks if the list is empty
                    timeAlerting = 0;
                    return;
                }
                ApocalypseEventFactory.fireSeekerAlertEvent(seeker.level(), seeker, toAlert, target);

                for (Mob mob : toAlert) {
                    if (mob.getTarget() != target) {
                        mob.setLastHurtByMob(null);
                        mob.setTarget(target);
                        AttributeInstance attributeInstance = mob.getAttribute(Attributes.FOLLOW_RANGE);
                        attributeInstance.setBaseValue(Math.max(attributeInstance.getValue(), 60.0D));
                    }
                }
                seeker.currentTarget = seeker.getTarget();
                seeker.setAlerting(true);
                seeker.playSound(SoundEvents.GHAST_SCREAM, 5.0F, 0.6F);
            }
        }

        @Override
        public void stop() {
            timeAlerting = 0;
            seeker.setAlerting(false);
        }

        @Override
        public void tick() {
            ++timeAlerting;
        }
    }
}
