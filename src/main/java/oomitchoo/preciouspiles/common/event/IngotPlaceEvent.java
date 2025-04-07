package oomitchoo.preciouspiles.common.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import oomitchoo.preciouspiles.block.ModBlocks;
import oomitchoo.preciouspiles.block.StackedIngotsBlock;

import java.util.Map;
import java.util.function.Supplier;

public class IngotPlaceEvent
{
    public static final TagKey<Item> STACKABLE_TAG = TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("preciouspiles", "stackable_ingot")
    );

    private static final Map<Item, Supplier<Block>> ITEM_TO_BLOCK = Map.of(
            Items.BRICK, ModBlocks.BRICKS_STACKED_BLOCK,
            Items.COPPER_INGOT, ModBlocks.COPPER_INGOTS_STACKED_BLOCK,
            Items.GOLD_INGOT, ModBlocks.GOLD_INGOTS_STACKED_BLOCK,
            Items.IRON_INGOT, ModBlocks.IRON_INGOTS_STACKED_BLOCK,
            Items.NETHER_BRICK, ModBlocks.NETHER_BRICKS_STACKED_BLOCK,
            Items.NETHERITE_INGOT, ModBlocks.NETHERITE_INGOTS_STACKED_BLOCK,
            Items.RESIN_BRICK, ModBlocks.RESIN_BRICKS_STACKED_BLOCK
    );

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        Player player = event.getEntity();
        Level level = event.getLevel();
        ItemStack heldItemStack = event.getItemStack();

        if(player.isShiftKeyDown() && !heldItemStack.isEmpty() && heldItemStack.is(STACKABLE_TAG))
        {
            if (player.isCreative() || heldItemStack.getCount()>7) {
                Direction faceClicked = event.getFace();
                if (faceClicked == null) { event.setCanceled(true); return; }

                Vec3 hitLocation = event.getHitVec().getLocation();
                double clicked_X = hitLocation.x;
                double clicked_Y = hitLocation.y;
                double clicked_Z = hitLocation.z;
                double clickedBlockSpace_X = clicked_X - Math.floor(clicked_X);
                double clickedBlockSpace_Y = clicked_Y - Math.floor(clicked_Y);
                double clickedBlockSpace_Z = clicked_Z - Math.floor(clicked_Z);

                boolean clickedWest = clickedBlockSpace_X < 0.5;
                if(clickedBlockSpace_X == 0.5 && faceClicked == Direction.WEST) clickedWest = true; // Für's Setzen innerhalb eines StackedIngotsBlock
                boolean clickedNorth = clickedBlockSpace_Z < 0.5;
                if(clickedBlockSpace_Z == 0.5 && faceClicked == Direction.NORTH) clickedNorth = true; // Für's Setzen innerhalb eines StackedIngotsBlock
                boolean clickedWestRel = clickedWest;
                boolean clickedNorthRel = clickedNorth;

                // Had to do some mirroring for North-South / West-East when placing in the block space beside the hit block space (rel: relative to hit face).
                if(faceClicked != Direction.UP && faceClicked != Direction.DOWN) {
                    if (faceClicked == Direction.WEST || faceClicked == Direction.EAST) {
                        clickedWestRel = faceClicked != Direction.WEST;
                    } else if (faceClicked == Direction.NORTH || faceClicked == Direction.SOUTH) {
                        clickedNorthRel = faceClicked != Direction.NORTH;
                    }
                }

                InteractionHand handHolding = event.getHand();
                BlockPos posClicked = event.getPos();
                BlockState blockStateClicked = level.getBlockState(posClicked);
                Item heldItem = heldItemStack.getItem();
                Block stackBlock = ITEM_TO_BLOCK.getOrDefault(heldItem, ModBlocks.RESIN_BRICKS_STACKED_BLOCK).get();
                BlockPos posClickedRel = event.getPos().relative(faceClicked);
                BlockState blockStateRel = level.getBlockState(posClickedRel);
                BlockPos posBelow = posClickedRel.below();
                BlockState blockStateBelow = level.getBlockState(posBelow);

                // todo: Aufräumen, damit es leserlich ist.
                // Ich glaube schönstes verhalten ist: clickedAtModBlock --true--> tryPlacingPrecise --false--> tryFilling --isFull--> tryPlacingNewBlock (sturdyFace?) (beside/ontop/under)
                //                                                          --false--> tryPlacingNewBlock (sturdyFace?) (beside/ontop/under)
                if(blockStateClicked.getBlock() == stackBlock) { //clicking on ModBlock (stackBlock)?
                    if(StackedIngotsBlock.tryAddingEightPrecise(clickedBlockSpace_Y < 0.5, clickedWest, clickedNorth, blockStateClicked, level, posClicked)) { // then try first to add 8 ingots preicse
                        placePilesBlockInWorld(clickedWest, clickedNorth, level, player, handHolding, heldItemStack, posClicked, stackBlock, true);
                    } else if(StackedIngotsBlock.tryAddingEightIngots(blockStateClicked, level, posClicked)) { // tryFilling ModBlock (stackBlock) instead
                        placePilesBlockInWorld(clickedWest, clickedNorth, level, player, handHolding, heldItemStack, posClicked, stackBlock, true);
                    } else { // ModBlock (stackBlock) is already a full precious piles block.
                        if (level.isEmptyBlock(posClickedRel)) { // then try placing a new block in the pos relative to the face clicked on.
                            if (blockStateBelow.isFaceSturdy(level, posBelow, Direction.UP)) { // placing on sturdy face?
                                placePilesBlockInWorld(clickedWestRel, clickedNorthRel, level, player, handHolding, heldItemStack, posClickedRel, stackBlock, false);
                            } else if (isFullStackedBlock(blockStateBelow)) { // is there a full StackedIngotsBlock below?
                                placePilesBlockInWorld(clickedWestRel, clickedNorthRel, level, player, handHolding, heldItemStack, posClickedRel, stackBlock, false);
                            }
                        } else if (blockStateRel.getBlock() == stackBlock) { // couldn't add to clickedModBlock AND place a new StackedIngotsBlock in relative pos.
                            if(StackedIngotsBlock.tryAddingEightPrecise(clickedBlockSpace_Y < 0.5, clickedWestRel, clickedNorthRel, blockStateRel, level, posClickedRel)) { // then try first to add 8 ingots preicse
                                placePilesBlockInWorld(clickedWestRel, clickedNorthRel, level, player, handHolding, heldItemStack, posClickedRel, stackBlock, true);
                            } else if(StackedIngotsBlock.tryAddingEightIngots(blockStateRel, level, posClickedRel)) { // tryFilling ModBlock (stackBlock) instead
                                placePilesBlockInWorld(clickedWestRel, clickedNorthRel, level, player, handHolding, heldItemStack, posClickedRel, stackBlock, true);
                            }
                        }
                    }
                } else {
                    if (level.isEmptyBlock(posClickedRel)) { // then try placing a new block in the pos relative to the face clicked on.
                        if (blockStateBelow.isFaceSturdy(level, posBelow, Direction.UP)) { // placing on sturdy face?
                            placePilesBlockInWorld(clickedWestRel, clickedNorthRel, level, player, handHolding, heldItemStack, posClickedRel, stackBlock, false);
                        } else if (isFullStackedBlock(blockStateBelow)) { // is there a full StackedIngotsBlock below?
                            placePilesBlockInWorld(clickedWestRel, clickedNorthRel, level, player, handHolding, heldItemStack, posClickedRel, stackBlock, false);
                        }
                    } else { // Couldn't place a new ModBlock. See if there already is a Modblock on that face to add to.
                        if(blockStateRel.getBlock() == stackBlock) {
                            if(StackedIngotsBlock.tryAddingEightPrecise(clickedBlockSpace_Y < 0.5, clickedWestRel, clickedNorthRel, blockStateRel, level, posClickedRel)) { // then try first to add 8 ingots precise
                                placePilesBlockInWorld(clickedWestRel, clickedNorthRel, level, player, handHolding, heldItemStack, posClickedRel, stackBlock, true);
                            } else if (StackedIngotsBlock.tryAddingEightIngots(blockStateRel, level, posClickedRel)) { // try adding 8 ingots NOT precise
                                placePilesBlockInWorld(clickedWestRel, clickedNorthRel, level, player, handHolding, heldItemStack, posClickedRel, stackBlock, true);
                            }
                        }
                    }
                }
                event.setCanceled(true);
            }
        }
    }

    private static boolean isFullStackedBlock(BlockState blockState) {
        return blockState.getBlock() instanceof StackedIngotsBlock &&
                blockState.getValue(StackedIngotsBlock.DOWN_FILLED) &&
                blockState.getValue(StackedIngotsBlock.INGOTS_AT_NW) &&
                blockState.getValue(StackedIngotsBlock.INGOTS_AT_NE) &&
                blockState.getValue(StackedIngotsBlock.INGOTS_AT_SE) &&
                blockState.getValue(StackedIngotsBlock.INGOTS_AT_SW);
    }

    private static void placePilesBlockInWorld(boolean clickedWest, boolean clickedNorth, Level level, Player player, InteractionHand hand, ItemStack heldItem, BlockPos placingPos, Block stackBlock, boolean addedToStackedBlock) {
        if (!addedToStackedBlock) {
            BlockState blockState = stackBlock.defaultBlockState();
            if (clickedWest) { // WEST
                if (clickedNorth) { // WEST-NORTH
                    blockState = blockState.setValue(StackedIngotsBlock.INGOTS_AT_NW, true);
                } else { // WEST-SOUTH
                    blockState = blockState.setValue(StackedIngotsBlock.INGOTS_AT_SW, true);
                }
            } else { // EAST
                if (clickedNorth) { // EAST-NORTH
                    blockState = blockState.setValue(StackedIngotsBlock.INGOTS_AT_NE, true);
                } else { // EAST-SOUTH
                    blockState = blockState.setValue(StackedIngotsBlock.INGOTS_AT_SE, true);
                }
            }
            level.setBlock(placingPos, blockState, 3);
        }
        if (!player.isCreative()) {
            heldItem.shrink(8);
            if (heldItem.isEmpty()) {
                player.setItemInHand(hand, ItemStack.EMPTY);
            }
        }
        player.swing(hand,true);
        level.playSound(null, placingPos.getX(), placingPos.getY(), placingPos.getZ(), SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0F, 0.8F);
    }
}
