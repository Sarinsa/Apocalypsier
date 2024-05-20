package com.toast.apocalypse.common.entity.living;

import com.toast.apocalypse.common.core.register.ApocalypseEffects;
import com.toast.apocalypse.common.core.register.ApocalypseSounds;
import com.toast.apocalypse.common.entity.living.ai.MobHurtByTargetGoal;
import com.toast.apocalypse.common.entity.living.ai.MoonMobPlayerTargetGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidType;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.UUID;

/**
 * This is a full moon mob that has the odd ability to completely ignore block collision. To compliment this, it has
 * unlimited aggro range and ignores line of sight.
 * These are the bread and butter of invasions. Ghosts deal light damage that can't be reduced below 1 and apply
 * a short increased gravity effect to help deal with flying players.
 *
 * The ghost will also occasionally maneuver away if damaged, potentially phasing
 * through walls and disorienting the target.
 *
 * //TODO - "Freezing Counter": Provide the player with a gadget for temporarily
 *          immobilizing/freezing ghosts in place.
 */
public class Ghost extends FlyingMob implements Enemy, IFullMoonMob {

    /**
     *  Used to determine if the ghost should be frozen in place
     *  and if the time freeze render should be rendered.<br>
     */
    private static final EntityDataAccessor<Boolean> IS_FROZEN = SynchedEntityData.defineId(Ghost.class, EntityDataSerializers.BOOLEAN);

    /** The constant player target, if this mob was spawned by the full moon event */
    private UUID playerTargetUUID;
    protected int eventGeneration = 0;
    /** If the ghost should move away from its target in a random direction */
    private boolean isManeuvering;
    /** How long the ghost should be frozen in ticks */
    private int freezeTime = 0;


    public Ghost(EntityType<? extends FlyingMob> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new GhostMovementController<>(this);
        this.xpReward = 3;
    }

    public static AttributeSupplier.Builder createGhostAttributes() {
        return FlyingMob.createMobAttributes()
                .add(Attributes.ATTACK_DAMAGE, 1.0D)
                .add(Attributes.MAX_HEALTH, 4.0D)
                .add(Attributes.FLYING_SPEED, 0.50D)
                .add(Attributes.FOLLOW_RANGE, 4096.0D);
    }

