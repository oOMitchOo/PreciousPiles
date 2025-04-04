package oomitchoo.preciouspiles.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        this.registerDefaultState(this.defaultBlockState().setValue(DOWN_FILLED, false).setValue(INGOTS_AT_NE, false).setValue(INGOTS_AT_SE, false).setValue(INGOTS_AT_SW, false).setValue(INGOTS_AT_NW, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DOWN_FILLED, INGOTS_AT_NE, INGOTS_AT_SE, INGOTS_AT_SW, INGOTS_AT_NW);
    }

    public static boolean tryAddingEightIngots(BlockState oldState, LevelAccessor level, BlockPos pos) {
        if (!oldState.getValue(INGOTS_AT_NW)) {
            updateOrDestroy(oldState, oldState.setValue(INGOTS_AT_NW, true), level, pos, 3);
        } else if (!oldState.getValue(INGOTS_AT_NE)) {
            updateOrDestroy(oldState, oldState.setValue(INGOTS_AT_NE, true), level, pos, 3);
        } else if (!oldState.getValue(INGOTS_AT_SE)) {
            updateOrDestroy(oldState, oldState.setValue(INGOTS_AT_SE, true), level, pos, 3);
        } else if (!oldState.getValue(INGOTS_AT_SW)) {
            if (!oldState.getValue(DOWN_FILLED)) {
                updateOrDestroy(oldState, oldState.setValue(DOWN_FILLED, true).setValue(INGOTS_AT_NW, false).setValue(INGOTS_AT_NE, false).setValue(INGOTS_AT_SE, false).setValue(INGOTS_AT_SW, false), level, pos, 3);
            } else {
                updateOrDestroy(oldState, oldState.setValue(INGOTS_AT_SW, true), level, pos, 3);
            }
        } else {
            // Will only be reached when the block is already full of ingots.
            return false;
        }
        // true means eight ingots got added to the blockState.
        return true;
    }

    @Override //todo: Sichergehen, dass diese Methode in Ordnung geht. Könnte schwierig sein, weil es eigentlich über loot_table geregelt sein soll.
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        if (this.drops.isEmpty()) {
            return Collections.emptyList();
        } else {
            LootParams lootparams = params.withParameter(LootContextParams.BLOCK_STATE, state).create(LootContextParamSets.BLOCK);
            ServerLevel serverlevel = lootparams.getLevel();
            LootTable loottable = serverlevel.getServer().reloadableRegistries().getLootTable((ResourceKey)this.drops.get());

            List<ItemStack> drops = loottable.getRandomItems(lootparams);
            List<ItemStack> adjustedDrops = new ArrayList<>();

            for (ItemStack drop : drops) {
                // Dynamisch die Anzahl der Eisenbarren basierend auf dem BlockState ändern
                int amount = calculateDropAmount(state);  // Diese Methode berechnet die Menge dynamisch
                ItemStack newDrop = drop.copy();
                newDrop.setCount(amount);  // Setze die dynamische Anzahl
                adjustedDrops.add(newDrop);
            }

            return adjustedDrops;
        }
    }

    //todo: Was passiert, wenn 0 zurückgegeben wird? Kommt MC damit klar?
    private int calculateDropAmount(BlockState state) {
        int amount = 0;
        if(state.getValue(DOWN_FILLED)) amount = amount + 32;
        if(state.getValue(INGOTS_AT_NW)) amount = amount + 8;
        if(state.getValue(INGOTS_AT_NE)) amount = amount + 8;
        if(state.getValue(INGOTS_AT_SE)) amount = amount + 8;
        if(state.getValue(INGOTS_AT_SW)) amount = amount + 8;

        return amount;
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

    @Override
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

    @Override
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
