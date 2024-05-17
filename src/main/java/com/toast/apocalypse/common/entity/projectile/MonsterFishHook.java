package com.toast.apocalypse.common.entity.projectile;

import com.toast.apocalypse.common.core.register.ApocalypseEntities;
import com.toast.apocalypse.common.entity.living.Grump;
import com.toast.apocalypse.common.network.NetworkHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;

/**
 * This is a fishhook projectile that can be fired by monsters to pull targets closer.<br>
 * Players are able to block this projectile with a shield, negating its effects.
 *
 * Essentially a copy-paste of {@link net.minecraft.world.entity.projectile.FishingHook}
 */
public class MonsterFishHook extends Projectile implements IEntityAdditionalSpawnData {

    private static final EntityDataAccessor<Integer> DATA_HOOKED_ENTITY = SynchedEntityData.defineId(MonsterFishHook.class, EntityDataSerializers.INT);
    private int life;
    private Entity hookedIn;
    private State currentState = State.FLYING;
    /** Whether this fishhook was launched by a player. */
    private boolean launchedOnCommand = false;


    public MonsterFishHook(EntityType<? extends MonsterFishHook> entityType, Level level) {
        super(entityType, level);
    }

    private MonsterFishHook(Level level, Mob mob) {
        super(ApocalypseEntities.MONSTER_FISH_HOOK.get(), level);
        this.setOwner(mob);
        this.noCulling = true;
    }

    public MonsterFishHook(Mob mob, LivingEntity target, Level level) {
        this(level, mob);

        final Vec3 lookVec = mob.getViewVector(1.0F).scale(mob.getBbWidth());
        this.setPos(mob.getX() + lookVec.x, mob.getEyeY() - 0.1, mob.getZ() + lookVec.z);

        final double dX = target.getX() - getX();
        final double dY = target.getY( 0.3333 ) - getY();
        final double dZ = target.getZ() - getZ();
        final double dH = Mth.sqrt((float) (dX * dX + dZ * dZ));
        this.shoot(dX, dY + dH * 0.2, dZ, 1.3F, 0);
    }

    public MonsterFishHook(LivingEntity rider, Mob mob, Level level) {
        this(level, mob);
        launchedOnCommand = true;

        final Vec3 riderLookVec = rider.getViewVector(1.0F).scale(rider.getBbWidth());
        final Vec3 grumpLookVec = mob.getViewVector(1.0F).scale(mob.getBbWidth());
        this.setPos(mob.getX() + grumpLookVec.x, mob.getEyeY() - 0.1, mob.getZ() + grumpLookVec.z);

        final double dX = riderLookVec.x() - getX();
        final double dY = riderLookVec.y() - getY();
        final double dZ = riderLookVec.z() - getZ();
        final double dH = Mth.sqrt((float) (dX * dX + dZ * dZ));
        this.shoot(dX, dY + dH * 0.2, dZ, 1.3F, 0);
    }

    @Override
    protected void defineSynchedData() {
        this.getEntityData().define(DATA_HOOKED_ENTITY, 0);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> dataParameter) {
        if (DATA_HOOKED_ENTITY.equals(dataParameter)) {
            int i = this.getEntityData().get(DATA_HOOKED_ENTITY);
            this.hookedIn = i > 0 ? level().getEntity(i - 1) : null;
        }
        super.onSyncedDataUpdated(dataParameter);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 4096.0D;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void lerpTo(double parameter, double mappings, double would, float be, float nice, int to, boolean have) {
    }

    @Override
    public void tick() {
        super.tick();
        LivingEntity livingEntity = this.getLivingOwner();

        if (livingEntity == null) {
            discard();
        }
        else if (level().isClientSide || !shouldStopFishing(livingEntity)) {
            if (onGround()) {
                ++life;
                if (life >= 1200) {
                    discard();
                    return;
                }
            }
            else {
                life = 0;
            }
            float f = 0.0F;
            BlockPos blockpos = blockPosition();
            FluidState fluidstate = level().getFluidState(blockpos);

            if (fluidstate.is(FluidTags.WATER)) {
                f = fluidstate.getHeight(level(), blockpos);
            }
            boolean flag = f > 0.0F;

            if (currentState == State.FLYING) {
                if (hookedIn != null) {
                    setDeltaMovement(Vec3.ZERO);
                    currentState = State.HOOKED_IN_ENTITY;
                    return;
                }

                if (flag) {
                    setDeltaMovement(getDeltaMovement().multiply(0.3D, 0.2D, 0.3D));
                    currentState = State.BOBBING;
                    return;
                }
                checkCollision();
            }
            else {
                if (currentState == State.HOOKED_IN_ENTITY) {
                    if (hookedIn != null) {
                        if (!hookedIn.isAlive()) {
                            hookedIn = null;
                            currentState = State.FLYING;
                        }
                        else {
                            setPos(hookedIn.getX(), hookedIn.getY(0.8D), hookedIn.getZ());
                        }
                    }
                    return;
                }

                if (currentState == State.BOBBING) {
                    Vec3 vec3 = getDeltaMovement();
                    double d0 = getY() + vec3.y - (double)blockpos.getY() - (double)f;

                    if (Math.abs(d0) < 0.01D) {
                        d0 += Math.signum(d0) * 0.1D;
                    }
                    setDeltaMovement(vec3.x * 0.9D, vec3.y - d0 * (double)random.nextFloat() * 0.2D, vec3.z * 0.9D);
                }
            }

            if (!fluidstate.is(FluidTags.WATER)) {
                setDeltaMovement(getDeltaMovement().add(0.0D, -0.03D, 0.0D));
            }
            move(MoverType.SELF, getDeltaMovement());
            updateRotation();

            if (currentState == State.FLYING && (onGround() || horizontalCollision)) {
                setDeltaMovement(Vec3.ZERO);
            }
            setDeltaMovement(getDeltaMovement().scale(0.92D));
            reapplyPosition();
        }
    }

    private boolean shouldStopFishing(LivingEntity owner) {
        if (!owner.isAlive() || !(distanceToSqr(owner) > 1024.0D)) {
            return false;
        }
        else {
            discard();
            return true;
        }
    }

    private void checkCollision() {
        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        this.onHit(hitResult);
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) || entity.isAlive() && entity instanceof ItemEntity;
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);

