package com.toast.apocalypse.common.mixin;

import com.toast.apocalypse.common.misc.mixin_work.CommonMixinHooks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    //@Unique
    private long timeAirborne = 0;

    //@ModifyVariable(method = "travel", at = @At("STORE"), index = 4)
    public AttributeInstance getGravityAttribute(AttributeInstance attributeInstance) {
        return CommonMixinHooks.livingEntityOnTravelModifyVariable(attributeInstance, (LivingEntity) (Object) this, this.timeAirborne);
    }

    //@Inject(method = "aiStep", at = @At("HEAD"))
    public void onAiStep(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (!entity.onGround())
            ++this.timeAirborne;
        else {
            this.timeAirborne = 0;
        }
    }
}
