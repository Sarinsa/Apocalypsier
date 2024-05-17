package com.toast.apocalypse.common.entity.projectile;

import com.toast.apocalypse.common.core.config.ApocalypseCommonConfig;
import com.toast.apocalypse.common.core.register.ApocalypseEntities;
import com.toast.apocalypse.common.entity.living.Destroyer;
import com.toast.apocalypse.common.misc.DestroyerExplosionCalculator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.network.NetworkHooks;

/**
 * This is the type of fireball shot by destroyers. It is capable of destroying any block (except bedrock).
 */
public class DestroyerFireballEntity extends Fireball {

    /** The explosion power for this fireball */
    private int explosionPower = 1;
    /** The time until this fireball explodes. Set when reflected.
     *  This makes it harder to damage the destroyer with it's own fireball. */
    private int fuseTime = -1;

    public DestroyerFireballEntity(EntityType<? extends Fireball> entityType, Level level) {
        super(entityType, level);
    }

    public DestroyerFireballEntity(Level level, Destroyer destroyer, double x, double y, double z) {
        super(ApocalypseEntities.DESTROYER_FIREBALL.get(), destroyer, x, y, z, level);
        this.explosionPower = destroyer.getExplosionPower();
    }

    /**
     * Helper method for creating the destroyer
     * explosion that can destroy any type of block.
     */
    public static void destroyerExplosion(Level level, Entity entity, DamageSource damageSource, double x, double y, double z, float explosionPower) {
        boolean canDestroy = ForgeEventFactory.getMobGriefingEvent(level, entity);
        level.explode(entity, damageSource, new DestroyerExplosionCalculator(), x, y, z, explosionPower, canDestroy, Level.ExplosionInteraction.MOB);
    }

    @Override
    protected void onHit(HitResult result) {
        if (result.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityResult = (EntityHitResult) result;
            Entity entity = entityResult.getEntity();
            DamageSource directImpact = level().damageSources().fireball(this, getOwner());
            entity.hurt(directImpact, 4.0F);

            if (entity instanceof LivingEntity livingEntity) {
                boolean damageBlocked = livingEntity.isDamageSourceBlocked(directImpact);
                final int armorDamage = ApocalypseCommonConfig.COMMON.getDestroyerEquipmentDamage();

                if (armorDamage > 0) {
                    // Deal heavy damage to shield, if blocking
                    if (damageBlocked) {
                        livingEntity.hurtCurrentlyUsedShield(armorDamage);
                    }
                    // Deal heavy damage to armor, if not blocking
                    else {
                        if (livingEntity instanceof ServerPlayer player) {
                            for (ItemStack armorStack : livingEntity.getArmorSlots()) {
                                armorStack.hurt(armorDamage, player.getRandom(), player);
                            }
                        } else {
                            for (ItemStack armorStack : livingEntity.getArmorSlots()) {
                                armorStack.hurtAndBreak(armorDamage, livingEntity, (e) -> {
                                    if (armorStack.getEquipmentSlot() != null)
                                        e.broadcastBreakEvent(armorStack.getEquipmentSlot());
                                });
                            }
                        }
                    }
                }
            }
        }
        if (!level().isClientSide) {
            destroyerExplosion(getCommandSenderWorld(), this, level().damageSources().fireball(this, getOwner()), getX(), getY(), getZ(), explosionPower);
            discard();
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (fuseTime >= 0 && --fuseTime < 0) {
            // Could have called onHit() here with a miss
            // BlockRayTraceResult, but just in case some other mod
            // wants to use the dummy info that would be parsed, lets not.
            // Did that explanation make sense? Probably not.
            if (!level().isClientSide) {
                destroyerExplosion(getCommandSenderWorld(), this, level().damageSources().fireball(this, getOwner()), getX(), getY(), getZ(), explosionPower);
                discard();
            }
        }
    }

    @Override
    public boolean hurt(DamageSource damageSource, float damage) {
        if (isInvulnerableTo(damageSource))
            return false;

        markHurt();

        if (fuseTime < 0 && damageSource.getEntity() != null) {
            // Reflect fireball and set fuse time
            Entity entity = damageSource.getEntity();
            Vec3 vec = entity.getLookAngle();
            entity.getCommandSenderWorld().playSound(null, blockPosition(), SoundEvents.TNT_PRIMED, SoundSource.NEUTRAL, 0.8F, 1.0F);
            fuseTime = 10;
            setDeltaMovement(vec);
            xPower = vec.x * 0.1D;
            yPower = vec.y * 0.1D;
            zPower = vec.z * 0.1D;
            setOwner(entity);
            return true;
        }
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("ExplosionPower", explosionPower);
    }

    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);

        if (compoundTag.contains("ExplosionPower", Tag.TAG_ANY_NUMERIC))
            explosionPower = compoundTag.getInt("ExplosionPower");
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
