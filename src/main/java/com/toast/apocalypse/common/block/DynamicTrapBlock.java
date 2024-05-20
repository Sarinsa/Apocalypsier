package com.toast.apocalypse.common.block;

import com.toast.apocalypse.common.blockentity.DynamicTrapBlockEntity;
import com.toast.apocalypse.common.blockentity.LunarPhaseSensorBlockEntity;
import com.toast.apocalypse.common.menus.DynamicTrapMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class DynamicTrapBlock extends Block implements EntityBlock {

    public static final EnumProperty<TrapState> TRAP_STATE = EnumProperty.create("trap_state", TrapState.class);
    public static final DirectionProperty VERTICAL_DIRECTION = BlockStateProperties.VERTICAL_DIRECTION;


    public DynamicTrapBlock() {
        super(BlockBehaviour.Properties.of()
                .requiresCorrectToolForDrops()
                .strength(1.0F, 2.0F)
                .sound(SoundType.STONE)
                .mapColor(MapColor.WOOD)
        );
        registerDefaultState(stateDefinition.any().setValue(TRAP_STATE, TrapState.IDLE).setValue(VERTICAL_DIRECTION, Direction.UP));
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        else {
            BlockEntity blockEntity = level.getBlockEntity(pos);

            if (blockEntity instanceof DynamicTrapBlockEntity dynamicTrap) {
                player.openMenu(dynamicTrap);
            }
            return InteractionResult.CONSUME;
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean b) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);

            if (blockEntity instanceof DynamicTrapBlockEntity dynamicTrap) {
                if (!level.isClientSide) {
                    Containers.dropContents(level, pos, dynamicTrap);
                }
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, newState, b);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block newBlock, BlockPos neighborPos, boolean p_52705_) {
        boolean hasSignal = level.hasNeighborSignal(pos);

        if (hasSignal) {
            if (level.getBlockEntity(pos) instanceof DynamicTrapBlockEntity dynamicTrap) {
                dynamicTrap.activateTrap();
            }
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext placeContext) {
        Direction direction = placeContext.getNearestLookingVerticalDirection().getOpposite();
        return defaultBlockState().setValue(VERTICAL_DIRECTION, direction);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DynamicTrapBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : (lvl, pos, state, blockEntity) -> DynamicTrapBlockEntity.serverTick(lvl, pos, state, (DynamicTrapBlockEntity) blockEntity);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateBuilder) {
        stateBuilder.add(TRAP_STATE, VERTICAL_DIRECTION);
    }


    public enum TrapState implements StringRepresentable {
        IDLE("idle"),
        PROCESSING("processing"),
        READY("ready"),
        NOT_OPERATIONAL("not_operational");

        TrapState(String name) {
            this.name = name;
        }
        final String name;

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