    public static boolean checkGhostSpawnRules(EntityType<? extends Ghost> entityType, ServerLevelAccessor level, MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL && Monster.isDarkEnoughToSpawn(level, pos, random);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new Ghost.ManeuverAttackerGoal<>(this));
        this.goalSelector.addGoal(1, new Ghost.MeleeAttackGoal<>(this));
        this.goalSelector.addGoal(2, new RandomFlyGoal(this));
        this.goalSelector.addGoal(3, new GhostLookAtGoal(this, Player.class,8.0F));
        this.targetSelector.addGoal(0, new MobHurtByTargetGoal(this, Enemy.class));
        this.targetSelector.addGoal(1, new MoonMobPlayerTargetGoal<>(this, false));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false, false));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.getEntityData().define(IS_FROZEN, false);
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return true;
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
        return 1.80F;
    }

    @Override
    public void knockback(double strength, double xRatio, double zRatio) {
        if (!isFrozen())
            super.knockback(strength, xRatio, zRatio);
    }

    @Override
    public boolean hurt(DamageSource damageSource, float damage) {
        Entity entity = damageSource.getEntity();

        if (entity instanceof LivingEntity livingEntity) {
            if (!isFrozen() && livingEntity.getItemInHand(InteractionHand.MAIN_HAND).getItem() == Items.BEDROCK) {
                freeze(200);
            }
        }
        if (super.hurt(damageSource, damage)) {
            if (!isFrozen() && entity != null && entity == getTarget() && random.nextInt(2) == 0) {
                setManeuvering(true);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        if (super.doHurtTarget(entity)) {
            if (entity instanceof Player player) {
                int duration = level().getDifficulty() == Difficulty.HARD ? 140 : 80;
                player.addEffect(new MobEffectInstance(ApocalypseEffects.HEAVY.get(), duration));
            }
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public void aiStep() {
        if (isAlive()) {
            boolean flag = isSunBurnTick();

            if (flag) {
                ItemStack itemstack = getItemBySlot(EquipmentSlot.HEAD);
                if (!itemstack.isEmpty()) {
                    if (itemstack.isDamageableItem()) {
                        itemstack.setDamageValue(itemstack.getDamageValue() + random.nextInt(2));
                        if (itemstack.getDamageValue() >= itemstack.getMaxDamage()) {
                            broadcastBreakEvent(EquipmentSlot.HEAD);
                            setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                        }
                    }
                    flag = false;
                }
                if (flag) {
                    setSecondsOnFire(8);
                }
            }
            if (!level().isClientSide) {
                if (freezeTime > 0) {
                    --freezeTime;

                    if (freezeTime <= 0 && isFrozen()) {
                        unfreeze();
                    }
                }
            }
        }
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
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
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
    public boolean isPushable() {
        return false; // Cannot be pushed by entities
    }

    @Override
    protected void doPush(Entity entity) {
        // Does not push other entities
    }
    @Override
    public boolean canDrownInFluidType(FluidType type) {
        return false;
    }

    @Override
    public boolean isPushedByFluid(FluidType fluidType) {
        return false; // Not pushed by fluids
    }

    @Override
    protected boolean isAffectedByFluids() {
        return false; // Not affected by fluids
    }

    @Override
    public void lavaHurt() {
        // Immune to lava
    }

    public boolean isManeuvering() {
        return this.isManeuvering;
    }

    protected void setManeuvering(boolean maneuvering) {
        this.isManeuvering = maneuvering;
    }

    public boolean isFrozen() {
        return this.entityData.get(IS_FROZEN);
    }

    public void freeze(int freezeTime) {
        this.entityData.set(IS_FROZEN, true);
        this.freezeTime = freezeTime;

        if (level() != null && !level().isClientSide) {
            level().broadcastEntityEvent(this, (byte) 7);
        }
        if (!level().isClientSide) {
            playSound(ApocalypseSounds.GHOST_FREEZE.get(), 1.0F, 1.0F - (random.nextFloat() / 5));
        }
    }

    private void unfreeze() {
        this.entityData.set(IS_FROZEN, false);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleEntityEvent(byte eventId) {
        if (eventId == 7) {
            displayFreezeParticles();
        }
        else {
            super.handleEntityEvent(eventId);
        }
    }

    private void displayFreezeParticles() {
        for (int i = 0; i < 13; i++) {
            level().addParticle(
                    ParticleTypes.END_ROD,
                    getX() + 0.5D,
                    getY() + 0.5D,
                    getZ() + 0.5D,
                    random.nextGaussian() / 3,
                    random.nextGaussian() / 3,
                    random.nextGaussian() / 3);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return isFrozen() ? null : SoundEvents.BLAZE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ENDERMAN_SCREAM;
    }

    @Override
    protected void playHurtSound(DamageSource damageSource) {
        if (isFrozen())
            return;

        super.playHurtSound(damageSource);
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BLAZE_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        // No step sound
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEAD;
    }

    @Override
    public void tick() {
        this.noPhysics = true;
        super.tick();
        this.noPhysics = false;
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
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);

        if (this.getPlayerTargetUUID() != null) {
            compoundTag.putUUID(PLAYER_UUID_KEY, this.getPlayerTargetUUID());
        }
        compoundTag.putInt("FreezeTime", this.freezeTime);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);

        if (compoundTag.hasUUID(PLAYER_UUID_KEY)) {
            this.setPlayerTargetUUID(compoundTag.getUUID(PLAYER_UUID_KEY));
        }
        if (compoundTag.contains("FreezeTime", Tag.TAG_ANY_NUMERIC)) {
            this.freezeTime = compoundTag.getInt("FreezeTime");
        }
    }

    private static class GhostMovementController<T extends Ghost> extends MoveControl {

        final T ghostEntity;

        public GhostMovementController(T ghost) {
            super(ghost);
            this.ghostEntity = ghost;
        }

        public void tick() {
            if (operation == Operation.MOVE_TO) {

                T ghost = ghostEntity;

                if (ghost.isFrozen())
                    return;

                Vec3 vec3 = new Vec3(wantedX - ghost.getX(), wantedY - ghost.getY(), wantedZ - ghost.getZ());
                double d0 = vec3.length();

                if (d0 < ghost.getBoundingBox().getSize()) {
                    operation = Operation.WAIT;
                    ghost.setDeltaMovement(ghost.getDeltaMovement().scale(0.5D));
                }
                else {
                    ghost.setDeltaMovement(ghost.getDeltaMovement().add(vec3.scale(speedModifier * 0.05D / d0)));

                    if (ghostEntity.getTarget() == null) {
                        Vec3 velocity = ghost.getDeltaMovement();
                        ghost.setYRot(-((float) Mth.atan2(velocity.x, velocity.z)) * (180F / (float)Math.PI));
                    }
                    else {
                        double xDist = ghost.getTarget().getX() - ghost.getX();
                        double zDist = ghost.getTarget().getZ() - ghost.getZ();
                        ghost.setYRot(-((float)Mth.atan2(xDist, zDist)) * (180F / (float)Math.PI));
                    }
                    ghost.yBodyRot = ghost.getYRot();
                }
            }
        }
    }

    private static class ManeuverAttackerGoal<T extends Ghost> extends Goal {

        private final T ghost;

        public ManeuverAttackerGoal(T ghost) {
            this.ghost = ghost;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return ghost.getTarget() != null && ghost.isManeuvering();
        }

        @Override
        public boolean canContinueToUse() {
            return ghost.getTarget() != null && ghost.getTarget().isAlive() && ghost.isManeuvering() && ghost.moveControl.hasWanted();
        }

        @Override
        public void start() {
            RandomSource random = ghost.getRandom();
            final double speed = ghost.getAttributeValue(Attributes.FLYING_SPEED) * 2;
            ghost.moveControl.setWantedPosition(ghost.getX() + (random.nextGaussian() * 10), ghost.getY() + (random.nextGaussian() * 10), ghost.getZ() + (random.nextGaussian() * 10), speed);
        }

        @Override
        public void stop() {
            ghost.setManeuvering(false);
        }
    }

    private static class MeleeAttackGoal<T extends Ghost> extends Goal {

        private final T ghost;
        private int ticksUntilNextAttack;

        public MeleeAttackGoal(T ghost) {
            setFlags(EnumSet.of(Goal.Flag.MOVE));
            this.ghost = ghost;
        }

        private void setWantedPosition(LivingEntity target) {
            Vec3 vec3 = target.getEyePosition(1.0F).add(0.0D, -(ghost.getBbHeight() / 1.8), 0.0D);
            final double speed = ghost.getAttributeValue(Attributes.FLYING_SPEED);
            ghost.moveControl.setWantedPosition(vec3.x, vec3.y, vec3.z, speed);
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            return !ghost.isFrozen() && ghost.getTarget() != null && ghost.getTarget().isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public void start() {
            ghost.setAggressive(true);
        }

        @Override
        public void stop() {
            LivingEntity target = ghost.getTarget();
            if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(target)) {
                ghost.setTarget(null);
            }
            ghost.setAggressive(false);
        }

        @Override
        public void tick() {
            LivingEntity target = ghost.getTarget();
            double distance = ghost.distanceToSqr(target);

            if (!ghost.isManeuvering() && ghost.tickCount % 20 == 0) {
                setWantedPosition(target);
            }
            if (ticksUntilNextAttack <= 0) {
                if (canAttackReach(target, distance)) {
                    ghost.doHurtTarget(target);
                    ticksUntilNextAttack = 20;
                }
            }
            else {
                --ticksUntilNextAttack;
            }
        }

        private boolean canAttackReach(LivingEntity target, double distance) {
            return distance <= (double)(ghost.getBbWidth() * 2.0F * ghost.getBbWidth() * 2.0F + target.getBbWidth());
        }
    }

    /** Copied from ghast */
    static class RandomFlyGoal extends Goal {

        private final Ghost ghost;

        public RandomFlyGoal(Ghost ghost) {
            this.ghost = ghost;
            setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            MoveControl moveControl = ghost.getMoveControl();

            if (ghost.getTarget() != null || ghost.isManeuvering())
                return false;

            if (!moveControl.hasWanted()) {
                return true;
            }
            else {
                double x = moveControl.getWantedX() - ghost.getX();
                double y = moveControl.getWantedY() - ghost.getY();
                double z = moveControl.getWantedZ() - ghost.getZ();
                double d3 = x * x + y * y + z * z;
                return d3 < 1.0D || d3 > 3600.0D;
            }
        }

        @Override
        public void stop() {
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            RandomSource random = ghost.getRandom();
            final double x = ghost.getX() + (double)((random.nextFloat() * 2.0F - 1.0F) * 8.0F);
            final double y = ghost.getY() + (double)((random.nextFloat() * 2.0F - 1.0F) * 8.0F);
            final double z = ghost.getZ() + (double)((random.nextFloat() * 2.0F - 1.0F) * 8.0F);
            final double speed = ghost.getAttributeValue(Attributes.FLYING_SPEED);
            ghost.getMoveControl().setWantedPosition(x, y, z, speed);
        }
    }

    static class GhostLookAtGoal extends LookAtPlayerGoal {

        private final Ghost ghost;

        public GhostLookAtGoal(Ghost ghost, Class<? extends LivingEntity> lookAt, float range) {
            super(ghost, lookAt, range);
            setFlags(EnumSet.of(Flag.LOOK));
            this.ghost = ghost;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            return super.canUse() && !ghost.isFrozen();
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && !ghost.isFrozen();
        }
    }
}
