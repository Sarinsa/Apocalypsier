package com.toast.apocalypse.common.entity.living.ai;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

/** A copy of HurtByTargetGoal except it is not restricted to pathfinder mobs entities */
public class MobHurtByTargetGoal extends TargetGoal {
    private static final TargetingConditions HURT_BY_TARGETING = TargetingConditions.forCombat().ignoreLineOfSight().ignoreInvisibilityTesting();
    private boolean alertSameType;
    private int timestamp;
    private final Class<?>[] toIgnoreDamage;
    @Nullable
    private Class<?>[] toIgnoreAlert;

    public MobHurtByTargetGoal(Mob mob, Class<?>... clazz) {
        super(mob, true);
        toIgnoreDamage = clazz;
        setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    public boolean canUse() {
        int timeLastHurt = mob.getLastHurtByMobTimestamp();
        LivingEntity lastHurtByMob = mob.getLastHurtByMob();

        if (timeLastHurt != timestamp && lastHurtByMob != null) {
            if (lastHurtByMob.getType() == EntityType.PLAYER && mob.level().getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
                return false;
            }
            else {
                for(Class<?> clazz : toIgnoreDamage) {
                    if (clazz.isAssignableFrom(lastHurtByMob.getClass())) {
                        return false;
                    }
                }
                return canAttack(lastHurtByMob, HURT_BY_TARGETING);
            }
        }
        else {
            return false;
        }
    }

    public MobHurtByTargetGoal setAlertOthers(Class<?>... entityClasses) {
        alertSameType = true;
        toIgnoreAlert = entityClasses;
        return this;
    }

    public void start() {
        mob.setTarget(mob.getLastHurtByMob());
        targetMob = mob.getTarget();
        timestamp = mob.getLastHurtByMobTimestamp();
        unseenMemoryTicks = 300;

        if (alertSameType) {
            alertOthers();
        }
        super.start();
    }

    protected void alertOthers() {
        double followDistance = getFollowDistance();
        AABB aabb = AABB.unitCubeFromLowerCorner(mob.position()).inflate(followDistance, 10.0D, followDistance);
        List<? extends Mob> list = mob.level().getEntitiesOfClass(mob.getClass(), aabb, EntitySelector.NO_SPECTATORS);
        Iterator<? extends Mob> iterator = list.iterator();

        while (true) {
            Mob mob;
            while (true) {
                if (!iterator.hasNext()) {
                    return;
                }

                mob = iterator.next();
                if (this.mob != mob && mob.getTarget() == null && (!(this.mob instanceof TamableAnimal) || ((TamableAnimal)this.mob).getOwner() == ((TamableAnimal)mob).getOwner()) && !mob.isAlliedTo(this.mob.getLastHurtByMob())) {
                    if (toIgnoreAlert == null) {
                        break;
                    }

                    boolean flag = false;

                    for(Class<?> clazz : toIgnoreAlert) {
                        if (mob.getClass() == clazz) {
                            flag = true;
                            break;
                        }
                    }

                    if (!flag) {
                        break;
                    }
                }
            }

            this.alertOther(mob, mob.getLastHurtByMob());
        }
    }

    protected void alertOther(Mob mob, LivingEntity livingEntity) {
        mob.setTarget(livingEntity);
    }
}
