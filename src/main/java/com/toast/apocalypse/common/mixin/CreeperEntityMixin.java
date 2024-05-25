package com.toast.apocalypse.common.mixin;

import com.toast.apocalypse.common.misc.mixin_work.CommonMixinHooks;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PowerableMob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Creeper.class)
public abstract class CreeperEntityMixin extends Monster implements PowerableMob {

    protected CreeperEntityMixin(EntityType<? extends Creeper> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * A somewhat wonky solution to preventing
     * Creepers from spawning lingering effect
     * clouds that can grant "infinite" buffs.
     */
    @Redirect(
            method = "spawnLingeringCloud",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    public boolean redirectSpawnLingeringCloud(Level level, Entity entity) {
        CommonMixinHooks.capAreaEffectCloudDurations((AreaEffectCloud) entity);
        return level.addFreshEntity(entity);
    }
}
