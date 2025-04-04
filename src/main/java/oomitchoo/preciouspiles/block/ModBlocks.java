package oomitchoo.preciouspiles.block;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import oomitchoo.preciouspiles.PreciousPiles;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(PreciousPiles.MODID);

    public static final DeferredBlock<Block> BRICKS_STACKED_BLOCK = BLOCKS.register("bricks_stacked_block",
            registryName -> new StackedIngotsBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.COLOR_RED)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(2.0F, 6.0F)
            ));

    public static final DeferredBlock<Block> COPPER_INGOTS_STACKED_BLOCK = BLOCKS.register("copper_ingots_stacked_block",
            registryName -> new StackedIngotsBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.COLOR_ORANGE)
                    .requiresCorrectToolForDrops()
                    .strength(3.0F, 6.0F)
                    .sound(SoundType.COPPER)
            ));

    public static final DeferredBlock<Block> GOLD_INGOTS_STACKED_BLOCK = BLOCKS.register("gold_ingots_stacked_block",
            registryName -> new StackedIngotsBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.GOLD)
                    .instrument(NoteBlockInstrument.BELL)
                    .requiresCorrectToolForDrops()
                    .strength(3.0F, 6.0F)
                    .sound(SoundType.METAL)
            ));

    public static final DeferredBlock<Block> IRON_INGOTS_STACKED_BLOCK = BLOCKS.register("iron_ingots_stacked_block",
            registryName -> new StackedIngotsBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.METAL)
                    .instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F, 6.0F)
                    .sound(SoundType.METAL)
            ));

    public static final DeferredBlock<Block> NETHER_BRICKS_STACKED_BLOCK = BLOCKS.register("nether_bricks_stacked_block",
            registryName -> new StackedIngotsBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.NETHER)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(2.0F, 6.0F)
                    .sound(SoundType.NETHER_BRICKS)
            ));

    public static final DeferredBlock<Block> NETHERITE_INGOTS_STACKED_BLOCK = BLOCKS.register("netherite_ingots_stacked_block",
            registryName -> new StackedIngotsBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.COLOR_BLACK)
                    .requiresCorrectToolForDrops()
                    .strength(50.0F, 1200.0F)
                    .sound(SoundType.NETHERITE_BLOCK)
            ));

    public static final DeferredBlock<Block> RESIN_BRICKS_STACKED_BLOCK = BLOCKS.register("resin_bricks_stacked_block",
            registryName -> new StackedIngotsBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.TERRACOTTA_ORANGE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.RESIN_BRICKS)
                    .strength(1.5F, 6.0F)
            ));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}