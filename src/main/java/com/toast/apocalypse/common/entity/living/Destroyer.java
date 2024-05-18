package com.toast.apocalypse.common.entity.living;

import com.toast.apocalypse.common.core.config.ApocalypseCommonConfig;
import com.toast.apocalypse.common.entity.living.ai.MobHurtByTargetGoal;
import com.toast.apocalypse.common.entity.living.ai.MoonMobPlayerTargetGoal;
import com.toast.apocalypse.common.entity.projectile.DestroyerFireballEntity;
import com.toast.apocalypse.common.entity.projectile.SeekerFireballEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.ForgeSpawnEggItem;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Optional;

/**
 * This is a full moon mob similar to a ghast, though it has unlimited aggro range ignoring line of sight and
 * its fireballs can potentially destroy any type of block.<br>
 * <br>
 * In addition to the above, Destroyers can also attempt to destroy their target player's
 * respawn point, forcing the player to confront it.
 */
public class Destroyer extends AbstractFullMoonGhast {

    public static final EntityDataAccessor<Boolean> ATTACKED_BY_PT = SynchedEntityData.defineId(Destroyer.class, EntityDataSerializers.BOOLEAN);
    protected boolean isTargetingSpawnPoint = false;

    public Destroyer(EntityType<? extends Ghast> entityType, Level level) {
        super(entityType, level);
        moveControl = new MoveHelperController(this);
        this.xpReward = 5;
    }

