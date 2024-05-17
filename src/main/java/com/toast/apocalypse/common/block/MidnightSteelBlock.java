package com.toast.apocalypse.common.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class MidnightSteelBlock extends Block {

    public MidnightSteelBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_LIGHT_GRAY)
                .strength(2.0F)
                .sound(SoundType.METAL)
                .lightLevel((state) -> 2)
                .requiresCorrectToolForDrops());
    }
}
