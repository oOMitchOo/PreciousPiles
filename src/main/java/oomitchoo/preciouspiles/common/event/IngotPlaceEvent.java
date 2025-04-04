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

        if(player.isShiftKeyDown() && !heldItemStack.isEmpty() && heldItemStack.is(STACKABLE_TAG) && heldItemStack.getCount()>7)
        {
            InteractionHand handHolding = event.getHand();
            BlockPos posBlockClicked = event.getPos();
            BlockState blockStateClicked = level.getBlockState(posBlockClicked);
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

            //First checking if clicked a StackedIngotsBlock, which we can add iron ingots to.
            if (blockStateClicked.getBlock() == stackBlock) {
                //tryAddingEightIngots returns true if eight ingots were added.
                if(StackedIngotsBlock.tryAddingEightIngots(blockStateClicked, level, posBlockClicked)) {
                    placeIngotsInWorld(level, player, handHolding, heldItemStack, posBlockClicked, stackBlock, true);
                    event.setCanceled(true);
                    return;
                }
            }

            Direction face = event.getFace();
            if (face == null) {
                event.setCanceled(true);
                return;
            }
            BlockPos posClickedRel = event.getPos().relative(face);
            BlockState blockStateRel = level.getBlockState(posClickedRel);

            //Second checking if there's an StackedIngotsBlock on the face we're placing on, which we can add ingots to.
            if (blockStateRel.getBlock() == stackBlock) {
                if(StackedIngotsBlock.tryAddingEightIngots(blockStateRel, level, posClickedRel)) {
                    placeIngotsInWorld(level, player, handHolding, heldItemStack, posClickedRel, stackBlock, true);
                    event.setCanceled(true);
                    return;
                }
            }

            //Last doing some checks before placing a new StackedIngotsBlock in world.
            if (face == Direction.UP) { //if placed ON a sturdy block
                boolean sturdyFace = blockStateClicked.isFaceSturdy(level, posBlockClicked, face);
                if (level.isEmptyBlock(posClickedRel) && sturdyFace)
                {
                    placeIngotsInWorld(level, player, handHolding, heldItemStack, posClickedRel, stackBlock, false);
                    event.setCanceled(true);
                    return;
                }
            } else {
                boolean sturdyFaceBelow = level.getBlockState(posClickedRel.below()).isFaceSturdy(level, posClickedRel.below(), Direction.UP);
                if (level.isEmptyBlock(posClickedRel) && sturdyFaceBelow) //if placed UNDER/BESIDES a block but with a sturdy face below
                {
                    placeIngotsInWorld(level, player, handHolding, heldItemStack, posClickedRel, stackBlock, false);
                    event.setCanceled(true);
                    return;
                }
            }
        }
    }

    private static void placeIngotsInWorld(Level level, Player player, InteractionHand hand, ItemStack heldItem, BlockPos placingPos, Block stackBlock, boolean addedToStackedBlock) {
        if (!addedToStackedBlock) {
            BlockState blockState = stackBlock.defaultBlockState();
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
