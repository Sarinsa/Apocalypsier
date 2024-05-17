package com.toast.apocalypse.common.entity.living.ai;

import com.toast.apocalypse.common.entity.living.AbstractFullMoonGhast;
import com.toast.apocalypse.common.entity.living.Grump;
import com.toast.apocalypse.common.entity.living.IFullMoonMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class MoonMobPlayerTargetGoal<T extends Mob & IFullMoonMob> extends TargetGoal {

    private final T moonMob;

    public MoonMobPlayerTargetGoal(T mobEntity, boolean mustSee) {
        super(mobEntity, mustSee);
        moonMob = mobEntity;
    }

    @Override
    public boolean canUse() {
        UUID playerTargetUUID = moonMob.getPlayerTargetUUID();

        if (playerTargetUUID == null)
            return false;

        Player player = moonMob.level().getPlayerByUUID(playerTargetUUID);

        if (player == null)
            return false;

        if (moonMob instanceof Grump grump) {
            if (grump.hasOwner())
                return false;
        }

        if (mustSee) {
            if (mob instanceof AbstractFullMoonGhast) {
                if (!((AbstractFullMoonGhast) moonMob).canSeeDirectly(player))
                    return false;
            }
            else if (!mob.getSensing().hasLineOfSight(player))
                return false;
        }
        return player.isAlive() && !player.isCreative() && !player.isSpectator();
    }

    @SuppressWarnings("ConstantConditions")
    public void start() {
        LivingEntity target = moonMob.getTarget();
        Player playerTarget = moonMob.level().getPlayerByUUID(moonMob.getPlayerTargetUUID());

        moonMob.setTarget(playerTarget != null ? playerTarget : target);
        super.start();
    }
}
