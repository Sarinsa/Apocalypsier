package com.toast.apocalypse.common.mixin;

import com.toast.apocalypse.common.misc.mixin_work.CommonMixinHooks;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Creds to gudenau (a cool guy in the Fabric Discord server) for helping with this.
@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    //@Unique
    private DamageSource apocalypseDamageSource;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "hurt", at = @At("HEAD"))
    private void onHurt(DamageSource damageSource, float damage, CallbackInfoReturnable<Boolean> cir){
        apocalypseDamageSource = damageSource;
    }

    @ModifyVariable(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/common/ForgeHooks;onPlayerAttack(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;F)Z"), ordinal = 0, argsOnly = true)
    public float modifyDamage(float damage){
        damage = CommonMixinHooks.livingEntityHurtModifyArg(apocalypseDamageSource, (Player) (Object) this, damage);
        apocalypseDamageSource = null;
        return damage;
    }
}