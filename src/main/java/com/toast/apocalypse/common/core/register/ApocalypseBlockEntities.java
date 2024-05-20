package com.toast.apocalypse.common.core.register;

import com.toast.apocalypse.common.blockentity.DynamicTrapBlockEntity;
import com.toast.apocalypse.common.core.Apocalypse;
import com.toast.apocalypse.common.blockentity.LunarPhaseSensorBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ApocalypseBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Apocalypse.MODID);


    public static final RegistryObject<BlockEntityType<LunarPhaseSensorBlockEntity>> LUNAR_PHASE_SENSOR = register("lunar_phase_sensor", () -> BlockEntityType.Builder.of(LunarPhaseSensorBlockEntity::new, ApocalypseBlocks.LUNAR_PHASE_SENSOR.get()).build(null));
    public static final RegistryObject<BlockEntityType<DynamicTrapBlockEntity>> DYNAMIC_TRAP = register("dynamic_trap", () -> BlockEntityType.Builder.of(DynamicTrapBlockEntity::new, ApocalypseBlocks.DYNAMIC_TRAP.get()).build(null));


    private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> register(String name, Supplier<BlockEntityType<T>> tileEntityTypeSupplier) {
        return BLOCK_ENTITIES.register(name, tileEntityTypeSupplier);
    }
}