        if (!level().isClientSide) {
            Entity entity = hitResult.getEntity();

            if (entity instanceof Player player) {
                if (isBlocked(player)) {
                    if (getLivingOwner() instanceof Grump grump) {
                        grump.hookBlocked();
                        level().broadcastEntityEvent(player, (byte) 29);
                    }
                    discard();
                    return;
                }
            }
            hookedIn = hitResult.getEntity();
            setHookedEntity();
        }
    }

    private boolean isBlocked(Player player) {
        if (player.isBlocking()) {
            Vec3 hookPos = position();

            Vec3 playerView = player.getViewVector(1.0F);
            Vec3 vec3 = hookPos.vectorTo(player.position()).normalize();
            vec3 = new Vec3(vec3.x, 0.0D, vec3.z);
            return vec3.dot(playerView) < 0.0D;
        }
        return false;
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        super.onHitBlock(hitResult);
        setDeltaMovement(getDeltaMovement().normalize().scale(hitResult.distanceTo(this)));
    }

    private void setHookedEntity() {
        getEntityData().set(DATA_HOOKED_ENTITY, hookedIn.getId() + 1);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        // Nothing to write.
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        // Nothing to read.
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleEntityEvent(byte event) {
        if (event == 31 && level().isClientSide && hookedIn instanceof Player player && player.isLocalPlayer()) {
            bringInHookedEntity();
        }
        super.handleEntityEvent(event);
    }

    public void bringInHookedEntity() {
        LivingEntity livingEntity = getLivingOwner();

        if (livingEntity != null) {
            level().playSound(null, livingEntity.blockPosition(), SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.HOSTILE, 0.6F, 0.4F / (level().random.nextFloat() * 0.4F + 0.8F));
            Entity entity = hookedIn;

            double xMotion = livingEntity.getX() - getX();
            double yMotion = livingEntity.getY() - getY();
            double zMotion = livingEntity.getZ() - getZ();

            double v = Math.sqrt(xMotion * xMotion + yMotion * yMotion + zMotion * zMotion);
            double multiplier = 0.3;

            Vec3 velocity = new Vec3(xMotion * multiplier, yMotion * multiplier + Math.sqrt(v) * 0.1, zMotion * multiplier);

            if (entity instanceof ServerPlayer serverPlayer) {
                NetworkHelper.sendEntityVelocityUpdate(serverPlayer, serverPlayer, velocity);
            }
            entity.setDeltaMovement(velocity);
        }
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Nullable
    public LivingEntity getLivingOwner() {
        Entity entity = this.getOwner();
        return entity instanceof LivingEntity ? (LivingEntity) entity : null;
    }

    @Nullable
    public Entity getHookedIn() {
        return this.hookedIn;
    }

    @Override
    public boolean canChangeDimensions() {
        return false;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeInt(getOwner() == null ? getId() : getOwner().getId());
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        this.setOwner(level().getEntity(additionalData.readInt()));
    }

    enum State {
        FLYING,
        HOOKED_IN_ENTITY,
        BOBBING
    }
}
