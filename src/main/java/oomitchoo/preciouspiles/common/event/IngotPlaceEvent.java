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

public class IngotPlaceEvent
{
    public static final TagKey<Item> STACKABLE_TAG = TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("preciouspiles", "stackable_ingot")
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
                InteractionHand handHolding = event.getHand();
                BlockPos posClicked = event.getPos();
                BlockState blockStateClicked = level.getBlockState(posClicked);
                Item heldItem = heldItemStack.getItem();
                Block stackBlock;

                if (heldItem == Items.BRICK) {
                    stackBlock = ModBlocks.BRICKS_STACKED_BLOCK.get();
                } else if (heldItem == Items.COPPER_INGOT) {
                    stackBlock = ModBlocks.COPPER_INGOTS_STACKED_BLOCK.get();
                } else if (heldItem == Items.GOLD_INGOT) {
                    stackBlock = ModBlocks.GOLD_INGOTS_STACKED_BLOCK.get();
                } else if (heldItem == Items.IRON_INGOT) {
                    stackBlock = ModBlocks.IRON_INGOTS_STACKED_BLOCK.get();
                } else if (heldItem == Items.NETHER_BRICK) {
                    stackBlock = ModBlocks.NETHER_BRICKS_STACKED_BLOCK.get();
                } else if (heldItem == Items.NETHERITE_INGOT) {
                    stackBlock = ModBlocks.NETHERITE_INGOTS_STACKED_BLOCK.get();
                } else { //Items.RESIN_BRICK
                    stackBlock = ModBlocks.RESIN_BRICKS_STACKED_BLOCK.get();
                }

                Direction faceClicked = event.getFace();
                if (faceClicked == null) { event.setCanceled(true); return; }

                Vec3 hitLocation = event.getHitVec().getLocation();
                double clicked_X = hitLocation.x;
                double clicked_Y = hitLocation.y;
                double clicked_Z = hitLocation.z;

                double clickedBlockSpaceHeight_Y = clicked_Y - Math.floor(clicked_Y);
                boolean clickedWest = clicked_X - Math.floor(clicked_X) < 0.5;
                boolean clickedNorth = clicked_Z - Math.floor(clicked_Z) < 0.5;
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

                BlockPos posClickedRel = event.getPos().relative(faceClicked);
                BlockState blockStateRel = level.getBlockState(posClickedRel);

