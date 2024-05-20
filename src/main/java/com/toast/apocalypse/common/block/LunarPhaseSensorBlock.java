package com.toast.apocalypse.common.block;

import com.toast.apocalypse.common.blockentity.LunarPhaseSensorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class LunarPhaseSensorBlock extends Block implements EntityBlock {

    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    public static final BooleanProperty INVERTED = BlockStateProperties.INVERTED;

    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D);

    public LunarPhaseSensorBlock() {
        super(BlockBehaviour.Properties.of().strength(1.0F).requiresCorrectToolForDrops().sound(SoundType.METAL));
        registerDefaultState(stateDefinition.any().setValue(POWER, 0).setValue(INVERTED, false));
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWER);
    }

    public static void updateSignalStrength(BlockState state, Level level, BlockPos pos) {
        if (level.dimension() == Level.OVERWORLD) {
            int power = Mth.floor(15 * level.getMoonBrightness());
            boolean inverted = state.getValue(INVERTED);

            if (inverted) {
                power = 15 - power;
            }
            power = Mth.clamp(power, 0, 15);

            if (level.getBrightness(LightLayer.SKY, pos) < 4) {
                power = 0;
            }
            if (state.getValue(POWER) != power) {
                level.setBlock(pos, state.setValue(POWER, power), 3);
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if (player.mayBuild()) {

            if (level.isClientSide) {
                return InteractionResult.SUCCESS;
            }
            else {
                BlockState blockstate = state.cycle(INVERTED);
                level.setBlock(pos, blockstate, 4);
                updateSignalStrength(blockstate, level, pos);
                return InteractionResult.CONSUME;
            }
        }
        else {
            return super.use(state, level, pos, player, hand, result);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LunarPhaseSensorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return (lvl, pos, state, blockEntity) -> LunarPhaseSensorBlockEntity.tick(lvl, pos, state, (LunarPhaseSensorBlockEntity) blockEntity);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateBuilder) {
        stateBuilder.add(POWER, INVERTED);
    }
}
