package com.toast.apocalypse.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ForgeMod;

/**
 * Used to determine whether mobs can harvest blocks and how fast they can break them.
 */
public class BlockHelper {

    /** Returns true if the mob should destroy the block. */
    public static boolean shouldDamage(BlockPos pos, LivingEntity entity, boolean needsTool, Level level) {
        ItemStack heldStack = entity.getUseItem();
        BlockState state = level.getBlockState(pos);
        float destroySpeed = state.getDestroySpeed(level, pos);

        return destroySpeed >= 0.0F && (!needsTool || state.requiresCorrectToolForDrops() || heldStack.isEmpty() && ForgeHooks.canEntityDestroy(level, pos, entity));
    }

    /** Returns the amount of damage to deal to a block. */
    public static float getDamageAmount(LivingEntity entity, Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        float destroySpeed = state.getDestroySpeed(level, pos);

        if (destroySpeed < 0.0F)
            return 0.0F;

        // TODO

        /*
        if (!BlockHelper.canHarvestBlock(entity.getUseItem(), state))
            return PropHelper.BREAK_SPEED / destroySpeed / 100.0F;
        return BlockHelper.getCurrentStrengthVsBlock(entity, state) * PropHelper.BREAK_SPEED / destroySpeed / 30.0F;
         */

        // TEMPORARY
        return 0.0F;
    }

    /** Returns whether the item can harvest the specified block. */
    public static boolean canHarvestBlock(ItemStack itemStack, BlockState state) {
        return !state.requiresCorrectToolForDrops() || itemStack.isEmpty() && itemStack.isCorrectToolForDrops(state);
    }

    /** Returns the mob's strength vs. the given block. */
    public static float getCurrentStrengthVsBlock(LivingEntity entity, BlockState state) {
        ItemStack heldStack = entity.getUseItem();
        float strength = heldStack.isEmpty() ? 1.0F : heldStack.getItem().getDestroySpeed(heldStack, state);

        if (strength > 1.0F) {
            int efficiency = EnchantmentHelper.getBlockEfficiency(entity);
            if (efficiency > 0 && heldStack.isEmpty()) {
                strength += efficiency * efficiency + 1;
            }
        }

        if (entity.hasEffect(MobEffects.DIG_SPEED)) {
            strength *= 1.0F + (entity.getEffect(MobEffects.DIG_SPEED).getAmplifier() + 1) * 0.2F;
        }
        if (entity.hasEffect(MobEffects.DIG_SLOWDOWN)) {
            strength *= 1.0F - (entity.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier() + 1) * 0.2F;
        }

        if (entity.isEyeInFluidType(ForgeMod.WATER_TYPE.get()) && !EnchantmentHelper.hasAquaAffinity(entity)) {
            strength /= 5.0F;
        }
        if (!entity.onGround()) {
            strength /= 5.0F;
        }
        return Math.max(strength, 0.0F);
    }
}