                // todo: Aufräumen, damit es leserlich ist.
                // Ich glaube schönstes verhalten ist: clickedAtModBlock --true--> tryPlacingPrecise --false--> tryFilling --isFull--> tryPlacingNewBlock (sturdyFace?) (beside/ontop/under)
                //                                                          --false--> tryPlacingNewBlock (sturdyFace?) (beside/ontop/under)
                if(blockStateClicked.getBlock() == stackBlock) { //clicking on ModBlock (stackBlock)?
                    if(StackedIngotsBlock.tryAddingEightPrecise(clickedBlockSpaceHeight_Y < 0.5, clickedWest, clickedNorth, blockStateClicked, level, posClicked)) { // then try first to add 8 ingots preicse
                        placePilesBlockInWorld(clickedWest, clickedNorth, level, player, handHolding, heldItemStack, posClicked, stackBlock, true);
                        event.setCanceled(true);
                        return;
                    } else if(StackedIngotsBlock.tryAddingEightIngots(blockStateClicked, level, posClicked)) { // tryFilling ModBlock (stackBlock) instead
                        placePilesBlockInWorld(clickedWest, clickedNorth, level, player, handHolding, heldItemStack, posClicked, stackBlock, true);
                        event.setCanceled(true);
                        return;
                    } else { // ModBlock (stackBlock) is already a full precious piles block.
                        if (level.isEmptyBlock(posClickedRel)) { // then try placing a new block in the pos relative to the face clicked on.
                            BlockState blockStateBelow = level.getBlockState(posClickedRel.below());
                            if (blockStateBelow.isFaceSturdy(level, posClickedRel.below(), Direction.UP)) { // placing on sturdy face?
                                placePilesBlockInWorld(clickedWestRel, clickedNorthRel, level, player, handHolding, heldItemStack, posClickedRel, stackBlock, false);
                                event.setCanceled(true);
                                return;
                            } else if (blockStateBelow.getBlock() instanceof StackedIngotsBlock && blockStateBelow.getValue(StackedIngotsBlock.DOWN_FILLED) && blockStateBelow.getValue(StackedIngotsBlock.INGOTS_AT_NW) &&
                                    blockStateBelow.getValue(StackedIngotsBlock.INGOTS_AT_NE) && blockStateBelow.getValue(StackedIngotsBlock.INGOTS_AT_SE) && blockStateBelow.getValue(StackedIngotsBlock.INGOTS_AT_SW)) { // is there a full StackedIngotsBlock below?
                                placePilesBlockInWorld(clickedWestRel, clickedNorthRel, level, player, handHolding, heldItemStack, posClickedRel, stackBlock, false);
                                event.setCanceled(true);
                                return;
                            }
                        } else if (blockStateRel.getBlock() == stackBlock) { // couldn't add to clickedModBlock AND place a new StackedIngotsBlock in relative pos.
                            if(StackedIngotsBlock.tryAddingEightPrecise(clickedBlockSpaceHeight_Y < 0.5, clickedWestRel, clickedNorthRel, blockStateRel, level, posClickedRel)) { // then try first to add 8 ingots preicse
                                placePilesBlockInWorld(clickedWestRel, clickedNorthRel, level, player, handHolding, heldItemStack, posClickedRel, stackBlock, true);
                                event.setCanceled(true);
                                return;
                            } else if(StackedIngotsBlock.tryAddingEightIngots(blockStateRel, level, posClickedRel)) { // tryFilling ModBlock (stackBlock) instead
                                placePilesBlockInWorld(clickedWestRel, clickedNorthRel, level, player, handHolding, heldItemStack, posClickedRel, stackBlock, true);
                                event.setCanceled(true);
                                return;
                            } else {
                                event.setCanceled(true);
                                return;
                            }
                        } else {
                            event.setCanceled(true);
                            return;
                        }
                    }
                } else {
                    if (level.isEmptyBlock(posClickedRel)) { // then try placing a new block in the pos relative to the face clicked on.
                        BlockState blockStateBelow = level.getBlockState(posClickedRel.below());
                        if (blockStateBelow.isFaceSturdy(level, posClickedRel.below(), Direction.UP)) { // placing on sturdy face?
                            placePilesBlockInWorld(clickedWestRel, clickedNorthRel, level, player, handHolding, heldItemStack, posClickedRel, stackBlock, false);
                            event.setCanceled(true);
                            return;
                        } else if (blockStateBelow.getBlock() instanceof StackedIngotsBlock && blockStateBelow.getValue(StackedIngotsBlock.DOWN_FILLED) && blockStateBelow.getValue(StackedIngotsBlock.INGOTS_AT_NW) &&
                                blockStateBelow.getValue(StackedIngotsBlock.INGOTS_AT_NE) && blockStateBelow.getValue(StackedIngotsBlock.INGOTS_AT_SE) && blockStateBelow.getValue(StackedIngotsBlock.INGOTS_AT_SW)) { // is there a full StackedIngotsBlock below?
                            placePilesBlockInWorld(clickedWestRel, clickedNorthRel, level, player, handHolding, heldItemStack, posClickedRel, stackBlock, false);
                            event.setCanceled(true);
                            return;
                        }
                    } else { // Couldn't place a new ModBlock. See if there already is a Modblock on that face to add to.
                        if(blockStateRel.getBlock() == stackBlock) {
                            if(StackedIngotsBlock.tryAddingEightPrecise(clickedBlockSpaceHeight_Y < 0.5, clickedWestRel, clickedNorthRel, blockStateRel, level, posClickedRel)) { // then try first to add 8 ingots precise
                                placePilesBlockInWorld(clickedWestRel, clickedNorthRel, level, player, handHolding, heldItemStack, posClickedRel, stackBlock, true);
                                event.setCanceled(true);
                                return;
                            } else if (StackedIngotsBlock.tryAddingEightIngots(blockStateRel, level, posClickedRel)) { // try adding 8 ingots NOT precise
                                placePilesBlockInWorld(clickedWestRel, clickedNorthRel, level, player, handHolding, heldItemStack, posClickedRel, stackBlock, true);
                                event.setCanceled(true);
                                return;
                            }
                        }
                        event.setCanceled(true);
                        return;
                    }
                }
            }
        }
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
