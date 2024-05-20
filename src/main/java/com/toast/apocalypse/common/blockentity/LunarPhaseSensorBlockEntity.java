package com.toast.apocalypse.common.blockentity;

import com.toast.apocalypse.common.block.LunarPhaseSensorBlock;
import com.toast.apocalypse.common.core.register.ApocalypseBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class LunarPhaseSensorBlockEntity extends BlockEntity {

    public LunarPhaseSensorBlockEntity(BlockPos pos, BlockState state) {
        super(ApocalypseBlockEntities.LUNAR_PHASE_SENSOR.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LunarPhaseSensorBlockEntity blockEntity) {
        if (level != null && !level.isClientSide && level.getGameTime() % 20L == 0L) {
            Block block = state.getBlock();

            if (block instanceof LunarPhaseSensorBlock) {
                LunarPhaseSensorBlock.updateSignalStrength(state, level, pos);
            }
        }
    }
}