    public static AttributeSupplier.Builder createDestroyerAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 12.0D)
                .add(Attributes.FOLLOW_RANGE, 4096.0D)
                .add(ForgeMod.SWIM_SPEED.get(), 1.1D);
    }

    public static boolean checkDestroyerSpawnRules(EntityType<? extends Destroyer> entityType, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL && Mob.checkMobSpawnRules(entityType, level, spawnType, pos, random);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(ATTACKED_BY_PT, false);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new DestroySpawnPointGoal<>(this));
        goalSelector.addGoal(1, new Destroyer.FireballAttackGoal(this));
        goalSelector.addGoal(1, new DestroyerLookAroundGoal(this));
        goalSelector.addGoal(2, new Destroyer.RandomOrRelativeToTargetFlyGoal(this));
        targetSelector.addGoal(0, new MobHurtByTargetGoal(this, Enemy.class));
        targetSelector.addGoal(1, new MoonMobPlayerTargetGoal<>(this, false));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false, false));
    }

    /**
     * @return True if this Destroyer at any point
     * has been attacked by their siege target player
     * since it spawned.
     */
    public boolean attackedBySiegeTarget() {
        return entityData.get(ATTACKED_BY_PT);
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
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
    public boolean isInvulnerableTo(DamageSource damageSource) {
        return !isReflectedFireball(damageSource)
                && (isRemoved()
                || isInvulnerable()
                && !damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                && !damageSource.isCreativePlayer()
                || damageSource.is(DamageTypeTags.IS_FIRE)
                && fireImmune()
                || damageSource.is(DamageTypeTags.IS_FALL));
    }

    private static boolean isReflectedFireball(DamageSource damageSource) {
        Entity entity = damageSource.getDirectEntity();

        return (entity instanceof LargeFireball || entity instanceof DestroyerFireballEntity) && damageSource.getEntity() instanceof Player;
    }

    @Override
    public boolean hurt(DamageSource damageSource, float damage) {
        if (isInvulnerableTo(damageSource)) {
            return false;
        }

        if (damageSource.getEntity() instanceof Player player) {
            // If attacked by siege target player, notify the Destroyer, so it
            // focuses on the player rather than their respawn point.
            if (getPlayerTargetUUID() != null && getPlayerTargetUUID() == player.getUUID()) {
                entityData.set(ATTACKED_BY_PT, true);
            }
        }

        // Prevent instant fireball death and return to sender advancement
        else if (damageSource.getDirectEntity() instanceof Fireball) {
            // Prevent the destroyer from damaging itself
            // when close up to a wall or solid obstacle
            if (damageSource.getEntity() == this)
                return false;

            if (damageSource.getEntity() instanceof Player player) {
                super.hurt(damageSources().playerAttack(player), 7.0F);
                return true;
            }
        }
        else if (damageSource.is(DamageTypeTags.IS_EXPLOSION) && damageSource.getEntity() == this) {
            return false;
        }
        return super.hurt(damageSource, damage);
    }

    @Override
    public int getExplosionPower() {
        return explosionPower == 0 ? ApocalypseCommonConfig.COMMON.getDestroyerExplosionPower() : explosionPower;
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevel, DifficultyInstance difficultyInstance, MobSpawnType spawnType, @Nullable SpawnGroupData data, @Nullable CompoundTag compoundTag) {
        data = super.finalizeSpawn(serverLevel, difficultyInstance, spawnType, data, compoundTag);

        if (compoundTag != null && compoundTag.contains("ExplosionPower", Tag.TAG_ANY_NUMERIC)) {
            explosionPower = compoundTag.getInt("ExplosionPower");
        }
        else {
            explosionPower = 0;
        }
        return data;
    }

    private boolean withinFiringRange(Vec3 vec3) {
        return horizontalDistanceToSqr(vec3) < 4096.0D;
    }

    /** Essentially a copy of the ghast's fireball goal */
    private static class FireballAttackGoal extends Goal {

        private final Destroyer destroyer;
        public int chargeTime;

        public FireballAttackGoal(Destroyer destroyer) {
            this.destroyer = destroyer;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            return destroyer.getTarget() != null && !destroyer.isTargetingSpawnPoint;
        }

        @Override
        public void start() {
            chargeTime = 0;
        }

        @Override
        public void stop() {
            destroyer.setCharging(false);
        }

        @Override
        public void tick() {
            LivingEntity target = destroyer.getTarget();

            if (destroyer.withinFiringRange(target.position()) && destroyer.hasLineOfSight(target)) {
                Level level = destroyer.level();
                ++chargeTime;
                if (chargeTime == 10 && !destroyer.isSilent()) {
                    level.levelEvent(null, 1015, destroyer.blockPosition(), 0);
                }

                if (chargeTime == 20) {
                    Vec3 vec3 = destroyer.getViewVector(1.0F);
                    double x = target.getX() - (destroyer.getX() + vec3.x * 4.0D);
                    double y = target.getY(0.5D) - (0.5D + destroyer.getY(0.5D));
                    double z = target.getZ() - (destroyer.getZ() + vec3.z * 4.0D);

                    if (!destroyer.isSilent()) {
                        level.levelEvent(null, 1016, destroyer.blockPosition(), 0);
                    }
                    DestroyerFireballEntity fireball = new DestroyerFireballEntity(level, destroyer, x, y, z);
                    fireball.setPos(destroyer.getX() + vec3.x * 4.0D, destroyer.getY(0.5D) + 0.5D, fireball.getZ() + vec3.z * 4.0D);
                    level.addFreshEntity(fireball);
                    chargeTime = -40;
                }
            }
            else if (chargeTime > 0) {
                --chargeTime;
            }
            destroyer.setCharging(chargeTime > 10);
        }
    }

    static class RandomOrRelativeToTargetFlyGoal extends Goal {

        private static final double maxDistanceBeforeFollow = 3000.0D;
        private final Destroyer destroyer;

        public RandomOrRelativeToTargetFlyGoal(Destroyer destroyer) {
            this.destroyer = destroyer;
            setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            MoveControl controller = destroyer.getMoveControl();

            if (!controller.hasWanted()) {
                return true;
            }
            else {
                double x = controller.getWantedX() - destroyer.getX();
                double y = controller.getWantedY() - destroyer.getY();
                double z = controller.getWantedZ() - destroyer.getZ();
                double d3 = x * x + y * y + z * z;
                return d3 < 1.0D || d3 > 3600.0D;
            }
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        private void setRandomWantedPosition() {
            RandomSource random = destroyer.getRandom();
            double x = destroyer.getX() + (double) ((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
            double y = destroyer.getY() + (double) ((random.nextFloat() * 2.0F - 1.0F) * 10.0F);
            double z = destroyer.getZ() + (double) ((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
            destroyer.getMoveControl().setWantedPosition(x, y, z, 1.0D);
        }

        @Override
        public void start() {
            MoveControl controller = destroyer.getMoveControl();

            if (destroyer.getTarget() != null) {
                Optional<Vec3> respawnPos = Optional.empty();

                if (ApocalypseCommonConfig.COMMON.getDestroyerTargetRespawnPos() && destroyer.getTarget() instanceof ServerPlayer serverPlayer && !destroyer.attackedBySiegeTarget()) {
                    if (destroyer.getPlayerTargetUUID() != null && destroyer.getPlayerTargetUUID() == serverPlayer.getUUID()) {
                        BlockPos pos = serverPlayer.getRespawnPosition();

                        if (isPlayerSpawnValid(pos, destroyer.level())) {
                            double x = pos.getX();
                            double y = pos.getY() + 10.0D;
                            double z = pos.getZ();

                            if (destroyer.canReachDist(x, y, z, 10)) {
                                respawnPos = Optional.of(new Vec3(x, y, z));
                            }
                        }
                    }
                }
                LivingEntity target = destroyer.getTarget();
                double distanceToTarget = destroyer.distanceToSqr(target);

                if (respawnPos.isPresent()) {
                    Vec3 vec3 = respawnPos.get();

                    if (!destroyer.withinFiringRange(vec3))
                        controller.setWantedPosition(vec3.x, vec3.y, vec3.z, 1.0D);
                }
                else {
                    if (distanceToTarget > maxDistanceBeforeFollow) {
                        controller.setWantedPosition(target.getX(), target.getY() + 10.0D, target.getZ(), 1.0D);
                    }
                    else {
                        setRandomWantedPosition();
                    }
                }
            }
            else {
                setRandomWantedPosition();
            }
        }
    }

    private static class DestroySpawnPointGoal<T extends Destroyer> extends Goal {

        private final T destroyer;
        private BlockPos respawnPos;
        public int chargeTime;

        private DestroySpawnPointGoal(T destroyer) {
            this.destroyer = destroyer;

        }

        @Override
        public boolean canUse() {
            if (!ApocalypseCommonConfig.COMMON.getDestroyerTargetRespawnPos())
                return false;

            if (IFullMoonMob.getEventTarget(destroyer) instanceof ServerPlayer targetPlayer && !destroyer.attackedBySiegeTarget()) {
                if (targetPlayer.getRespawnPosition() != null && (targetPlayer.getRespawnDimension().equals(destroyer.level().dimension())) && isPlayerSpawnValid(targetPlayer.getRespawnPosition(), destroyer.level())) {
                    respawnPos = targetPlayer.getRespawnPosition();
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            if (!ApocalypseCommonConfig.COMMON.getDestroyerTargetRespawnPos())
                return false;

            if (IFullMoonMob.getEventTarget(destroyer) instanceof ServerPlayer targetPlayer && !destroyer.attackedBySiegeTarget()) {
                if (respawnPos != null && (targetPlayer.getRespawnDimension().equals(destroyer.level().dimension()))) {
                    return isPlayerSpawnValid(respawnPos, destroyer.level());
                }
            }
            return false;
        }

        @Override
        public void start() {
            chargeTime = 0;
            destroyer.isTargetingSpawnPoint = true;
        }

        @Override
        public void stop() {
            respawnPos = null;
            destroyer.setCharging(false);
            destroyer.isTargetingSpawnPoint = false;
        }

        @Override
        public void tick() {
            if (destroyer.horizontalDistanceToSqr(respawnPos) < 4096.0D) {
                Level level = destroyer.level();
                ++chargeTime;

                if (chargeTime == 10 && !destroyer.isSilent()) {
                    level.levelEvent(null, 1015, destroyer.blockPosition(), 0);
                }

                if (chargeTime == 20) {
                    Vec3 vec3 = destroyer.getViewVector(1.0F);
                    double x = respawnPos.getX() - (destroyer.getX() + vec3.x * 4.0D);
                    double y = respawnPos.getY() - (0.5D + destroyer.getY(0.5D));
                    double z = respawnPos.getZ() - (destroyer.getZ() + vec3.z * 4.0D);

                    if (!destroyer.isSilent()) {
                        level.levelEvent(null, 1016, destroyer.blockPosition(), 0);
                    }
                    DestroyerFireballEntity fireball = new DestroyerFireballEntity(level, destroyer, x, y, z);
                    fireball.setPos(destroyer.getX() + vec3.x * 4.0D, destroyer.getY(0.5D) + 0.5D, fireball.getZ() + vec3.z * 4.0D);
                    level.addFreshEntity(fireball);
                    chargeTime = -40;
                }
            }
            else if (chargeTime > 0) {
                --chargeTime;
            }
            destroyer.setCharging(chargeTime > 10);
        }
    }

    protected static class DestroyerLookAroundGoal extends Goal {
        private final Destroyer destroyer;

        public DestroyerLookAroundGoal(Destroyer destroyer) {
            this.destroyer = destroyer;
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            return true;
        }

        public void tick() {
            if (destroyer.getTarget() == null) {
                Vec3 vec3 = destroyer.getDeltaMovement();
                destroyer.setYRot(-((float) Mth.atan2(vec3.x, vec3.z)) * (180F / (float)Math.PI));
            }
            else if (destroyer.getTarget() instanceof ServerPlayer serverPlayer) {
                double x, z;

                if (!destroyer.attackedBySiegeTarget() && serverPlayer.getRespawnPosition() != null && (serverPlayer.getRespawnDimension().equals(destroyer.level().dimension())) && isPlayerSpawnValid(serverPlayer.getRespawnPosition(), destroyer.level())) {
                    BlockPos respawnPos = serverPlayer.getRespawnPosition();
                    x = respawnPos.getX() - destroyer.getX();
                    z = respawnPos.getZ() - destroyer.getZ();
                }
                else {
                    x = serverPlayer.getX() - destroyer.getX();
                    z = serverPlayer.getZ() - destroyer.getZ();
                }
                destroyer.setYRot(-((float)Mth.atan2(x, z)) * (180F / (float)Math.PI));
            }
            else {
                LivingEntity target = destroyer.getTarget();

                double x = target.getX() - destroyer.getX();
                double z = target.getZ() - destroyer.getZ();
                destroyer.setYRot(-((float) Mth.atan2(x, z)) * (180F / (float)Math.PI));
            }
            destroyer.yBodyRot = destroyer.getYRot();
        }
    }

    /**
     * Checks if the player's respawn point is still valid
     * after possibly having been destroyed.
     */
    private static boolean isPlayerSpawnValid(@Nullable BlockPos pos, Level level) {
        if (pos == null)
            return false;

        Block block = level.getBlockState(pos).getBlock();
        return block instanceof BedBlock || block instanceof RespawnAnchorBlock;
    }
}
