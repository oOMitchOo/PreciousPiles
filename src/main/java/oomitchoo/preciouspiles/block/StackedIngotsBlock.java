package oomitchoo.preciouspiles.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class StackedIngotsBlock extends Block {
    public static final BooleanProperty DOWN_FILLED = BooleanProperty.create("down_filled");
    public static final BooleanProperty INGOTS_AT_NE = BooleanProperty.create("ingots_at_ne");
    public static final BooleanProperty INGOTS_AT_SE = BooleanProperty.create("ingots_at_se");
    public static final BooleanProperty INGOTS_AT_SW = BooleanProperty.create("ingots_at_sw");
    public static final BooleanProperty INGOTS_AT_NW = BooleanProperty.create("ingots_at_nw");

    protected static final VoxelShape DOWN_FILLED_SHAPE = Shapes.box(0,0,0,1,0.5,1);
    protected static final VoxelShape INGOTS_AT_NE_SHAPE = Shapes.box(0.5,0,0,1,0.5,0.5);
    protected static final VoxelShape INGOTS_AT_SE_SHAPE = Shapes.box(0.5,0,0.5,1,0.5,1);
    protected static final VoxelShape INGOTS_AT_SW_SHAPE = Shapes.box(0,0,0.5,0.5,0.5,1);
    protected static final VoxelShape INGOTS_AT_NW_SHAPE = Shapes.box(0,0,0,0.5,0.5,0.5);

    protected static final float float1 = (float) 3 /32;
    protected static final float float2 = (float) 13 /32;
    protected static final float float3 = (float) 19 /32;
    protected static final float float4 = (float) 29 /32;
    protected static final VoxelShape INGOTS_AT_NE_OCL_SHAPE = Shapes.box(float3,0,float1, float4, 0.5, float2);
    protected static final VoxelShape INGOTS_AT_SE_OCL_SHAPE = Shapes.box(float3,0,float3, float4, 0.5, float4);
    protected static final VoxelShape INGOTS_AT_SW_OCL_SHAPE = Shapes.box(float1,0,float3, float2, 0.5, float4);
    protected static final VoxelShape INGOTS_AT_NW_OCL_SHAPE = Shapes.box(float1,0,float1, float2, 0.5, float2);

    public StackedIngotsBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(DOWN_FILLED, false).setValue(INGOTS_AT_NE, false).setValue(INGOTS_AT_SE, false).setValue(INGOTS_AT_SW, false).setValue(INGOTS_AT_NW, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DOWN_FILLED, INGOTS_AT_NE, INGOTS_AT_SE, INGOTS_AT_SW, INGOTS_AT_NW);
    }

    public static boolean tryAddingEightIngots(BlockState oldState, LevelAccessor level, BlockPos pos) {
        BlockState newState = oldState;
        if(oldState.getValue(DOWN_FILLED)) {
            if (!oldState.getValue(INGOTS_AT_NW)) {
                updateOrDestroy(oldState, oldState.setValue(INGOTS_AT_NW, true), level, pos, 3);
                return true;
            } else if (!oldState.getValue(INGOTS_AT_NE)) {
                updateOrDestroy(oldState, oldState.setValue(INGOTS_AT_NE, true), level, pos, 3);
                return true;
            } else if (!oldState.getValue(INGOTS_AT_SE)) {
                updateOrDestroy(oldState, oldState.setValue(INGOTS_AT_SE, true), level, pos, 3);
                return true;
            } else if (!oldState.getValue(INGOTS_AT_SW)) {
                updateOrDestroy(oldState, oldState.setValue(INGOTS_AT_SW, true), level, pos, 3);
                return true;
            }
            return false;
        } else {
            if (!oldState.getValue(INGOTS_AT_NW)) {
                newState = oldState.setValue(INGOTS_AT_NW, true);
            } else if (!oldState.getValue(INGOTS_AT_NE)) {
                newState = oldState.setValue(INGOTS_AT_NE, true);
            } else if (!oldState.getValue(INGOTS_AT_SE)) {
                newState = oldState.setValue(INGOTS_AT_SE, true);
            } else if (!oldState.getValue(INGOTS_AT_SW)) {
                newState = oldState.setValue(INGOTS_AT_SW, true);
            }

            if (newState.getValue(INGOTS_AT_NW) && newState.getValue(INGOTS_AT_NE) && newState.getValue(INGOTS_AT_SE) && newState.getValue(INGOTS_AT_SW)) {
                updateOrDestroy(oldState, newState.setValue(DOWN_FILLED, true).setValue(INGOTS_AT_NW, false).setValue(INGOTS_AT_NE, false).setValue(INGOTS_AT_SE, false).setValue(INGOTS_AT_SW, false), level, pos, 3);
            } else {
                updateOrDestroy(oldState, newState, level, pos, 3);
            }
            return true;
        }
    }

    public static boolean tryAddingEightPrecise (boolean placingLow, boolean clickedWest, boolean clickedNorth, BlockState oldState, LevelAccessor level, BlockPos pos) {
        if (placingLow && !oldState.getValue(DOWN_FILLED)) { // can only place low if lower half isn't filled yet
            if (clickedWest) { // trying to place WEST
                if (clickedNorth && !oldState.getValue(INGOTS_AT_NW)) { // trying to place WEST-NORTH
                    if (oldState.getValue(INGOTS_AT_NE) && oldState.getValue(INGOTS_AT_SE) && oldState.getValue(INGOTS_AT_SW)) { // if all other corners are filled already it becomes a half-slab
                        updateOrDestroy(oldState, oldState.setValue(DOWN_FILLED, true).setValue(INGOTS_AT_NE, false).setValue(INGOTS_AT_SE, false).setValue(INGOTS_AT_SW, false), level, pos, 3);
                    } else {
                        updateOrDestroy(oldState, oldState.setValue(INGOTS_AT_NW, true), level, pos, 3); // else only this one value gets updated.
                    }
                    return true;
                } else if (!clickedNorth && !oldState.getValue(INGOTS_AT_SW)) { // trying to place WEST-SOUTH
                    if (oldState.getValue(INGOTS_AT_NW) && oldState.getValue(INGOTS_AT_NE) && oldState.getValue(INGOTS_AT_SE)) { // if all other corners are filled already it becomes a half-slab
                        updateOrDestroy(oldState, oldState.setValue(DOWN_FILLED, true).setValue(INGOTS_AT_NW, false).setValue(INGOTS_AT_NE, false).setValue(INGOTS_AT_SE, false), level, pos, 3);
                    } else {
                        updateOrDestroy(oldState, oldState.setValue(INGOTS_AT_SW, true), level, pos, 3); // else only this one value gets updated.
                    }
                    return true;
                }
            } else { // trying to place EAST
                if (clickedNorth && !oldState.getValue(INGOTS_AT_NE)) { // trying to place EAST-NORTH
                    if (oldState.getValue(INGOTS_AT_NW) && oldState.getValue(INGOTS_AT_SE) && oldState.getValue(INGOTS_AT_SW)) { // if all other corners are filled already it becomes a half-slab
                        updateOrDestroy(oldState, oldState.setValue(DOWN_FILLED, true).setValue(INGOTS_AT_NW, false).setValue(INGOTS_AT_SE, false).setValue(INGOTS_AT_SW, false), level, pos, 3);
                    } else {
                        updateOrDestroy(oldState, oldState.setValue(INGOTS_AT_NE, true), level, pos, 3); // else only this one value gets updated.
                    }
                    return true;
                } else if (!clickedNorth && !oldState.getValue(INGOTS_AT_SE)) { // trying to place EAST-SOUTH
                    if (oldState.getValue(INGOTS_AT_NW) && oldState.getValue(INGOTS_AT_NE) && oldState.getValue(INGOTS_AT_SW)) { // if all other corners are filled already it becomes a half-slab
                        updateOrDestroy(oldState, oldState.setValue(DOWN_FILLED, true).setValue(INGOTS_AT_NW, false).setValue(INGOTS_AT_NE, false).setValue(INGOTS_AT_SW, false), level, pos, 3);
                    } else {
                        updateOrDestroy(oldState, oldState.setValue(INGOTS_AT_SE, true), level, pos, 3); // else only this one value gets updated.
                    }
                    return true;
                }
            }
        } else if (!placingLow && oldState.getValue(DOWN_FILLED)){ // can only place high if lower half is already filled.
            if (clickedWest) { // trying to place WEST
                if (clickedNorth && !oldState.getValue(INGOTS_AT_NW)) { // trying to place WEST-NORTH
                    updateOrDestroy(oldState, oldState.setValue(INGOTS_AT_NW, true), level, pos, 3); // else only this one value gets updated.
                    return true;
                } else if (!clickedNorth && !oldState.getValue(INGOTS_AT_SW)) { // trying to place WEST-SOUTH
                    updateOrDestroy(oldState, oldState.setValue(INGOTS_AT_SW, true), level, pos, 3); // else only this one value gets updated.
                    return true;
                }
            } else { // trying to place EAST
                if (clickedNorth && !oldState.getValue(INGOTS_AT_NE)) { // trying to place EAST-NORTH
                    updateOrDestroy(oldState, oldState.setValue(INGOTS_AT_NE, true), level, pos, 3); // else only this one value gets updated.
                    return true;
                } else if (!clickedNorth && !oldState.getValue(INGOTS_AT_SE)) { // trying to place EAST-SOUTH
                    updateOrDestroy(oldState, oldState.setValue(INGOTS_AT_SE, true), level, pos, 3); // else only this one value gets updated.
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape tempShape = Shapes.empty();
        if(state.getValue(DOWN_FILLED)){
            tempShape = DOWN_FILLED_SHAPE;
            if(state.getValue(INGOTS_AT_NW)) tempShape = Shapes.or(tempShape, INGOTS_AT_NW_SHAPE.move(0,0.5,0));
            if(state.getValue(INGOTS_AT_NE)) tempShape = Shapes.or(tempShape, INGOTS_AT_NE_SHAPE.move(0,0.5,0));
            if(state.getValue(INGOTS_AT_SE)) tempShape = Shapes.or(tempShape, INGOTS_AT_SE_SHAPE.move(0,0.5,0));
            if(state.getValue(INGOTS_AT_SW)) tempShape = Shapes.or(tempShape, INGOTS_AT_SW_SHAPE.move(0,0.5,0));
        } else {
            if(state.getValue(INGOTS_AT_NW)) tempShape = Shapes.or(tempShape, INGOTS_AT_NW_SHAPE);
            if(state.getValue(INGOTS_AT_NE)) tempShape = Shapes.or(tempShape, INGOTS_AT_NE_SHAPE);
            if(state.getValue(INGOTS_AT_SE)) tempShape = Shapes.or(tempShape, INGOTS_AT_SE_SHAPE);
            if(state.getValue(INGOTS_AT_SW)) tempShape = Shapes.or(tempShape, INGOTS_AT_SW_SHAPE);
        }
        return tempShape;
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pos1, Direction direction, BlockPos pos2, BlockState blockState2, RandomSource randomSource) {
        return direction == Direction.DOWN && !this.canSurvive(blockState, level, pos1) ? Blocks.AIR.defaultBlockState() : super.updateShape(blockState, level, scheduledTickAccess, pos1, direction, pos2, blockState2, randomSource);
    }

    @Override // The StackedIngotsBlock can only survive on a sturdyFace or on a full StackedIngotsBlock
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState blockBelow = level.getBlockState(pos.below());
        if (blockBelow.isFaceSturdy(level, pos.below(), Direction.UP)) {
            return true;
        } else if (blockBelow.getBlock() instanceof StackedIngotsBlock) {
            return blockBelow.getValue(DOWN_FILLED) && blockBelow.getValue(INGOTS_AT_NW) && blockBelow.getValue(INGOTS_AT_NE) && blockBelow.getValue(INGOTS_AT_SE) && blockBelow.getValue(INGOTS_AT_SW);
        } else {
            return false;
        }
    }

    @Override // empty means no torches/buttons and so on can be placed on the block faces.
    protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state) {
        VoxelShape tempShape = Shapes.empty();
        if(state.getValue(DOWN_FILLED)){
            tempShape = Shapes.or(INGOTS_AT_NE_OCL_SHAPE, INGOTS_AT_SE_OCL_SHAPE, INGOTS_AT_SW_OCL_SHAPE, INGOTS_AT_NW_OCL_SHAPE);
            if(state.getValue(INGOTS_AT_NW)) tempShape = Shapes.or(tempShape, INGOTS_AT_NW_OCL_SHAPE.move(0,0.5,0));
            if(state.getValue(INGOTS_AT_NE)) tempShape = Shapes.or(tempShape, INGOTS_AT_NE_OCL_SHAPE.move(0,0.5,0));
            if(state.getValue(INGOTS_AT_SE)) tempShape = Shapes.or(tempShape, INGOTS_AT_SE_OCL_SHAPE.move(0,0.5,0));
            if(state.getValue(INGOTS_AT_SW)) tempShape = Shapes.or(tempShape, INGOTS_AT_SW_OCL_SHAPE.move(0,0.5,0));
        } else {
            if(state.getValue(INGOTS_AT_NW)) tempShape = Shapes.or(tempShape, INGOTS_AT_NW_OCL_SHAPE);
            if(state.getValue(INGOTS_AT_NE)) tempShape = Shapes.or(tempShape, INGOTS_AT_NE_OCL_SHAPE);
            if(state.getValue(INGOTS_AT_SE)) tempShape = Shapes.or(tempShape, INGOTS_AT_SE_OCL_SHAPE);
            if(state.getValue(INGOTS_AT_SW)) tempShape = Shapes.or(tempShape, INGOTS_AT_SW_OCL_SHAPE);
        }
        return tempShape;
    }
}
